package com.skala.warehouse_helpdesk.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class RagDataInitializer implements CommandLineRunner {

    private final DocumentIngestService documentIngestService;
    private final ResolutionCaseIndexService resolutionCaseIndexService;
    private final boolean enabled;

    public RagDataInitializer(
            DocumentIngestService documentIngestService,
            ResolutionCaseIndexService resolutionCaseIndexService,
            @Value("${warehouse.rag.ingest-on-startup:true}") boolean enabled) {
        this.documentIngestService = documentIngestService;
        this.resolutionCaseIndexService = resolutionCaseIndexService;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        ingest("damaged-pallet-policy.md");
        ingest("outbound-hold-policy.md");
        resolutionCaseIndexService.reindexAll();
    }

    private void ingest(String source) {
        documentIngestService.ingest(new ClassPathResource("docs/" + source), source);
    }
}
