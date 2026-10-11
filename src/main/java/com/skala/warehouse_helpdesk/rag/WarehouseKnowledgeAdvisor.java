package com.skala.warehouse_helpdesk.rag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import com.skala.warehouse_helpdesk.config.WarehouseAiProperties;

@Component
public class WarehouseKnowledgeAdvisor implements BaseAdvisor {

    private final KnowledgeSearchService knowledgeSearchService;
    private final WarehouseAiProperties properties;

    public WarehouseKnowledgeAdvisor(
            KnowledgeSearchService knowledgeSearchService,
            WarehouseAiProperties properties) {
        this.knowledgeSearchService = knowledgeSearchService;
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        UserMessage userMessage = request.prompt().getUserMessage();
        String query = userMessage.getText() == null ? "" : userMessage.getText();
        List<Document> policies = knowledgeSearchService.searchPolicies(query);
        List<Document> resolutionCases = knowledgeSearchService.searchResolutionCases(
                query,
                properties.testUser().warehouseId());

        List<Document> documents = new ArrayList<>(policies);
        documents.addAll(resolutionCases);

        Map<String, Object> context = new HashMap<>(request.context());
        context.put(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS, documents);

        String augmentedUserText = """
                %s

                다음 검색 자료만 근거로 답한다.
                공식 정책과 유사 처리 사례가 충돌하면 공식 정책을 우선한다.
                유사 처리 사례를 공식 규정처럼 표현하지 않는다.

                [공식 정책]
                %s

                [유사 처리 사례]
                %s
                """.formatted(query, contextText(policies), contextText(resolutionCases));

        return request.mutate()
                .prompt(request.prompt().augmentUserMessage(augmentedUserText))
                .context(context)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        ChatResponse.Builder chatResponse = ChatResponse.builder();
        if (response.chatResponse() != null) {
            chatResponse.from(response.chatResponse());
        }
        chatResponse.metadata(
                QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS,
                response.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS));
        return ChatClientResponse.builder()
                .chatResponse(chatResponse.build())
                .context(response.context())
                .build();
    }

    private String contextText(List<Document> documents) {
        if (documents.isEmpty()) {
            return "관련 자료 없음";
        }
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
