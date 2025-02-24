// SPDX-License-Identifier: Apache-2.0

package com.hedera.mirror.importer.reader.signature;

import com.hedera.mirror.importer.domain.StreamFileData;
import com.hedera.mirror.importer.domain.StreamFileSignature;

public interface SignatureFileReader {
    /**
     * 1. Extract the Hash of the content of corresponding RecordStream file. This Hash is the signed Content of this
     * signature 2. Extract signature from the file.
     *
     * @param signatureFileData {@link StreamFileData} object for the signature file
     * @return streamFileSignature containing the hash of the corresponding RecordStream file and the signature
     */
    StreamFileSignature read(StreamFileData signatureFileData);
}
