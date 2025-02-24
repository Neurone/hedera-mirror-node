// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.web3.state;

import com.hedera.hapi.node.base.Key;
import com.hedera.hapi.node.base.Timestamp;
import com.hedera.pbj.runtime.ParseException;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import java.time.Instant;
import java.util.Arrays;
import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import org.hyperledger.besu.datatypes.Address;

@CustomLog
@UtilityClass
public class Utils {

    public static final long DEFAULT_AUTO_RENEW_PERIOD = 7776000L;
    public static final int EVM_ADDRESS_LEN = 20;
    /* A placeholder to store the 12-byte of zeros prefix that marks an EVM address as a "mirror" address. */
    private static final byte[] MIRROR_PREFIX = new byte[12];

    public static Key parseKey(final byte[] keyBytes) {
        try {
            if (keyBytes != null && keyBytes.length > 0) {
                return Key.PROTOBUF.parse(Bytes.wrap(keyBytes));
            }
        } catch (final ParseException e) {
            return null;
        }

        return null;
    }

    /**
     * Converts a timestamp in nanoseconds to a PBJ Timestamp object.
     *
     * @param timestamp The timestamp in nanoseconds.
     * @return The PBJ Timestamp object.
     */
    public static Timestamp convertToTimestamp(final long timestamp) {
        var instant = Instant.ofEpochSecond(0, timestamp);
        return new Timestamp(instant.getEpochSecond(), instant.getNano());
    }

    public boolean isMirror(final Address address) {
        return isMirror(address.toArrayUnsafe());
    }

    public static boolean isMirror(final byte[] address) {
        if (address.length != EVM_ADDRESS_LEN) {
            return false;
        }

        return Arrays.equals(MIRROR_PREFIX, 0, 12, address, 0, 12);
    }
}
