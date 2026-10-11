package com.skala.warehouse_helpdesk.rag;

import java.time.LocalDate;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DocumentIngestService {

    private final VectorStore vectorStore;

    public DocumentIngestService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public IngestResult ingest(Resource resource, String source) {
        List<Document> documents = new TikaDocumentReader(resource).read();

        List<Document> chunks = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .build()
                .split(documents)
                .stream()
                .map(document -> document.mutate()
                        .metadata("source", source)
                        .metadata("documentType", "POLICY")
                        .metadata("dept", "operation")
                        .metadata("version", LocalDate.now().toString())
                        .build())
                .toList();

        vectorStore.delete("source == '" + source + "'");

        if (!chunks.isEmpty()) {
            vectorStore.add(chunks);
        }

        return new IngestResult(source, chunks.size());
    }
}
