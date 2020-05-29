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
import uk.ac.ebi.ega.egacryptor.pipeline.CryptographyPipeline;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class TaskExecutorService implements ITaskExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutorService.class);
    private final CryptographyPipeline cryptographyPipeline;

    public TaskExecutorService(final CryptographyPipeline cryptographyPipeline) {
        this.cryptographyPipeline = cryptographyPipeline;
    }

    /**
     * Executes process as sequential stream.
     *
     * @param fileToProcessList List of files to process.
     */
    @Override
    public void execute(final List<FileToProcess> fileToProcessList) {
        LOGGER.trace("Sequential task executor is running");
        LOGGER.debug("File to process list size={}", fileToProcessList.size());
        fileToProcessList.forEach(cryptographyPipeline::process);
    }

    /**
     * Executes process in parallel based on thread specified by user or
     * calculated by application based on no. of cores/processors.
     *
     * @param fileToProcessList List of files to process.
     * @param noOfThreads No of threads to process list of files.
     */
    @Override
    public void execute(final List<FileToProcess> fileToProcessList, final int noOfThreads) {
        LOGGER.trace("Parallel task executor is running");
        LOGGER.debug("File to process list size={}, No of threads={}", fileToProcessList.size(), noOfThreads);

        final int noOfThreadsToCreate = Math.min(fileToProcessList.size(), noOfThreads);
        LOGGER.info("Based on file count, {} no. of threads will process the file(s)", noOfThreads);

        final ExecutorService executor = Executors.newFixedThreadPool(noOfThreadsToCreate);
        final List<? extends Future<String>> futureList = fileToProcessList
                .stream()
                .map(fileToProcess -> executor.submit(() -> cryptographyPipeline
                        .process(fileToProcess), fileToProcess.getFileToEncryptPath().toString()))
                .collect(Collectors.toList());

        for (final Future<String> stringFuture : futureList) {
            try {
                stringFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Error while iterating over future list - " + e.getMessage(), e);
            }
        }
        executor.shutdownNow();
    }
}
