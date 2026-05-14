package com.bix.event_consumer.evaluator;

import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;

public interface MarketEvaluator {
    // Evaluates the result of the slip based on the final score
    // Returns the slip status code (1-PENDING,3-LOST,5-WON,7-VOID)
    int evaluate(Slip slip, Score score);
}
