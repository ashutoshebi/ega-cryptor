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
package uk.ac.ebi.ega.egacryptor.stream.pgp;

import org.bouncycastle.openpgp.PGPCompressedDataGenerator;

import java.io.IOException;
import java.io.OutputStream;

public class PGPEncryptedOutputStream extends OutputStream {

    private final OutputStream encryptedOutputStream;
    private final PGPCompressedDataGenerator pgpCompressedDataGenerator;
    private final OutputStream pgpEncryptedDataGeneratorOutputStream;

    public PGPEncryptedOutputStream(final OutputStream encryptedOutputStream,
                                    final PGPCompressedDataGenerator pgpCompressedDataGenerator,
                                    final OutputStream pgpEncryptedDataGeneratorOutputStream) {
        this.encryptedOutputStream = encryptedOutputStream;
        this.pgpCompressedDataGenerator = pgpCompressedDataGenerator;
        this.pgpEncryptedDataGeneratorOutputStream = pgpEncryptedDataGeneratorOutputStream;
    }

    @Override
    public void write(int b) throws IOException {
        encryptedOutputStream.write(b);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        encryptedOutputStream.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        encryptedOutputStream.write(bytes, off, len);
    }

    @Override
    public void close() throws IOException {
        encryptedOutputStream.close();
        pgpCompressedDataGenerator.close();
        pgpEncryptedDataGeneratorOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        encryptedOutputStream.flush();
    }
}
