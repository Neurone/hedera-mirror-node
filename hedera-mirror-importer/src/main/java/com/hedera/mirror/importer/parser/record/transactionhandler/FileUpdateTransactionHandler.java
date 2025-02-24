// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.parser.record.transactionhandler;

import com.hedera.mirror.common.domain.entity.Entity;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.entity.EntityType;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.common.util.DomainUtils;
import com.hedera.mirror.importer.domain.EntityIdService;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;
import jakarta.inject.Named;

@Named
class FileUpdateTransactionHandler extends AbstractEntityCrudTransactionHandler {

    private final FileDataHandler fileDataHandler;

    FileUpdateTransactionHandler(
            EntityIdService entityIdService, EntityListener entityListener, FileDataHandler fileDataHandler) {
        super(entityIdService, entityListener, TransactionType.FILEUPDATE);
        this.fileDataHandler = fileDataHandler;
    }

    @Override
    public EntityId getEntity(RecordItem recordItem) {
        return EntityId.of(recordItem.getTransactionBody().getFileUpdate().getFileID());
    }

    @Override
    protected void doUpdateEntity(Entity entity, RecordItem recordItem) {
        var transactionBody = recordItem.getTransactionBody().getFileUpdate();

        if (transactionBody.hasExpirationTime()) {
            entity.setExpirationTimestamp(DomainUtils.timestampInNanosMax(transactionBody.getExpirationTime()));
        }

        if (transactionBody.hasKeys()) {
            entity.setKey(transactionBody.getKeys().toByteArray());
        }

        if (transactionBody.hasMemo()) {
            entity.setMemo(transactionBody.getMemo().getValue());
        }

        entity.setType(EntityType.FILE);
        entityListener.onEntity(entity);
    }

    @Override
    protected void doUpdateTransaction(Transaction transaction, RecordItem recordItem) {
        if (!recordItem.isSuccessful()) {
            return;
        }

        var contents = recordItem.getTransactionBody().getFileUpdate().getContents();
        fileDataHandler.handle(transaction, contents);
    }
}
