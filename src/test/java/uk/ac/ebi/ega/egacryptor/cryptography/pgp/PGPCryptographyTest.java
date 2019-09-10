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

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ega.egacryptor.configuration.EgaCryptorConfiguration;
import uk.ac.ebi.ega.egacryptor.cryptography.Cryptography;
import uk.ac.ebi.ega.egacryptor.exception.CryptographyException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertNotNull;
import static uk.ac.ebi.ega.egacryptor.cryptography.util.FileUtils.newEmptyPath;

@TestPropertySource("classpath:application-test.properties")
@ContextConfiguration(classes = EgaCryptorConfiguration.class)
@RunWith(SpringRunner.class)
public class PGPCryptographyTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Autowired
    private Cryptography cryptography;

    @After
    public void cleanTestEnvironment() {
        temporaryFolder.delete();
    }

    @Test
    public void encrypt_WhenGivenOutputStream_ThenReturnsPGPOutputStream() throws IOException, CryptographyException {
        final File outputFolder = temporaryFolder.newFolder("path", "to", "process", "files");
        final File createdFile = new File(outputFolder, "fileToProcess.txt.gpg");
        try (final OutputStream outputStream = new FileOutputStream(createdFile);
             final OutputStream pgpOutputStream = cryptography.encrypt(outputStream)) {
            assertNotNull(pgpOutputStream);
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void encrypt_WhenGivenInvalidOutputStream_ThenThrowsException() throws IOException, CryptographyException {
        try (final OutputStream outputStream = new FileOutputStream(newEmptyPath().toFile());
             final OutputStream pgpOutputStream = cryptography.encrypt(outputStream)) {
            assertNotNull(pgpOutputStream);
        }
    }
}
