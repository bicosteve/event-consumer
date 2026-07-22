package com.bix.event_consumer.repositories;

import com.bix.event_consumer.events.BetStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionsRepository {
    private final JdbcTemplate jdbcTemplate;

    public void addTransaction(BetStatusUpdate bet, String createdBy) {
        String query = """
                INSERT INTO transactions
                    (profile_id,reference,type,amount,status,created_by,created_at)
                VALUES (?,?,?,?,?,?,?)
                """;

 try {
 int rowAffected = this.jdbcTemplate.update(
 query,
 bet.getProfileId(),
 bet.getReference(),
 bet.getType(),
 bet.getAmount(),
 bet.getCurrentStatus(),
 createdBy,
 bet.getUpdateAt()
 );

 if(rowAffected < 1){
 log.warn(
 "Could not add the transaction for betId={} and profileId={}",
 bet.getBetId(),
 bet.getProfileId()
 );
 }
} catch (DuplicateKeyException duplicate) {
Map<String, Object> existing = jdbcTemplate.queryForMap(
"SELECT profile_id, reference, type, amount FROM transactions WHERE reference = ?",
bet.getReference()
);
if (sameImmutableBusinessFields(existing, bet)) {
log.info("Settlement transaction {} already exists with identical immutable fields; redelivery ignored", bet.getReference());
return;
}
throw new TransactionReferenceConflictException(bet.getReference());
}
}

private boolean sameImmutableBusinessFields(Map<String, Object> existing, BetStatusUpdate requested) {
return Objects.equals(asLong(existing.get("profile_id")), requested.getProfileId())
&& Objects.equals(String.valueOf(existing.get("reference")), requested.getReference())
&& Objects.equals(asInteger(existing.get("type")), requested.getType())
&& amountEquals(existing.get("amount"), requested.getAmount());
}

private Long asLong(Object value) {
return value instanceof Number number ? number.longValue() : null;
}

private Integer asInteger(Object value) {
return value instanceof Number number ? number.intValue() : null;
}

private boolean amountEquals(Object stored, BigDecimal requested) {
if (stored == null || requested == null) {
return stored == null && requested == null;
}
BigDecimal storedAmount = stored instanceof BigDecimal decimal ? decimal : new BigDecimal(stored.toString());
return storedAmount.compareTo(requested) == 0;
}
}
