// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.restjava.common;

import com.hedera.mirror.common.domain.entity.EntityId;
import java.util.regex.Pattern;

public record EntityIdNumParameter(EntityId id) implements EntityIdParameter {

    private static final String ENTITY_ID_REGEX = "^((\\d{1,4})\\.)?((\\d{1,5})\\.)?(\\d{1,12})$";
    private static final Pattern ENTITY_ID_PATTERN = Pattern.compile(ENTITY_ID_REGEX);

    public static EntityIdNumParameter valueOf(String id) {
        var matcher = ENTITY_ID_PATTERN.matcher(id);

        if (!matcher.matches()) {
            return null;
        }

        var properties = PROPERTIES.get();
        long shard = properties.getShard();
        long realm = properties.getRealm();

        String secondGroup = matcher.group(2);
        String forthGroup = matcher.group(4);
        if (secondGroup != null && forthGroup != null) {
            shard = Long.parseLong(secondGroup);
            realm = Long.parseLong(forthGroup);
        } else if (secondGroup != null || forthGroup != null) {
            realm = Long.parseLong(secondGroup != null ? secondGroup : forthGroup);
        }

        var num = Long.parseLong(matcher.group(5));
        return new EntityIdNumParameter(EntityId.of(shard, realm, num));
    }

    @Override
    public long shard() {
        return id().getShard();
    }

    @Override
    public long realm() {
        return id().getRealm();
    }
}
