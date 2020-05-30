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

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import uk.ac.ebi.ega.egacryptor.cryptography.pgp.PGPCryptography;
import uk.ac.ebi.ega.egacryptor.model.FileToProcess;
import uk.ac.ebi.ega.egacryptor.service.IFileDiscoveryService;
import uk.ac.ebi.ega.egacryptor.service.ITaskExecutorService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static uk.ac.ebi.ega.egacryptor.runner.CommandLineOptionParser.OPTIONS_HELP;

public class EgaCryptorCommandLinerRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgaCryptorCommandLinerRunner.class);

    private final ITaskExecutorService taskExecutorService;
    private final IFileDiscoveryService fileDiscoveryService;
    private final ApplicationContext applicationContext;
    private final Path defaultOutputFilePath;

    public EgaCryptorCommandLinerRunner(final ITaskExecutorService taskExecutorService,
                                        final IFileDiscoveryService fileDiscoveryService,
                                        final ApplicationContext applicationContext,
                                        final String defaultOutputFilePath) {
        this.taskExecutorService = taskExecutorService;
        this.fileDiscoveryService = fileDiscoveryService;
        this.applicationContext = applicationContext;
        this.defaultOutputFilePath = Paths.get(defaultOutputFilePath);
    }

    @Override
    public void run(final String... args) throws IOException {
        final OptionParser optionParser = CommandLineOptionParser.getOptionParser();
        try {
            final OptionSet optionSet = optionParser.parse(args);

            if (optionSet.has(OPTIONS_HELP)) {
                optionParser.printHelpOn(System.out);
                terminateApplication(ApplicationStatus.SUCCESS::getValue);
            }

            final CommandLineOptionProcessor commandLineOptionProcessor = CommandLineOptionProcessor
                    .processOptions(optionSet, defaultOutputFilePath);
            terminateApplication(() -> doRun(commandLineOptionProcessor));
        } catch (OptionException e) {
            LOGGER.error("Passed invalid command line arguments");
            optionParser.printHelpOn(System.out);
            terminateApplication(ApplicationStatus.INVALID_COMMANDLINE_ARGUMENTS::getValue);
        }
    }

    private int doRun(final CommandLineOptionProcessor parser) {
        LOGGER.info("Process started at {} ---------------", new Date());
        try {
            final List<FileToProcess> fileToProcessList = fileDiscoveryService.discoverFilesRecursively(parser.getFileToEncryptPaths(),
                    parser.getOutputFolderPath());
            if (parser.getNoOfThreads() == 1) {
                taskExecutorService.execute(fileToProcessList);
            } else {
                taskExecutorService.execute(fileToProcessList, parser.getNoOfThreads());
            }
            LOGGER.info("Process completed at {} ---------------", new Date());
            return ApplicationStatus.SUCCESS.getValue();
        } catch (Exception e) {
            LOGGER.error("Error while running an application - ", e);
            return ApplicationStatus.APPLICATION_FAILED.getValue();
        }
    }

    private void terminateApplication(final ExitCodeGenerator exitCodeGenerator) {
        System.exit(SpringApplication.exit(applicationContext, exitCodeGenerator));
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
