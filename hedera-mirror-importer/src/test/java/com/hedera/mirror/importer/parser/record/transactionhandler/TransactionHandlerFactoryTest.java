// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.parser.record.transactionhandler;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.importer.ImporterIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@RequiredArgsConstructor
class TransactionHandlerFactoryTest extends ImporterIntegrationTest {

    private final TransactionHandlerFactory transactionHandlerFactory;

    @EnumSource(TransactionType.class)
    @ParameterizedTest
    void get(TransactionType transactionType) {
        assertThat(transactionHandlerFactory.get(transactionType))
                .isNotNull()
                .extracting(TransactionHandler::getType)
                .isEqualTo(transactionType);
    }
}
