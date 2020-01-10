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
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import uk.ac.ebi.ega.egacryptor.cryptography.pgp.PGPCryptography;
import uk.ac.ebi.ega.egacryptor.model.FileToProcess;
import uk.ac.ebi.ega.egacryptor.service.IFileDiscoveryService;
import uk.ac.ebi.ega.egacryptor.service.ITaskExecutorService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EgaCryptorCommandLinerRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PGPCryptography.class);

    private final ITaskExecutorService taskExecutorService;
    private final IFileDiscoveryService fileDiscoveryService;
    private final ApplicationContext applicationContext;

    public EgaCryptorCommandLinerRunner(final ITaskExecutorService taskExecutorService,
                                        final IFileDiscoveryService fileDiscoveryService,
                                        final ApplicationContext applicationContext) {
        this.taskExecutorService = taskExecutorService;
        this.fileDiscoveryService = fileDiscoveryService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(final String... args) throws IOException {
        final Optional<CommandLineOptionParser> optionalParsedArgs = CommandLineOptionParser.parse(args);

        System.exit(SpringApplication.exit(applicationContext,
                () -> optionalParsedArgs
                        .map(this::doRun)
                        .orElse(ApplicationStatus.INVALID_COMMANDLINE_ARGUMENTS.getValue())));

    }

    private int doRun(final CommandLineOptionParser parser) {
        LOGGER.trace("Executing CommandLiner");
        try {
            final List<FileToProcess> fileToProcessList = fileDiscoveryService.discoverFilesRecursively(parser.getFileToEncryptPaths(),
                    parser.getOutputFolderPath());
            if (parser.getNoOfThreads() == 1) {
                taskExecutorService.execute(fileToProcessList);
            } else {
                taskExecutorService.execute(fileToProcessList, parser.getNoOfThreads());
            }
            return ApplicationStatus.SUCCESS.getValue();
        } catch (Exception e) {
            LOGGER.error("Error while running an application - ", e);
            return ApplicationStatus.APPLICATION_FAILED.getValue();
        }
    }

    private enum ApplicationStatus {
        SUCCESS(0),
        APPLICATION_FAILED(1),
        INVALID_COMMANDLINE_ARGUMENTS(2);

        private final int value;

        ApplicationStatus(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}