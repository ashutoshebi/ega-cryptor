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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.egacryptor.model.FileToProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isHidden;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.walk;
import static uk.ac.ebi.ega.egacryptor.constant.FileExtensionType.GPG;
import static uk.ac.ebi.ega.egacryptor.constant.FileExtensionType.MD5;
import static uk.ac.ebi.ega.egacryptor.constant.FileExtensionType.containsFileExtension;

public class FileDiscoveryService implements IFileDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDiscoveryService.class);

    @SuppressWarnings("unchecked")
    @Override
    public List<FileToProcess> discoverFilesRecursively(final List<Path> rootFilePaths, final Path outputFilePath) {
        LOGGER.trace("Executing file discovery service");

        final List<FileToProcess> retrievedFilePaths = new ArrayList<>();
        rootFilePaths.stream().map(rootFilePath -> {
            try {
                return discoverFiles(rootFilePath, outputFilePath);
            } catch (IOException e) {
                LOGGER.error("Error in file discovery - {}", e.getMessage());
            }
            return Collections.emptyList();
        }).forEach(eachRetrievedFilePaths -> retrievedFilePaths.addAll((List<? extends FileToProcess>) eachRetrievedFilePaths));
        return retrievedFilePaths;
    }

    private List<FileToProcess> discoverFiles(final Path rootFilePath, final Path outputFilePath) throws IOException {
        return walk(rootFilePath).filter(path -> {
            try {
                LOGGER.debug("FilePath {}", path);
                if (isRegularFile(path) && !isHidden(path) && !isDirectory(path) && !containsFileExtension(path.toString())) {
                    final String originalFilePath = path.toString();
                    final File md5File = new File(originalFilePath.concat(MD5.getFileExtension()));
                    final File gpgFile = new File(originalFilePath.concat(GPG.getFileExtension()));
                    final File gpgMd5File = new File(originalFilePath.concat(GPG.getFileExtension().concat(MD5.getFileExtension())));

                    if (gpgFile.exists() || md5File.exists() || gpgMd5File.exists()) {
                        LOGGER.info("Process skip for file {}. All or some of these files are already exists - {},{},{}", originalFilePath, md5File.getPath(),
                                gpgFile.getPath(), gpgMd5File.getPath());
                        return false;
                    }
                    return true;
                }
            } catch (IOException e) {
                LOGGER.error("Invalid file {}. - {}", path.toString(), e.getMessage());
            }
            return false;
        }).map(validFilePath -> calculateOutputPath(rootFilePath, validFilePath, outputFilePath)
        ).collect(Collectors.toList());
    }

    private FileToProcess calculateOutputPath(final Path rootFilePath, final Path subFilePath, final Path outputFilePath) {
        if (outputFilePath.toString().isEmpty()) {
            return new FileToProcess(subFilePath);
        }

        final Path subPathMinusRootPath;
        if (!subFilePath.equals(rootFilePath) &&
                ((subPathMinusRootPath = subFilePath.subpath(rootFilePath.getNameCount(), subFilePath.getNameCount()))).getParent() != null) {
            final Path newOutputFilePath = outputFilePath.resolve(subPathMinusRootPath.getParent());
            return new FileToProcess(subFilePath, newOutputFilePath);
        }
        return new FileToProcess(subFilePath, outputFilePath);
    }
}
