/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
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
 *
 */
package uk.ac.ebi.ega.egacryptor.stream;

import uk.ac.ebi.ega.egacryptor.cryptography.util.Hash;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class EncryptInputStream extends InputStream {

    private final MessageDigest messageDigest;
    private final DigestInputStream digestedFileToEncryptInputStream;

    public EncryptInputStream(final InputStream fileToEncryptInputStream) {
        messageDigest = Hash.getMD5();
        digestedFileToEncryptInputStream = new DigestInputStream(fileToEncryptInputStream, messageDigest);
    }

    @Override
    public int read() throws IOException {
        return digestedFileToEncryptInputStream.read();
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return digestedFileToEncryptInputStream.read(bytes);
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        return digestedFileToEncryptInputStream.read(bytes, off, len);
    }

    @Override
    public void close() throws IOException {
        digestedFileToEncryptInputStream.close();
    }

    public String getMD5() {
        return Hash.normalize(messageDigest);
    }
}
