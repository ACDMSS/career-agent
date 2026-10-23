package org.itanna.langchain4jdemo.consultant.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 会话记忆配置：内存消息仓库 + 按 memoryId 创建记忆窗口的工厂。
 * 不额外提供 ChatMemory Bean，避免所有会话共享同一份记忆。
 */
@Configuration
public class CommonConfig {

    /**
     * 注册内存消息仓库：{@code memoryId -> 消息列表}，应用重启后上下文丢失。
     * 同类型 Bean 只能有一个，否则 @AiService 自动装配会报冲突。
     *
     * @return 内存版会话消息仓库
     */
    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /**
     * 注册记忆工厂：每个 memoryId 一份独立的记忆窗口。
     *
     * @param chatMemoryStore 由 Spring 注入的仓库，与上面的 Bean 是同一实例
     * @return 记忆工厂
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        // ChatMemoryProvider 是函数式接口，lambda 即 get(Object memoryId) 的实现；
        // 窗口构造时会通过 store 自动读出该会话的历史消息，实现多轮上下文。
        return memoryId -> MessageWindowChatMemory.builder()
                // 与 CareerAgentService.chat(@MemoryId ...) 收到的值一致
                .id(memoryId)
                // 只保留最近 20 条，超出丢最旧消息，控制上下文和 token 成本
                .maxMessages(20)
                // 消息读写都委托给仓库，窗口对象自身不存数据
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}
