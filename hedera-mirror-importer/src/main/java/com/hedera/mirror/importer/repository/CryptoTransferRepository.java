// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import com.hedera.mirror.common.domain.transaction.CryptoTransfer;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CryptoTransferRepository
        extends CrudRepository<CryptoTransfer, CryptoTransfer.Id>, RetentionRepository {

    @Modifying
    @Override
    @Query("delete from CryptoTransfer where consensusTimestamp <= ?1")
    int prune(long consensusTimestamp);
}
