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
package uk.ac.ebi.ega.egacryptor.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ega.egacryptor.pipeline.CryptographyPipeline;
import uk.ac.ebi.ega.egacryptor.pipeline.DefaultCryptographyPipeline;
import uk.ac.ebi.ega.egacryptor.service.FileDiscoveryService;
import uk.ac.ebi.ega.egacryptor.service.IFileDiscoveryService;
import uk.ac.ebi.ega.egacryptor.service.ITaskExecutorService;
import uk.ac.ebi.ega.egacryptor.service.TaskExecutorService;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

@Configuration
public class EgaCryptorConfiguration {

    @Value("${pgp.encryption.buffersize:2048}")
    private int bufferSize;

    @Bean
    public CryptographyPipeline initDefaultCryptographyPipeline(@Value("${pgp.public.key}") String publicKeyPath) throws URISyntaxException {

        final File publicKeyFile = Paths.get(ClassLoader.getSystemResource(publicKeyPath).toURI()).toFile();

        if (!publicKeyFile.exists()) {
            throw new RuntimeException("Public key file " + publicKeyPath + " not found");
        }

        if (bufferSize > 0 && ((bufferSize & (bufferSize - 1)) != 0)) {
            throw new RuntimeException("Buffer size for pgp encryption should be power of 2");
        }
        return new DefaultCryptographyPipeline(publicKeyFile, bufferSize);
    }

    @Bean
    public ITaskExecutorService initTaskExecutorService(final CryptographyPipeline cryptographyPipeline) {
        return new TaskExecutorService(cryptographyPipeline);
    }

    @Bean
    public IFileDiscoveryService initFileDiscoveryService() {
        return new FileDiscoveryService();
    }
}
