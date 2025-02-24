// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.restjava.repository;

import com.hedera.mirror.common.domain.entity.AbstractNftAllowance.Id;
import com.hedera.mirror.common.domain.entity.NftAllowance;
import org.springframework.data.repository.CrudRepository;

public interface NftAllowanceRepository extends CrudRepository<NftAllowance, Id>, NftAllowanceRepositoryCustom {}
