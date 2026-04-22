package com.bix.event_consumer.services;


import com.bix.event_consumer.models.Score;
import com.bix.event_consumer.models.Slip;
import com.bix.event_consumer.repositories.BetRepository;
import com.bix.event_consumer.repositories.BetSlipRepository;
import com.bix.event_consumer.repositories.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResultService{
    private final ScoreRepository scoreRepository;
    private final BetSlipRepository betSlipRepository;
    private final BetRepository betRepository;


    public void processBetResults(String eventId){
        log.info("ResultService::processing results for event {} ",eventId);

        // 01. Get event's final score
        Score score = this.scoreRepository.findScoreByEventId(eventId);
        if(score == null){
            log.warn("ResultService::no final score for event {} ",eventId);
            return;
        }

        // 02. Get event's pending slips
        List<Slip> pendingSlips = betSlipRepository.findEventsPendingSlips(eventId);
        if(pendingSlips.isEmpty()){
            log.info("ResultService::no pending slips for event {} ",eventId);
            return;
        }

        // 03. Evaluate each slip
        pendingSlips.forEach(slip -> {});


    }

}
