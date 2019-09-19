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
import uk.ac.ebi.ega.egacryptor.model.FileToProcess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.ega.egacryptor.cryptography.util.FileUtils.newEmptyPath;

@TestPropertySource("classpath:application-test.properties")
@ContextConfiguration(classes = EgaCryptorConfiguration.class)
@RunWith(SpringRunner.class)
public class DefaultCryptographyPipelineTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Autowired
    private CryptographyPipeline cryptographyPipeline;

    @After
    public void cleanTestEnvironment() {
        temporaryFolder.delete();
    }

    @Test
    public void process_WhenGivenValidFilePathToProcess_ThenEncryptFiles() throws IOException {
        final File outputFolder = temporaryFolder.newFolder("path", "to", "process", "files");
        final File createdFile = new File(outputFolder, "fileToProcess.txt");

        try (final FileOutputStream fileOutputStream = new FileOutputStream(createdFile)) {
            fileOutputStream.write("File to encrypt".getBytes());
            fileOutputStream.flush();
        }

        final Path outputFolderPath = outputFolder.toPath().toAbsolutePath();
        final FileToProcess fileToProcess = new FileToProcess(createdFile.toPath().toAbsolutePath(), outputFolderPath);

        cryptographyPipeline.process(fileToProcess);

        assertTrue(new File(outputFolderPath.resolve(createdFile.getName()).toString().concat(".md5")).exists());
        assertTrue(new File(outputFolderPath.resolve(createdFile.getName()).toString().concat(".gpg")).exists());
        assertTrue(new File(outputFolderPath.resolve(createdFile.getName()).toString().concat(".gpg.md5")).exists());
    }

    @Test(expected = RuntimeException.class)
    public void process_WhenGivenInValidInputPath_ThenThrowsException() throws IOException {
        final File outputFolder = temporaryFolder.newFolder("path", "to", "process", "files");
        final FileToProcess fileToProcess = new FileToProcess(newEmptyPath(), outputFolder.toPath().toAbsolutePath());
        cryptographyPipeline.process(fileToProcess);
    }

    @Test(expected = RuntimeException.class)
    public void process_WhenGivenInValidOutputPath_ThenThrowsException() {
        final FileToProcess fileToProcess = new FileToProcess(newEmptyPath().toAbsolutePath(), newEmptyPath());
        cryptographyPipeline.process(fileToProcess);
    }
}
