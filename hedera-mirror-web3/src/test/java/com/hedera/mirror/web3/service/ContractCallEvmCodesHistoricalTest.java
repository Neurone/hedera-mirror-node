// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.web3.service;

import static com.hedera.mirror.web3.utils.ContractCallTestUtil.EVM_V_34_BLOCK;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_SOLIDITY_ADDRESS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.ByteString;
import com.hedera.mirror.common.domain.transaction.RecordFile;
import com.hedera.mirror.web3.exception.MirrorEvmTransactionException;
import com.hedera.mirror.web3.web3j.generated.EvmCodesHistorical;
import java.math.BigInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ContractCallEvmCodesHistoricalTest extends AbstractContractCallServiceHistoricalTest {
    private RecordFile recordFileAfterEvm34;

    @BeforeEach
    void beforeEach() {
        recordFileAfterEvm34 = recordFilePersist(EVM_V_34_BLOCK);
        setupHistoricalStateInService(EVM_V_34_BLOCK, recordFileAfterEvm34);
    }

    @ParameterizedTest
    @CsvSource({
        // function getCodeHash with accounts that do not exist expected to revert with INVALID_SOLIDITY_ADDRESS
        "00000000000000000000000000000000000000000000000000000000000005ee",
        "00000000000000000000000000000000000000000000000000000000000005e4",
    })
    void testSystemContractCodeHashPreVersion38(String input) {
        final var contract = testWeb3jService.deploy(EvmCodesHistorical::deploy);
        assertThatThrownBy(() -> contract.call_getCodeHash(input).send())
                .isInstanceOf(MirrorEvmTransactionException.class)
                .satisfies(ex -> assertEquals(ex.getMessage(), INVALID_SOLIDITY_ADDRESS.name()));
    }

    @Test
    void testBlockPrevrandao() throws Exception {
        // Given
        final var contract = testWeb3jService.deploy(EvmCodesHistorical::deploy);

        // When
        final var result = contract.call_getBlockPrevrandao().send();

        // Then
        assertThat(result).isNotNull();
        assertTrue(result.compareTo(BigInteger.ZERO) > 0);
    }

    @Test
    void getLatestBlockHashReturnsCorrectValue() throws Exception {
        // Given
        domainBuilder
                .recordFile()
                .customize(f -> f.index(recordFileAfterEvm34.getIndex() + 1))
                .persist();
        final var contract = testWeb3jService.deploy(EvmCodesHistorical::deploy);

        // When
        var result = contract.call_getLatestBlockHash().send();

        // Then
        var expectedResult = ByteString.fromHex(recordFileAfterEvm34.getHash().substring(0, 64))
                .toByteArray();
        assertThat(result).isEqualTo(expectedResult);
    }
}
