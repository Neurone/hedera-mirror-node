// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.mirror.common.domain.token.Token;
import com.hedera.mirror.importer.ImporterIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

@RequiredArgsConstructor
class TokenHistoryRepositoryTest extends ImporterIntegrationTest {

    private static final RowMapper<Token> ROW_MAPPER = rowMapper(Token.class);
    private final TokenHistoryRepository tokenHistoryRepository;
    private final TokenRepository tokenRepository;

    @Test
    void save() {
        var tokenHistory = domainBuilder.tokenHistory().persist();
        assertThat(tokenHistoryRepository.findById(tokenHistory.getTokenId()))
                .get()
                .isEqualTo(tokenHistory);
    }

    /**
     * This test verifies that the domain object and table definition are in sync with the history table.
     */
    @Test
    void history() {
        var token = domainBuilder.token().persist();

        jdbcOperations.update("insert into token_history select * from token");
        var tokenHistory = jdbcOperations.query("select * from token_history", ROW_MAPPER);

        assertThat(tokenRepository.findAll()).containsExactly(token);
        assertThat(tokenHistory).containsExactly(token);
    }
}
