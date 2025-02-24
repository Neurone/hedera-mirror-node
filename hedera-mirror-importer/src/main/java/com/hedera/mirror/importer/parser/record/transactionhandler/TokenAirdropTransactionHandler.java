// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.parser.record.transactionhandler;

import com.google.common.collect.Range;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.token.TokenAirdrop;
import com.hedera.mirror.common.domain.token.TokenAirdropStateEnum;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;
import com.hedera.mirror.importer.parser.record.entity.EntityProperties;
import com.hederahashgraph.api.proto.java.TokenID;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

@Named
@RequiredArgsConstructor
class TokenAirdropTransactionHandler extends AbstractTransactionHandler {

    private final EntityListener entityListener;
    private final EntityProperties entityProperties;

    @Override
    protected void doUpdateTransaction(Transaction transaction, RecordItem recordItem) {
        if (!entityProperties.getPersist().isTokenAirdrops() || !recordItem.isSuccessful()) {
            return;
        }

        var pendingAirdrops = recordItem.getTransactionRecord().getNewPendingAirdropsList();
        for (var pendingAirdrop : pendingAirdrops) {
            var pendingAirdropId = pendingAirdrop.getPendingAirdropId();
            var receiver = EntityId.of(pendingAirdropId.getReceiverId());
            var sender = EntityId.of(pendingAirdropId.getSenderId());
            recordItem.addEntityId(receiver);
            recordItem.addEntityId(sender);

            var tokenAirdrop = new TokenAirdrop();
            tokenAirdrop.setState(TokenAirdropStateEnum.PENDING);
            tokenAirdrop.setReceiverAccountId(receiver.getId());
            tokenAirdrop.setSenderAccountId(sender.getId());
            tokenAirdrop.setTimestampRange(Range.atLeast(recordItem.getConsensusTimestamp()));

            TokenID tokenId;
            if (pendingAirdropId.hasFungibleTokenType()) {
                tokenId = pendingAirdropId.getFungibleTokenType();
                var amount = pendingAirdrop.getPendingAirdropValue().getAmount();
                tokenAirdrop.setAmount(amount);
            } else {
                tokenId = pendingAirdropId.getNonFungibleToken().getTokenID();
                var serialNumber = pendingAirdropId.getNonFungibleToken().getSerialNumber();
                tokenAirdrop.setSerialNumber(serialNumber);
            }

            var tokenEntityId = EntityId.of(tokenId);
            recordItem.addEntityId(tokenEntityId);
            tokenAirdrop.setTokenId(tokenEntityId.getId());
            entityListener.onTokenAirdrop(tokenAirdrop);
        }
    }

    @Override
    public TransactionType getType() {
        return TransactionType.TOKENAIRDROP;
    }
}
