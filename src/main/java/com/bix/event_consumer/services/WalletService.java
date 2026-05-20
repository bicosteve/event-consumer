package com.bix.event_consumer.services;


import com.bix.event_consumer.repositories.TransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {
    private final TransactionsRepository transactionsRepository;
}
