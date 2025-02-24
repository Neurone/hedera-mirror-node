// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.mirror.common.domain.entity.NftAllowanceHistory;
import com.hedera.mirror.importer.ImporterIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class NftAllowanceHistoryRepositoryTest extends ImporterIntegrationTest {

    private final NftAllowanceHistoryRepository nftAllowanceHistoryRepository;

    @Test
    void prune() {
        domainBuilder.nftAllowanceHistory().persist();
        var nftAllowanceHistory2 = domainBuilder.nftAllowanceHistory().persist();
        var nftAllowanceHistory3 = domainBuilder.nftAllowanceHistory().persist();

        nftAllowanceHistoryRepository.prune(nftAllowanceHistory2.getTimestampUpper());

        assertThat(nftAllowanceHistoryRepository.findAll()).containsExactly(nftAllowanceHistory3);
    }

    @Test
    void save() {
        NftAllowanceHistory nftAllowanceHistory =
                domainBuilder.nftAllowanceHistory().persist();
        assertThat(nftAllowanceHistoryRepository.findById(nftAllowanceHistory.getId()))
                .get()
                .isEqualTo(nftAllowanceHistory);
    }
}
