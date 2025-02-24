// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.mirror.importer.ImporterIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ContractActionRepositoryTest extends ImporterIntegrationTest {

    private final ContractActionRepository contractActionRepository;

    @Test
    void prune() {
        domainBuilder.contractAction().persist();
        var contractAction2 = domainBuilder.contractAction().persist();
        var contractAction3 = domainBuilder.contractAction().persist();

        contractActionRepository.prune(contractAction2.getConsensusTimestamp());

        assertThat(contractActionRepository.findAll()).containsOnly(contractAction3);
    }

    @Test
    void save() {
        var contractAction = domainBuilder.contractAction().get();

        contractActionRepository.save(contractAction);
        assertThat(contractActionRepository.findAll()).containsOnly(contractAction);
    }
}
