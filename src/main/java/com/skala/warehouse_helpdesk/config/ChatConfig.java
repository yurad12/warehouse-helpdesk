package com.skala.warehouse_helpdesk.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.skala.warehouse_helpdesk.rag.WarehouseKnowledgeAdvisor;

@Configuration
@EnableConfigurationProperties(WarehouseAiProperties.class)
public class ChatConfig {

    @Bean
    ChatMemory chatMemory(WarehouseAiProperties properties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(properties.memory().maxMessages())
                .build();
    }

    @Bean
    ChatClient chatClient(
        ChatClient.Builder builder,
        ChatMemory chatMemory,
        WarehouseAiProperties properties,
        WarehouseKnowledgeAdvisor warehouseKnowledgeAdvisor) {
       return builder
               .defaultSystem(properties.systemPrompt())
               .defaultAdvisors(
                       MessageChatMemoryAdvisor.builder(chatMemory).build(),
                       warehouseKnowledgeAdvisor)
               .build(); 
    }
}
