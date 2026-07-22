package com.bix.event_consumer.repositories;

import com.bix.event_consumer.events.BetStatusUpdate;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionsRepositoryTest {

 @Test
 void treatsUniqueReferenceConflictFromRedeliveryAsAnAlreadyRecordedSettlement() {
 JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
 doThrow(new DuplicateKeyException("Duplicate entry 'settlement:bet:42:status:5'"))
 .when(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), any(), any());
 TransactionsRepository repository = new TransactionsRepository(jdbcTemplate);
 BetStatusUpdate update = BetStatusUpdate.builder()
 .betId(42L)
 .profileId(2L)
 .amount(new BigDecimal("12.50"))
 .currentStatus(5)
 .type(1)
 .reference("settlement:bet:42:status:5")
 .updateAt(LocalDateTime.now())
 .build();

when(jdbcTemplate.queryForMap(anyString(), any())).thenReturn(Map.of(
"profile_id", 2L,
"reference", "settlement:bet:42:status:5",
"type", 1,
"amount", new BigDecimal("12.500")
));

assertDoesNotThrow(() -> repository.addTransaction(update, "TRX-SERVICE"));
}

@Test
void rejectsDuplicateReferenceWhenImmutableBusinessFieldsDiffer() {
JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
doThrow(new DuplicateKeyException("duplicate reference"))
.when(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), any(), any());
when(jdbcTemplate.queryForMap(anyString(), any())).thenReturn(Map.of(
"profile_id", 99L,
"reference", "settlement:bet:42:status:5",
"type", 1,
"amount", new BigDecimal("12.50")
));
TransactionsRepository repository = new TransactionsRepository(jdbcTemplate);
BetStatusUpdate update = BetStatusUpdate.builder()
.betId(42L)
.profileId(2L)
.amount(new BigDecimal("12.50"))
.currentStatus(5)
.type(1)
.reference("settlement:bet:42:status:5")
.updateAt(LocalDateTime.now())
.build();

assertThrows(TransactionReferenceConflictException.class,
() -> repository.addTransaction(update, "TRX-SERVICE"));
}
}
