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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.isHidden;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.walk;

public class FileDiscoveryService implements IFileDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDiscoveryService.class);

    @Override
    public List<Path> discoverFilesRecursively(final Path filePath) {
        LOGGER.trace("Executing FileDiscoveryService::discoverFilesRecursively(Path)");
        LOGGER.info("Root FilePath {}", filePath);

        try (final Stream<Path> walk = walk(filePath)) {
            return walk.filter(path -> {
                try {
                    LOGGER.info("FilePath {}", path);
                    return isRegularFile(path) && !isHidden(path);
                } catch (IOException e) {
                    LOGGER.error("Invalid file {}. - {}", path.toString(), e.getMessage());
                }
                return false;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error while listing files in FileDiscoveryService::discoverFilesRecursively(Path). {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
