package com.skala.warehouse_helpdesk.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.skala.warehouse_helpdesk.audit.ToolAuditContext;
import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;
import com.skala.warehouse_helpdesk.dto.AskRequest;
import com.skala.warehouse_helpdesk.dto.ChatSource;
import com.skala.warehouse_helpdesk.tool.PalletTools;
import com.skala.warehouse_helpdesk.tool.TicketTools;
import com.skala.warehouse_helpdesk.tool.WarehouseRegistryTools;

@Service
public class HelpDeskService {

    private final ChatClient chatClient;
    private final WarehouseAiProperties properties;
    private final PalletTools palletTools;
    private final TicketTools ticketTools;
    private final WarehouseRegistryTools warehouseRegistryTools;

    public HelpDeskService(
            ChatClient chatClient,
            WarehouseAiProperties properties,
            PalletTools palletTools,
            TicketTools ticketTools,
            WarehouseRegistryTools warehouseRegistryTools) {
        this.chatClient = chatClient;
        this.properties = properties;
        this.palletTools = palletTools;
        this.ticketTools = ticketTools;
        this.warehouseRegistryTools = warehouseRegistryTools;
    }

    public com.skala.warehouse_helpdesk.dto.ChatResponse chat(AskRequest request) {
        String question = request.question();
        String conversationId = conversationId(request.sessionId());

        ToolAuditContext.start();

        try {
            ChatClientResponse response = chatClient.prompt()
                    .advisors(advisors -> advisors.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(question)
                    .tools(
                        palletTools,
                        ticketTools,
                        warehouseRegistryTools)
                    .call()
                    .chatClientResponse();

            return new com.skala.warehouse_helpdesk.dto.ChatResponse(
                    answer(response.chatResponse()),
                    sources(response.context()),
                    ToolAuditContext.wasToolUsed());
        }
        finally {
            ToolAuditContext.clear();
        }
    }

    public SseEmitter stream(AskRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        String conversationId = conversationId(request.sessionId());
        AtomicReference<List<ChatSource>> latestSources = new AtomicReference<>(List.of());

        chatClient.prompt()
                .advisors(advisors -> advisors.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(request.question())
                .tools(
                    palletTools,
                    ticketTools,
                    warehouseRegistryTools)
                .stream()
                .chatClientResponse()
                .subscribe(
                        response -> sendMessageChunk(emitter, response, latestSources),
                        emitter::completeWithError,
                        () -> completeStream(emitter, latestSources.get()));

        return emitter;
    }

    private String conversationId(String sessionId) {
        String normalizedSessionId = StringUtils.hasText(sessionId) ? sessionId : properties.memory().defaultSessionId();
        return properties.testUser().warehouseId() + ":" + properties.testUser().userId() + ":" + normalizedSessionId;
    }

    private String answer(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResult() == null) {
            return "";
        }

        return chatResponse.getResult().getOutput().getText();
    }

    private void sendMessageChunk(
            SseEmitter emitter,
            ChatClientResponse response,
            AtomicReference<List<ChatSource>> latestSources) {
        List<ChatSource> responseSources = sources(response.context());

        if (!responseSources.isEmpty()) {
            latestSources.set(responseSources);
        }

        String chunk = answer(response.chatResponse());

        if (StringUtils.hasText(chunk)) {
            sendEvent(emitter, "message", chunk);
        }
    }

    private void completeStream(SseEmitter emitter, List<ChatSource> sources) {
        sendEvent(emitter, "sources", sources);
        sendEvent(emitter, "done", "[DONE]");
        emitter.complete();
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        }
        catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }

    private List<ChatSource> sources(Map<String, Object> context) {
        Object retrievedDocuments = context.get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        if (!(retrievedDocuments instanceof List<?> documents)) {
            return List.of();
        }

        return documents.stream()
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .map(this::source)
                .distinct()
                .sorted(Comparator.comparing(ChatSource::document).thenComparing(ChatSource::version))
                .toList();
    }

    private ChatSource source(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return new ChatSource(
                metadataValue(metadata, "source"),
                metadataValue(metadata, "version"));
    }

    private String metadataValue(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value == null ? "" : value.toString();
    }
}
