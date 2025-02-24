// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.repository;

import com.hedera.mirror.common.domain.contract.Contract;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ContractRepository extends CrudRepository<Contract, Long> {}
