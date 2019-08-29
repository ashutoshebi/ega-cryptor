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
package uk.ac.ebi.ega.egacryptor.model;

import java.nio.file.Path;

public class FileToProcess {
    private final Path fileToEncryptPath;
    private final Path outputFilePath;

    public FileToProcess(final Path fileToEncryptPath, final Path outputFilePath) {
        this.fileToEncryptPath = fileToEncryptPath;
        this.outputFilePath = outputFilePath;
    }

    public FileToProcess(final Path fileToEncryptPath) {
        this(fileToEncryptPath, fileToEncryptPath.getParent());
    }

    public Path getFileToEncryptPath() {
        return fileToEncryptPath;
    }

    public Path getOutputFilePath() {
        return outputFilePath;
    }
}
