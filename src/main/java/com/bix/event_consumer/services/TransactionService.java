package com.bix.event_consumer.services;

import com.bix.event_consumer.enums.BetStatus;
import com.bix.event_consumer.enums.TransactionType;
import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.producer.TransactionProducer;
import com.bix.event_consumer.repositories.TransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionProducer transactionProducer;
    private final TransactionsRepository transactionsRepository;

    // to be called in producer
    public void publishBetStatus(BetStatusUpdate betStatusUpdate) {
        this.transactionProducer.publish(betStatusUpdate);
    }

    // to be called in consumer
    public void consumeBetTransactions(BetStatusUpdate bet) {
        String createdBy = "TRX-SERVICE";
        if(bet.getCurrentStatus() != BetStatus.WON.getStatus() &&
                bet.getCurrentStatus() != BetStatus.VOID.getStatus()){
            log.info("Bet status {} does not qualify for transaction", bet.getCurrentStatus());
            return;
        }

        if(bet.getCurrentStatus() == BetStatus.WON.getStatus()){
            log.info("Bet status={} qualify for transaction. Setting type to {} CREDIT",
                    bet.getCurrentStatus(),
                    BetStatus.WON.getStatus()
                    );
            bet.setType(TransactionType.CREDIT.getStatus());
        }else if(bet.getCurrentStatus() == BetStatus.VOID.getStatus()){
            log.info("Bet status={} qualify for transaction. Setting type to {} REFUND",
                    bet.getCurrentStatus(),
                    BetStatus.VOID.getStatus()
            );
            bet.setType(TransactionType.REFUND.getStatus());
        }

        log.info("Received BetStatusUpdate is={}", bet);
        this.transactionsRepository.addTransaction(bet,createdBy);
    }
}
