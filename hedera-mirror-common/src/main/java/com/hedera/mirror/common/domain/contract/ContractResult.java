/*
 * Copyright (C) 2019-2025 Hedera Hashgraph, LLC
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

package com.hedera.mirror.common.domain.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hedera.mirror.common.converter.ListToStringSerializer;
import com.hedera.mirror.common.domain.entity.EntityId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

@Data
@Entity
@NoArgsConstructor
@SuperBuilder
public class ContractResult implements Persistable<Long> {

    private Long amount;

    @ToString.Exclude
    private byte[] bloom;

    @ToString.Exclude
    private byte[] callResult;

    @Id
    private Long consensusTimestamp;

    private long contractId;

    @Builder.Default
    @JsonSerialize(using = ListToStringSerializer.class)
    private List<Long> createdContractIds = Collections.emptyList();

    private String errorMessage;

    @ToString.Exclude
    private byte[] failedInitcode;

    @ToString.Exclude
    private byte[] functionParameters;

    private byte[] functionResult; // Temporary field until we can confirm the migration captured everything

    private Long gasConsumed;

    private Long gasLimit;

    private Long gasUsed;

    private EntityId payerAccountId;

    private EntityId senderId;

    private byte[] transactionHash;

    private Integer transactionIndex;

    private int transactionNonce;

    private Integer transactionResult;

    @JsonIgnore
    @Override
    public Long getId() {
        return consensusTimestamp;
    }

    @JsonIgnore
    @Override
    public boolean isNew() {
        return true; // Since we never update and use a natural ID, avoid Hibernate querying before insert
    }

    public ContractTransactionHash toContractTransactionHash() {
        return ContractTransactionHash.builder()
                .consensusTimestamp(consensusTimestamp)
                .hash(transactionHash)
                .entityId(contractId)
                .payerAccountId(payerAccountId.getId())
                .transactionResult(transactionResult)
                .build();
    }
}
