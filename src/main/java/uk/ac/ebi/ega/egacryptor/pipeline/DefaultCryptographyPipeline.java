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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.egacryptor.cryptography.Cryptography;
import uk.ac.ebi.ega.egacryptor.cryptography.util.FileUtils;
import uk.ac.ebi.ega.egacryptor.cryptography.util.Hash;
import uk.ac.ebi.ega.egacryptor.exception.CryptographyException;
import uk.ac.ebi.ega.egacryptor.model.FileToProcess;
import uk.ac.ebi.ega.egacryptor.stream.pipeline.DefaultStream;
import uk.ac.ebi.ega.egacryptor.stream.pipeline.PipelineStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import static uk.ac.ebi.ega.egacryptor.constant.FileExtensionType.GPG;
import static uk.ac.ebi.ega.egacryptor.constant.FileExtensionType.MD5;
import static uk.ac.ebi.ega.egacryptor.cryptography.util.FileUtils.writeToFile;

public class DefaultCryptographyPipeline implements CryptographyPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCryptographyPipeline.class);

    private final int bufferSize;
    private final Cryptography cryptography;

    public DefaultCryptographyPipeline(final Cryptography cryptography, final int bufferSize) {
        this.cryptography = cryptography;
        this.bufferSize = bufferSize;
    }

    @Override
    public void process(final FileToProcess fileToProcess) {
        LOGGER.trace("Cryptography process is running");
        LOGGER.debug("filePathToEncrypt={}", fileToProcess);
        try {
            doProcess(fileToProcess);
        } catch (CryptographyException | IOException e) {
            LOGGER.error("Error while executing cryptography pipeline - {}", e.getMessage());
            throw new RuntimeException("Error while processing request", e);
        }
    }

    private void doProcess(final FileToProcess fileToProcess) throws CryptographyException, IOException {
        final File inputFile = fileToProcess.getFileToEncryptPath().toFile();
        final Path outputFilePath = fileToProcess.getOutputFilePath();
        final File outputFile = outputFilePath.toFile();

        if (!outputFile.exists() && !outputFile.mkdir()) {
            throw new FileNotFoundException("Path ".concat(outputFile.getPath()).concat(" doesn't exists. Unable to create path."));
        }

        final File outputFileMD5 = FileUtils.newEmptyPath().resolve(outputFilePath).resolve(inputFile.getName().
                concat(MD5.getFileExtension())).toFile();
        final File outputFileGPG = FileUtils.newEmptyPath().resolve(outputFilePath).resolve(inputFile.getName().
                concat(GPG.getFileExtension())).toFile();
        final File outputFileGPGMD5 = FileUtils.newEmptyPath().resolve(outputFilePath).resolve(inputFile.getName().
                concat(GPG.getFileExtension().concat(MD5.getFileExtension()))).toFile();

        if (outputFileMD5.exists() || outputFileGPG.exists() || outputFileGPGMD5.exists()) {
            LOGGER.info("Process skip for file {}. All or some of these files are already exists - {},{},{}", inputFile.getPath(),
                    outputFileMD5.getPath(), outputFileGPG.getPath(), outputFileGPGMD5.getPath());
            return;
        }

        final MessageDigest inputStreamMessageDigest = Hash.getMD5();
        final MessageDigest outputStreamMessageDigest = Hash.getMD5();

        final DigestInputStream digestInputStream = new DigestInputStream(new FileInputStream(inputFile), inputStreamMessageDigest);//Will be closed in PipelineStream
        long bytesRead;

        try (final DigestOutputStream digestOutputStream = new DigestOutputStream(new FileOutputStream(outputFileGPG), outputStreamMessageDigest)) {
            final OutputStream pgpEncryptedOutputStream = cryptography.encrypt(digestOutputStream);//Will be closed in PipelineStream
            try (final PipelineStream pipelineStream = new DefaultStream(digestInputStream, pgpEncryptedOutputStream, bufferSize)) {
                LOGGER.info("File {} is being processed", inputFile.getPath());
                bytesRead = pipelineStream.execute();
            }
        }
        writeToFile(outputFileMD5, Hash.normalize(inputStreamMessageDigest));
        writeToFile(outputFileGPGMD5, Hash.normalize(outputStreamMessageDigest));
        LOGGER.info("File {} is successfully encrypted. Total bytes read {}. These files have been generated {},{},{}", inputFile.getPath(), bytesRead,
                outputFileMD5.getPath(), outputFileGPG.getPath(), outputFileGPGMD5.getPath());
    }
}
