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
package uk.ac.ebi.ega.egacryptor.service;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.ega.egacryptor.model.FileToProcess;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.ega.egacryptor.cryptography.util.FileUtils.newEmptyPath;

public class FileDiscoveryServiceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void cleanTestEnvironment() {
        temporaryFolder.delete();
    }

    @Test
    public void discoverFilesRecursively_WhenCallWithoutOutputDirectory_ReturnsListOfFilesToBeProcessed() throws IOException {
        temporaryFolder.newFolder("path", "to", "process", "files");

        final Path firstCreatedFilePath = temporaryFolder.newFile("path/to/process/files/fileToProcessFirst.txt").toPath();
        final Path secondCreatedFilePath = temporaryFolder.newFile("path/to/process/files/fileToProcessSecond.txt").toPath();
        final List<Path> filesToProcess = Arrays.asList(firstCreatedFilePath, secondCreatedFilePath);
        final FileDiscoveryService fileDiscoveryService = new FileDiscoveryService();
        final List<FileToProcess> fileToProcessList = fileDiscoveryService.discoverFilesRecursively(filesToProcess, newEmptyPath());

        assertNotNull(fileToProcessList);
        assertFalse(fileToProcessList.isEmpty());
        assertEquals(2, fileToProcessList.size());
    }

    @Test
    public void discoverFilesRecursively_WhenCallWithOutputDirectory_ReturnsListOfFilesToBeProcessed() throws IOException {
        final Path outputFolderPath = temporaryFolder.newFolder("output", "folder", "path").toPath();
        temporaryFolder.newFolder("path", "to", "process", "files");

        final Path firstFileToProcessPath = temporaryFolder.newFile("path/to/process/files/firstFileToProcess.txt").toPath();
        final Path secondFileToProcessPath = temporaryFolder.newFile("path/to/process/files/secondFileToProcess.txt").toPath();
        final List<Path> fileToProcessPathList = Arrays.asList(firstFileToProcessPath, secondFileToProcessPath);
        final FileDiscoveryService fileDiscoveryService = new FileDiscoveryService();
        final List<FileToProcess> fileToProcessList = fileDiscoveryService.discoverFilesRecursively(fileToProcessPathList, outputFolderPath);

        assertNotNull(fileToProcessList);
        assertFalse(fileToProcessList.isEmpty());
        assertEquals(2, fileToProcessList.size());

        final FileToProcess firstFileToProcess = fileToProcessList.get(0);

        assertNotNull(firstFileToProcess);
        assertTrue(firstFileToProcess.getFileToEncryptPath().endsWith("path/to/process/files/firstFileToProcess.txt"));
        assertTrue(firstFileToProcess.getOutputFilePath().endsWith("output/folder/path"));

        final FileToProcess secondFileToProcess = fileToProcessList.get(1);

        assertNotNull(secondFileToProcess);
        assertTrue(secondFileToProcess.getFileToEncryptPath().endsWith("path/to/process/files/secondFileToProcess.txt"));
        assertTrue(secondFileToProcess.getOutputFilePath().endsWith("output/folder/path"));
    }

    @Test
    public void discoverFilesRecursively_WhenCallWithEmptyFilePath_ReturnsListFilesInBaseDirectory() {
        final FileDiscoveryService fileDiscoveryService = new FileDiscoveryService();
        final List<FileToProcess> fileToProcessList = fileDiscoveryService.discoverFilesRecursively(Collections.singletonList(newEmptyPath()), newEmptyPath());

        assertNotNull(fileToProcessList);
        assertFalse(fileToProcessList.isEmpty());
    }

    @Test
    public void discoverFilesRecursively_WhenCallDotAsFilePath_ReturnsListFilesInBaseDirectory() {
        final FileDiscoveryService fileDiscoveryService = new FileDiscoveryService();
        final List<FileToProcess> fileToProcessList = fileDiscoveryService.discoverFilesRecursively(Collections.singletonList(Paths.get(".")), newEmptyPath());

        assertNotNull(fileToProcessList);
        assertFalse(fileToProcessList.isEmpty());
    }
}
