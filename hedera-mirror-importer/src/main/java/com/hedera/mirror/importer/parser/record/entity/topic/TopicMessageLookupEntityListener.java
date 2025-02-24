// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.parser.record.entity.topic;

import com.hedera.mirror.common.domain.StreamType;
import com.hedera.mirror.common.domain.topic.TopicMessage;
import com.hedera.mirror.common.domain.topic.TopicMessageLookup;
import com.hedera.mirror.common.util.DomainUtils;
import com.hedera.mirror.importer.db.TimePartitionService;
import com.hedera.mirror.importer.exception.ImporterException;
import com.hedera.mirror.importer.parser.record.entity.ConditionOnEntityRecordParser;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;
import com.hedera.mirror.importer.parser.record.entity.EntityProperties;
import com.hedera.mirror.importer.parser.record.entity.ParserContext;
import jakarta.inject.Named;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;

@Named
@Order(3)
@ConditionOnEntityRecordParser
@RequiredArgsConstructor
public class TopicMessageLookupEntityListener implements EntityListener {

    private static final long FILE_CLOSE_INTERVAL_SECS =
            StreamType.RECORD.getFileCloseInterval().toSeconds();
    private static final String TOPIC_MESSAGE_TABLE_NAME = "topic_message";

    private final EntityProperties entityProperties;
    private final ParserContext parserContext;
    private final TimePartitionService timePartitionService;

    @Override
    public boolean isEnabled() {
        var persistProperties = entityProperties.getPersist();
        if (!(persistProperties.isTopics() && persistProperties.isTopicMessageLookups())) {
            return false;
        }

        var partitions = timePartitionService.getTimePartitions(TOPIC_MESSAGE_TABLE_NAME);
        return !partitions.isEmpty();
    }

    @Override
    public void onTopicMessage(TopicMessage topicMessage) throws ImporterException {
        // round down the seconds part of the consensus timestamp to achieve better cache hit rate
        long seconds =
                Instant.ofEpochSecond(0, topicMessage.getConsensusTimestamp()).getEpochSecond();
        long fromSeconds = seconds - (seconds % FILE_CLOSE_INTERVAL_SECS);
        long fromTimestamp = DomainUtils.convertToNanosMax(fromSeconds, 0L);
        long toTimestamp = DomainUtils.convertToNanosMax(fromSeconds + FILE_CLOSE_INTERVAL_SECS, 0L);
        var partitions =
                timePartitionService.getOverlappingTimePartitions(TOPIC_MESSAGE_TABLE_NAME, fromTimestamp, toTimestamp);

        for (var partition : partitions) {
            if (partition.getTimestampRange().contains(topicMessage.getConsensusTimestamp())) {
                var topicMessageLookup = TopicMessageLookup.from(partition.getName(), topicMessage);
                parserContext.merge(topicMessageLookup.getId(), topicMessageLookup, this::mergeTopicMessageLookup);
                break;
            }
        }
    }

    private TopicMessageLookup mergeTopicMessageLookup(TopicMessageLookup cached, TopicMessageLookup newValue) {
        cached.setSequenceNumberRange(cached.getSequenceNumberRange().span(newValue.getSequenceNumberRange()));
        cached.setTimestampRange(cached.getTimestampRange().span(newValue.getTimestampRange()));
        return cached;
    }
}
