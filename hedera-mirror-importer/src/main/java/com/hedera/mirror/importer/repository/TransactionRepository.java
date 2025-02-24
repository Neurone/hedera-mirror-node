// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import com.hedera.mirror.common.domain.transaction.Transaction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, Long>, RetentionRepository {

    @Modifying
    @Override
    @Query("delete from Transaction where consensusTimestamp <= ?1")
    int prune(long consensusTimestamp);
}
