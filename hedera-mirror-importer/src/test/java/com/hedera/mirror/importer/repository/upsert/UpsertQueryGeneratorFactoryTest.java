/*
 * Copyright (C) 2021-2025 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
