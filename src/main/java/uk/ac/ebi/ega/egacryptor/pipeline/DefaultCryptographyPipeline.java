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
package uk.ac.ebi.ega.egacryptor.pipeline;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.egacryptor.cryptography.pgp.PGPCryptography;
import uk.ac.ebi.ega.egacryptor.exception.CryptographyException;
import uk.ac.ebi.ega.egacryptor.stream.EncryptInputStream;
import uk.ac.ebi.ega.egacryptor.stream.EncryptedOutputStream;
import uk.ac.ebi.ega.egacryptor.stream.pipeline.DefaultStream;
import uk.ac.ebi.ega.egacryptor.stream.pipeline.PipelineStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public class DefaultCryptographyPipeline implements CryptographyPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCryptographyPipeline.class);

    private final File publicKeyFile;
    private final int bufferSize;

    public DefaultCryptographyPipeline(final File publicKeyFile, final int bufferSize) {
        this.publicKeyFile = publicKeyFile;
        this.bufferSize = bufferSize;
    }

    @Override
    public void process(final Path filePathToEncrypt, final Path encryptedOutputFilePath) {
        LOGGER.trace("Executing DefaultCryptographyPipeline::process(Path, Path)");
        LOGGER.info("filePathToEncrypt={}, encryptedOutputFilePath={}", filePathToEncrypt, encryptedOutputFilePath);
        try {
            doProcess(filePathToEncrypt, encryptedOutputFilePath);
        } catch (CryptographyException | IOException | PGPException e) {
            LOGGER.error("Error in DefaultCryptographyPipeline::process(Path, Path) - {}", e.getMessage());
            throw new RuntimeException("Error while processing request", e);
        }
    }

    private void doProcess(final Path filePathToEncrypt, final Path encryptedOutputFilePath) throws CryptographyException, IOException, PGPException {
        final File inputFile = filePathToEncrypt.toFile();
        final File outputFile = new File(filePathToEncrypt.toString().concat(".gpg"));//TODO add logic to generate output files in encryptedOutputFilePath folder
        final EncryptInputStream encryptInputStream = getEncryptInputStream(inputFile);
        final EncryptedOutputStream encryptedOutputStream = getEncryptedOutputStream(outputFile);

        try (final PipelineStream pipelineStream = new DefaultStream(encryptInputStream, encryptedOutputStream, bufferSize)) {
            final long bytesRead = pipelineStream.execute();
            LOGGER.info("Total bytes read={}", bytesRead);
        }
        //TODO write plain & encrpted MD5 in a file. Generate 2 files.
    }

    private EncryptInputStream getEncryptInputStream(final File fileToEncrypt) throws FileNotFoundException {
        return new EncryptInputStream(new FileInputStream(fileToEncrypt));
    }

    private EncryptedOutputStream getEncryptedOutputStream(final File encryptedOutputFile) throws CryptographyException, IOException, PGPException {
        return new EncryptedOutputStream(encryptedOutputFile, new PGPCryptography(publicKeyFile, bufferSize));
    }
}
