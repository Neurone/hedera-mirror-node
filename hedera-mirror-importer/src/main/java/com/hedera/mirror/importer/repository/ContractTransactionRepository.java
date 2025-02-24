// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import com.hedera.mirror.common.domain.contract.ContractTransaction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ContractTransactionRepository
        extends CrudRepository<ContractTransaction, ContractTransaction.Id>, RetentionRepository {
    @Modifying
    @Override
    @Query("delete from ContractTransaction where consensusTimestamp <= ?1")
    int prune(long consensusTimestamp);
}
