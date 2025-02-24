// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.restjava.repository;

import com.hedera.mirror.common.domain.token.CustomFee;
import org.springframework.data.repository.CrudRepository;

public interface CustomFeeRepository extends CrudRepository<CustomFee, Long> {}
