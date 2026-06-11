package com.bix.event_consumer.services;

import com.bix.event_consumer.repositories.TransactionsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private TransactionsRepository transactionsRepository;

    @Test
    void testCanInstantiateWalletService() {
        WalletService walletService = new WalletService(transactionsRepository);
        assertNotNull(walletService);
    }
}
