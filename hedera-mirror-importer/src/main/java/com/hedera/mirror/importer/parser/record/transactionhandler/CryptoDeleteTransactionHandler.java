// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.parser.record.transactionhandler;

import com.hedera.mirror.common.domain.entity.Entity;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.entity.EntityType;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.importer.domain.EntityIdService;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;
import com.hedera.mirror.importer.util.Utility;
import jakarta.inject.Named;
import lombok.CustomLog;

@CustomLog
@Named
class CryptoDeleteTransactionHandler extends AbstractEntityCrudTransactionHandler {

    CryptoDeleteTransactionHandler(EntityIdService entityIdService, EntityListener entityListener) {
        super(entityIdService, entityListener, TransactionType.CRYPTODELETE);
    }

    @Override
    public EntityId getEntity(RecordItem recordItem) {
        return EntityId.of(recordItem.getTransactionBody().getCryptoDelete().getDeleteAccountID());
    }

    @Override
    protected void doUpdateEntity(Entity entity, RecordItem recordItem) {
        var transactionBody = recordItem.getTransactionBody().getCryptoDelete();
        var obtainerId =
                entityIdService.lookup(transactionBody.getTransferAccountID()).orElse(EntityId.EMPTY);
        if (EntityId.isEmpty(obtainerId)) {
            Utility.handleRecoverableError(
                    "Unable to lookup ObtainerId at consensusTimestamp {}", recordItem.getConsensusTimestamp());
        } else {
            entity.setObtainerId(obtainerId);
        }

        entity.setType(EntityType.ACCOUNT);
        entityListener.onEntity(entity);
        recordItem.addEntityId(obtainerId);
    }
}
