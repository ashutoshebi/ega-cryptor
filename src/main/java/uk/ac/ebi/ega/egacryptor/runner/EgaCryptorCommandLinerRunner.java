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
package uk.ac.ebi.ega.egacryptor.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import uk.ac.ebi.ega.egacryptor.cryptography.pgp.PGPCryptography;
import uk.ac.ebi.ega.egacryptor.service.IFileDiscoveryService;
import uk.ac.ebi.ega.egacryptor.service.ITaskExecutorService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EgaCryptorCommandLinerRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PGPCryptography.class);

    private final ITaskExecutorService taskExecutorService;
    private final IFileDiscoveryService fileDiscoveryService;

    public EgaCryptorCommandLinerRunner(final ITaskExecutorService taskExecutorService,
                                        final IFileDiscoveryService fileDiscoveryService) {
        this.taskExecutorService = taskExecutorService;
        this.fileDiscoveryService = fileDiscoveryService;
    }

    @Override
    public void run(final String... args) {
        //TODO add options parser to verify arguments.
        //TODO add System.exit() support with proper return value
        LOGGER.trace("Executing EgaCryptorCommandLinerRunner::run(String[])");
        final List<Path> paths = fileDiscoveryService.discoverFilesRecursively(Paths.get("/Users/ashutosh/gpg/test"));
        taskExecutorService.execute(paths, Paths.get(""));
    }
}