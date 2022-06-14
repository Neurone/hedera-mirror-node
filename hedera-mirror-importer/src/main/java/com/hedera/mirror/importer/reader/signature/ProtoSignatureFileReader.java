package com.hedera.mirror.importer.reader.signature;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2022 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import static java.lang.String.format;

import java.io.IOException;
import javax.inject.Named;

import com.hedera.mirror.importer.domain.FileStreamSignature;
import com.hedera.mirror.importer.domain.StreamFileData;
import com.hedera.mirror.importer.exception.InvalidStreamFileException;
import com.hedera.mirror.importer.exception.SignatureFileParsingException;
import com.hedera.services.stream.proto.SignatureFile;
import com.hedera.services.stream.proto.SignatureType;

@Named
public class ProtoSignatureFileReader implements SignatureFileReader {
    public static final int SIGNATURE_FILE_FORMAT_VERSION = 6;

    @Override
    public FileStreamSignature read(StreamFileData signatureFileData) {

        try {
            var signatureFile = readSignatureFile(signatureFileData);

            var fileSignature = signatureFile.getFileSignature();
            var metadataSignature = signatureFile.getMetadataSignature();

            FileStreamSignature fileStreamSignature = new FileStreamSignature();
            fileStreamSignature.setBytes(signatureFileData.getBytes());
            fileStreamSignature.setFileHash(fileSignature.getHashObject().getHash().toByteArray());
            fileStreamSignature.setFileHashSignature(fileSignature.getSignature().toByteArray());
            fileStreamSignature.setFilename(signatureFileData.getFilename());
            fileStreamSignature.setMetadataHash(metadataSignature.getHashObject().getHash().toByteArray());
            fileStreamSignature.setMetadataHashSignature(metadataSignature.getSignature().toByteArray());
            fileStreamSignature.setSignatureType(fromProtobufSigTypeToDomainSigType(fileSignature.getType()));

            return fileStreamSignature;
        } catch (IOException e) {
            throw new SignatureFileParsingException(e);
        }
    }

    private SignatureFile readSignatureFile(StreamFileData signatureFileData) throws IOException {
        var inputStream = signatureFileData.getInputStream();
        int givenFileVersion = inputStream.read();
        if (givenFileVersion != SIGNATURE_FILE_FORMAT_VERSION) {
            throw new InvalidStreamFileException(
                    format("Expected file with version %d, given %d.", SIGNATURE_FILE_FORMAT_VERSION,
                            givenFileVersion));
        }

        var signatureFile = SignatureFile.parseFrom(inputStream);

        if (!signatureFile.hasFileSignature()) {
            throw new InvalidStreamFileException(
                    format("The file %s does not have file signature.", signatureFileData.getFilename()));
        }

        if (!signatureFile.hasMetadataSignature()) {
            throw new InvalidStreamFileException(
                    format("The file %s does not have file metadata signature.", signatureFileData.getFilename()));
        }

        return signatureFile;
    }

    private FileStreamSignature.SignatureType fromProtobufSigTypeToDomainSigType(SignatureType type) {
        if (type != SignatureType.SHA_384_WITH_RSA) {
            throw new SignatureFileParsingException(
                    format("The signature type %s is not supported", type));
        }
        return FileStreamSignature.SignatureType.SHA_384_WITH_RSA;
    }
}
