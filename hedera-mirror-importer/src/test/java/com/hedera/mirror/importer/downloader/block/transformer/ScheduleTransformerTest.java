// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.downloader.block.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.TransactionID;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScheduleTransformerTest extends AbstractTransformerTest {

    @Test
    void scheduleCreate() {
        // given
        var accountId = recordItemBuilder.accountId();
        var expectedRecordItem = recordItemBuilder
                .scheduleCreate()
                .receipt(r -> r.setScheduledTransactionID(
                        TransactionID.newBuilder().setAccountID(accountId).build()))
                .customize(this::finalize)
                .build();
        var blockItem = blockItemBuilder.scheduleCreate(expectedRecordItem).build();
        var blockFile = blockFileBuilder.items(List.of(blockItem)).build();

        // when
        var recordFile = blockFileTransformer.transform(blockFile);

        // then
        assertRecordFile(recordFile, blockFile, items -> assertThat(items).containsExactly(expectedRecordItem));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void scheduleCreateWithIdenticalSchedule(boolean scheduleExecuted) {
        // given
        var accountId = recordItemBuilder.accountId();
        var expectedRecordItem = recordItemBuilder
                .scheduleCreate()
                .receipt(r -> {
                    if (scheduleExecuted) {
                        r.setScheduledTransactionID(TransactionID.newBuilder().setAccountID(accountId));
                    }
                })
                .status(ResponseCodeEnum.IDENTICAL_SCHEDULE_ALREADY_CREATED)
                .customize(this::finalize)
                .build();
        var blockItem = blockItemBuilder.scheduleCreate(expectedRecordItem).build();
        var blockFile = blockFileBuilder.items(List.of(blockItem)).build();

        // when
        var recordFile = blockFileTransformer.transform(blockFile);

        // then
        assertRecordFile(recordFile, blockFile, items -> assertThat(items).containsExactly(expectedRecordItem));
    }

    @Test
    void scheduleCreateUnsuccessful() {
        // given
        var expectedRecordItem = recordItemBuilder
                .scheduleCreate()
                .receipt(r -> r.clearScheduleID().setStatus(ResponseCodeEnum.INVALID_TRANSACTION))
                .customize(this::finalize)
                .build();
        var blockItem = blockItemBuilder.scheduleCreate(expectedRecordItem).build();
        var blockFile = blockFileBuilder.items(List.of(blockItem)).build();

        // when
        var recordFile = blockFileTransformer.transform(blockFile);

        // then
        assertRecordFile(recordFile, blockFile, items -> assertThat(items).containsExactly(expectedRecordItem));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void scheduleSign(boolean triggerExecution) {
        // given
        var builder = recordItemBuilder.scheduleSign();
        if (triggerExecution) {
            builder.receipt(r -> r.setScheduledTransactionID(
                    TransactionID.newBuilder().setAccountID(recordItemBuilder.accountId())));
        }

        var expectedRecordItem = builder.customize(this::finalize).build();
        var blockItem = blockItemBuilder.scheduleSign(expectedRecordItem).build();
        var blockFile = blockFileBuilder.items(List.of(blockItem)).build();

        // when
        var recordFile = blockFileTransformer.transform(blockFile);

        // then
        assertRecordFile(recordFile, blockFile, items -> assertThat(items).containsExactly(expectedRecordItem));
    }

    @Test
    void scheduleSignUnsuccessful() {
        // given
        var expectedRecordItem = recordItemBuilder
                .scheduleSign()
                .receipt(r -> r.setStatus(ResponseCodeEnum.INVALID_TRANSACTION))
                .customize(this::finalize)
                .build();
        var blockItem = blockItemBuilder.scheduleSign(expectedRecordItem).build();
        var blockFile = blockFileBuilder.items(List.of(blockItem)).build();

        // when
        var recordFile = blockFileTransformer.transform(blockFile);

        // then
        assertRecordFile(recordFile, blockFile, items -> assertThat(items).containsExactly(expectedRecordItem));
    }

    @Test
    void scheduledTransaction() {
        // given
        var scheduleId = recordItemBuilder.scheduleId();
        var expectedRecordItem = recordItemBuilder
                .cryptoTransfer()
                .record(r -> r.setScheduleRef(scheduleId))
                .customize(this::finalize)
                .build();
        var blockItem = blockItemBuilder
                .cryptoTransfer(expectedRecordItem)
                .transactionResult(r -> r.setScheduleRef(scheduleId))
                .build();
        var blockFile = blockFileBuilder.items(List.of(blockItem)).build();

        // when
        var recordFile = blockFileTransformer.transform(blockFile);

        // then
        assertRecordFile(recordFile, blockFile, items -> assertThat(items).containsExactly(expectedRecordItem));
    }
}
