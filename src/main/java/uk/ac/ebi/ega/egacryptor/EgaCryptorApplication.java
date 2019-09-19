package uk.ac.ebi.ega.egacryptor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.ega.egacryptor.runner.EgaCryptorCommandLinerRunner;
import uk.ac.ebi.ega.egacryptor.service.IFileDiscoveryService;
import uk.ac.ebi.ega.egacryptor.service.ITaskExecutorService;

@SpringBootApplication
public class EgaCryptorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EgaCryptorApplication.class, args);
    }

    @Bean
    public EgaCryptorCommandLinerRunner initEgaCryptorCommandLinerRunner(final ITaskExecutorService taskExecutorService,
                                                                         final IFileDiscoveryService fileDiscoveryService,
                                                                         final ApplicationContext applicationContext) {
        return new EgaCryptorCommandLinerRunner(taskExecutorService, fileDiscoveryService, applicationContext);
    }
}
