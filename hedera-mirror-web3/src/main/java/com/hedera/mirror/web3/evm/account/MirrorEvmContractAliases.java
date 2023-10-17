/*
 * Copyright (C) 2019-2023 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hedera.mirror.web3.evm.account;

import static com.hedera.services.utils.MiscUtils.isRecoveredEvmAddress;

import com.hedera.mirror.web3.common.ContractCallContext;
import com.hedera.mirror.web3.evm.store.Store;
import com.hedera.mirror.web3.evm.store.Store.OnMissing;
import com.hedera.node.app.service.evm.accounts.HederaEvmContractAliases;
import com.hedera.node.app.service.evm.utils.EthSigsUtils;
import com.hedera.services.jproto.JECDSASecp256k1Key;
import com.hedera.services.jproto.JKey;
import edu.umd.cs.findbugs.annotations.Nullable;
import jakarta.inject.Named;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Address;

@RequiredArgsConstructor
@Named
public class MirrorEvmContractAliases extends HederaEvmContractAliases {

    final Store store;

    public boolean maybeLinkEvmAddress(@Nullable final JKey key, final Address address) {
        final var evmAddress = tryAddressRecovery(key);
        if (isRecoveredEvmAddress(evmAddress)) {
            if (evmAddress != null) { // NOSONAR - null check is required
                link(Address.wrap(Bytes.wrap(evmAddress)), address);
            }
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private byte[] tryAddressRecovery(@Nullable final JKey key) {
        if (key != null) {
            // Only compressed keys are stored at the moment
            final var keyBytes = key.getECDSASecp256k1Key();
            if (keyBytes.length == JECDSASecp256k1Key.ECDSA_SECP256K1_COMPRESSED_KEY_LENGTH) {
                final var evmAddress = EthSigsUtils.recoverAddressFromPubKey(keyBytes);
                if (isRecoveredEvmAddress(evmAddress)) {
                    return evmAddress;
                }
            }
        }
        return null;
    }

    @Override
    public Address resolveForEvm(Address addressOrAlias) {
        if (isMirror(addressOrAlias)) {
            return addressOrAlias;
        }

        return resolveFromAliases(addressOrAlias).orElseGet(() -> resolveFromStore(addressOrAlias));
    }

    private Optional<Address> resolveFromAliases(Address alias) {
        ContractCallContext contractCallContext = ContractCallContext.get();
        Map<Address, Address> pendingAliases = contractCallContext.getPendingAliases();
        if (pendingAliases.containsKey(alias)) {
            return Optional.ofNullable(pendingAliases.get(alias));
        }
        Map<Address, Address> aliases = contractCallContext.getAliases();
        if (aliases.containsKey(alias)
                && !contractCallContext.getPendingRemovals().contains(alias)) {
            return Optional.ofNullable(aliases.get(alias));
        }
        return Optional.empty();
    }

    private Address resolveFromStore(Address addressOrAlias) {
        final var account = store.getAccount(addressOrAlias, OnMissing.DONT_THROW);

        if (account.isEmptyAccount()) {
            return Address.ZERO;
        } else {
            final var resolvedAddress = account.getId().asEvmAddress();

            link(addressOrAlias, resolvedAddress);
            return resolvedAddress;
        }
    }

    public boolean isInUse(final Address address) {
        return ContractCallContext.get().containsAlias(address);
    }

    public void link(final Address alias, final Address address) {
        ContractCallContext contractCallContext = ContractCallContext.get();
        contractCallContext.getPendingAliases().put(alias, address);
        contractCallContext.getPendingRemovals().remove(alias);
    }

    public void unlink(Address alias) {
        ContractCallContext contractCallContext = ContractCallContext.get();
        contractCallContext.getPendingRemovals().add(alias);
        contractCallContext.getPendingAliases().remove(alias);
    }

    public void commit() {
        ContractCallContext contractCallContext = ContractCallContext.get();
        Map<Address, Address> aliases = contractCallContext.getAliases();
        aliases.putAll(contractCallContext.getPendingAliases());
        aliases.keySet().removeAll(contractCallContext.getPendingRemovals());

        resetPendingChanges();
    }

    public void resetPendingChanges() {
        ContractCallContext contractCallContext = ContractCallContext.get();
        contractCallContext.getPendingAliases().clear();
        contractCallContext.getPendingRemovals().clear();
    }
}
