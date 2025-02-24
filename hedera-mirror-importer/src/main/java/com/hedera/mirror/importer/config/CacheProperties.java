// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "hedera.mirror.importer.cache")
public class CacheProperties {

    @NotBlank
    private String addressBook = "maximumSize=100,expireAfterWrite=5m,recordStats";

    @NotBlank
    private String alias = "maximumSize=100000,expireAfterAccess=30m,recordStats";

    private boolean enabled = true;

    @NotBlank
    private String timePartition = "maximumSize=50,expireAfterWrite=1d,recordStats";

    @NotBlank
    private String timePartitionOverlap = "maximumSize=50,expireAfterWrite=1d,recordStats";
}
