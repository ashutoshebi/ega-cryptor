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

public class TaskExecutorService implements ITaskExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDiscoveryService.class);
    private final CryptographyPipeline cryptographyPipeline;

    public TaskExecutorService(final CryptographyPipeline cryptographyPipeline) {
        this.cryptographyPipeline = cryptographyPipeline;
    }

    @Override
    public void execute(final List<FileToProcess> fileToProcessList) {
        LOGGER.trace("Task executor is running");
        LOGGER.debug("Path list size={}", fileToProcessList.size());
        fileToProcessList.parallelStream().forEach(cryptographyPipeline::process);
    }
}
