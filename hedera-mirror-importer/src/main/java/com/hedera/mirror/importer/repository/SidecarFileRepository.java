// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import com.hedera.mirror.common.domain.transaction.SidecarFile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SidecarFileRepository extends CrudRepository<SidecarFile, SidecarFile.Id>, RetentionRepository {

    @Modifying
    @Query("delete from SidecarFile where consensusEnd <= ?1")
    int prune(long consensusTimestamp);
}
