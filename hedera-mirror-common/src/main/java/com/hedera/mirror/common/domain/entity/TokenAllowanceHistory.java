// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.common.domain.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class TokenAllowanceHistory extends AbstractTokenAllowance {
    // Only the parent class should contain fields so that they're shared with both the history and non-history tables.
}
