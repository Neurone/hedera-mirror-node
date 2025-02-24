// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository.upsert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hedera.mirror.common.domain.entity.Entity;
import com.hedera.mirror.common.domain.token.DissociateTokenTransfer;
import com.hedera.mirror.importer.ImporterIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class UpsertQueryGeneratorFactoryTest extends ImporterIntegrationTest {

    private final DissociateTokenTransferUpsertQueryGenerator customUpsertQueryGenerator;
    private final UpsertQueryGeneratorFactory factory;

    @Test
    void getExistingGenerator() {
        assertThat(factory.get(DissociateTokenTransfer.class)).isEqualTo(customUpsertQueryGenerator);
    }

    @Test
    void getGenericGenerator() {
        assertThat(factory.get(Entity.class)).isInstanceOf(GenericUpsertQueryGenerator.class);
    }

    @Test
    void unsupportedClass() {
        assertThatThrownBy(() -> factory.get(Object.class))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not annotated with @Upsertable");
    }
}
