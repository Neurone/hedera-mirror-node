// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.exception;

import com.hedera.mirror.common.exception.MirrorNodeException;

public abstract class ImporterException extends MirrorNodeException {

    private static final long serialVersionUID = -4366690969696518274L;

    protected ImporterException(String message) {
        super(message);
    }

    protected ImporterException(Throwable throwable) {
        super(throwable);
    }

    protected ImporterException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
