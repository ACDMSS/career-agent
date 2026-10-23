package org.itanna.langchain4jdemo.consultant.aiService;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 岗位需求反推成长规划 Agent。
 * 系统提示词见 resources/system.txt，检索到的岗位资料由 ContentRetriever 注入到用户消息中。
 */
@AiService
public interface CareerAgentService {

    /**
     * 流式对话：同一 memoryId 共享上下文，不同 memoryId 互相隔离。
     *
     * @param memoryId 会话标识，对应前端每段对话的 UUID
     * @param message  学生档案 + 用户问题（由 Controller 组装）
     * @return 流式中文回答
     */
    @SystemMessage(fromResource = "system.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}
