package com.bix.event_consumer.consumers;

import com.bix.event_consumer.events.BetStatusUpdate;
import com.bix.event_consumer.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionsConsumer {
    private final TransactionService transactionService;

    @RabbitListener(queues = "${app.rabbitmq.transactions.queue}")
    public void consume(BetStatusUpdate bet) {
        log.info("Received BetStatusUpdate for betId={}", bet.getBetId());
        try{
            this.transactionService.consumeBetTransactions(bet);
            log.info("Updated bet details for betId={}", bet.getBetId());
        }catch (Exception e){
            log.error(
                    "Error while processing bet status for betId={}:error={}",
                    bet.getBetId(),e.getMessage());
        }
    }

}
