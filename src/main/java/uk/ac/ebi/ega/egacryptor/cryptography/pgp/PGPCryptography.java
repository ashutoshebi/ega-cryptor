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
package uk.ac.ebi.ega.egacryptor.cryptography.pgp;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.egacryptor.cryptography.Cryptography;
import uk.ac.ebi.ega.egacryptor.exception.CryptographyException;
import uk.ac.ebi.ega.egacryptor.exception.pgp.PGPCryptographyException;
import uk.ac.ebi.ega.egacryptor.stream.pgp.PGPEncryptedOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Date;

public class PGPCryptography implements Cryptography {

    private static final Logger LOGGER = LoggerFactory.getLogger(PGPCryptography.class);

    private final PGPPublicKey pgpPublicKey;
    private final int bufferSize;

    public PGPCryptography(final InputStream publicKeyringInputStream, final int bufferSize) throws IOException, PGPException {
        this.pgpPublicKey = PGPUtils.readPublicKey(publicKeyringInputStream);
        this.bufferSize = bufferSize;
        installProviderIfNeeded();
    }

    @Override
    public OutputStream encrypt(final OutputStream fileOutputStream) throws CryptographyException {
        try {
            return doEncrypt(fileOutputStream);
        } catch (IOException | PGPException e) {
            LOGGER.error("Error in PGPCryptography::encrypt(File, String) - {}", e.getMessage());
            throw new PGPCryptographyException(e.getMessage(), e);
        }
    }

    @Override
    public InputStream decrypt(final InputStream fileToDecryptInputStream, final char[] password) {
        throw new UnsupportedOperationException();
    }

    private OutputStream doEncrypt(final OutputStream fileOutputStream)
            throws IOException, PGPException {
        final OutputStream pgpEncryptedDataGeneratorOutputStream = PGPUtils.getEncryptedGenerator(pgpPublicKey).open(fileOutputStream, new byte[bufferSize]);
        final PGPCompressedDataGenerator pgpCompressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
        final OutputStream pgpCompressedDataGeneratorOutputStream = pgpCompressedDataGenerator.open(pgpEncryptedDataGeneratorOutputStream);//Don't close this
        final PGPLiteralDataGenerator pgpLiteralDataGenerator = new PGPLiteralDataGenerator();
        final OutputStream pgpLiteralDataGeneratorOutputStream = pgpLiteralDataGenerator.open(pgpCompressedDataGeneratorOutputStream, PGPLiteralData.BINARY, ""
                , new Date(), new byte[bufferSize]);
        return new PGPEncryptedOutputStream(pgpLiteralDataGeneratorOutputStream, pgpCompressedDataGenerator,
                pgpEncryptedDataGeneratorOutputStream);
    }

    private void installProviderIfNeeded() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
