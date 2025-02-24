// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import com.hedera.mirror.common.domain.topic.TopicMessageLookup;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TopicMessageLookupRepository
        extends CrudRepository<TopicMessageLookup, TopicMessageLookup.Id>, RetentionRepository {
    @Modifying
    @Override
    @Query(value = "delete from topic_message_lookup where upper(timestamp_range) <= ?1", nativeQuery = true)
    int prune(long consensusTimestamp);
}
