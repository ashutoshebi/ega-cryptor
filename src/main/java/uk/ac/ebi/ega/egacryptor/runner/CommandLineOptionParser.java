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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandLineOptionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineOptionParser.class);

    private static final String FILE_TO_ENCRYPT_PATH = "fileToEncryptPaths";
    private static final String OUTPUT_FOLDER_PATH = "outputFolderPath";
    private static final String UTILIZE_FULL_RESOURCE = "f";//No of cores -1
    private static final String UTILIZE_OPTIMIZE_RESOURCE = "o";//75% of total no of cores
    private static final String UTILIZE_HALF_RESOURCE = "h";//50% of total no of cores
    private static final String USER_THREADS = "t";//User specified no of threads

    private final List<Path> fileToEncryptPaths;
    private final Path outputFolderPath;
    private final int noOfThreads;

    static Optional<CommandLineOptionParser> parse(final String... parameters) throws IOException {
        final OptionParser parser = buildParser();
        try {
            return Optional.of(new CommandLineOptionParser(parser.parse(parameters)));
        } catch (OptionException e) {
            LOGGER.error("Passed invalid command line arguments");
            parser.printHelpOn(System.out);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public List<Path> getFileToEncryptPaths() {
        return fileToEncryptPaths;
    }

    public Path getOutputFolderPath() {
        return outputFolderPath;
    }

    public int getNoOfThreads() {
        return noOfThreads;
    }

    private static OptionParser buildParser() {
        OptionParser parser = new OptionParser();
        parser.accepts(FILE_TO_ENCRYPT_PATH, "File(s) to encrypt. Provide file/folder path or comma separated file path if multiple files in double quotes").
                withRequiredArg().
                required().
                ofType(String.class);
        parser.accepts(OUTPUT_FOLDER_PATH, "Path of the output file. This is optional. If not provided then output files will be generated in the same path as that of source file").
                withRequiredArg().
                ofType(String.class).
                defaultsTo("");
        parser.accepts(UTILIZE_FULL_RESOURCE, "");
        parser.accepts(UTILIZE_OPTIMIZE_RESOURCE, "");
        parser.accepts(UTILIZE_HALF_RESOURCE, "");
        parser.accepts(USER_THREADS, "").
                withRequiredArg().
                ofType(Integer.class).
                defaultsTo(1);
        parser.allowsUnrecognizedOptions();
        return parser;
    }

    private CommandLineOptionParser(final OptionSet optionSet) throws FileNotFoundException {
        outputFolderPath = Paths.get(optionSet.valueOf(OUTPUT_FOLDER_PATH).toString());

        final File outputFolder;

        if (!outputFolderPath.toString().isEmpty() && !(outputFolder = outputFolderPath.normalize().toAbsolutePath().toFile()).exists()
                && !outputFolder.mkdirs()) {
            throw new FileNotFoundException("Output directory path doesn't exists. Unable to create directory.");
        }
        fileToEncryptPaths = Arrays.asList(optionSet.valueOf(FILE_TO_ENCRYPT_PATH).toString().split(",")).
                parallelStream().map(filePath -> Paths.get(filePath.trim()).normalize().toAbsolutePath()).collect(Collectors.toList());
        noOfThreads = determineNoOfThreads(optionSet);
        LOGGER.info("Maximum {} no. of threads will be created to process the file(s)", noOfThreads);
    }

    private int determineNoOfThreads(final OptionSet optionSet) {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        LOGGER.info("Application has detected {} no. of cores/processors. Threads will be created based on option provided", availableProcessors);
        if (optionSet.has(UTILIZE_HALF_RESOURCE)) {
            return availableProcessors / 2;
        } else if (optionSet.has(UTILIZE_OPTIMIZE_RESOURCE)) {
            return (int) (availableProcessors * (75.0f / 100.0f));
        } else if (optionSet.has(UTILIZE_FULL_RESOURCE)) {
            return availableProcessors - 1;
        } else if (optionSet.has(USER_THREADS)) {
            final int userDefinedThreads = Integer.parseInt(optionSet.valueOf(USER_THREADS).toString());
            if (userDefinedThreads >= availableProcessors) {
                LOGGER.warn("Provided no. of threads are >= available cores on this machine. Application will try to use maximum resources to process the file(s)");
                return availableProcessors - 1;
            } else if (userDefinedThreads <= 0) {
                LOGGER.warn("Provided no. of threads are <= 0. Application will process the file(s) sequentially using single thread");
                return 1;
            }
            return userDefinedThreads;
        }
        LOGGER.warn("No option has been provided. Application will process the file(s) sequentially using single thread");
        return 1;
    }
}
