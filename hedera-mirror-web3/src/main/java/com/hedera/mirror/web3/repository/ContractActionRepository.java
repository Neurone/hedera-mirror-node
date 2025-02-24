// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.web3.repository;

import com.hedera.mirror.common.domain.contract.ContractAction;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ContractActionRepository extends CrudRepository<ContractAction, Long> {

    @Query(
            value =
                    "select * from contract_action where consensus_timestamp = ?1 and result_data_type = 12 and call_type = 4 order by index asc",
            nativeQuery = true)
    List<ContractAction> findFailedSystemActionsByConsensusTimestamp(long consensusTimestamp);
}
