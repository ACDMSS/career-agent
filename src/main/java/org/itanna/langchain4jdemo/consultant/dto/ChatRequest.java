package org.itanna.langchain4jdemo.consultant.dto;

/**
 * /api/chat 的请求体。
 *
 * @param memoryId 会话标识，前端为每段对话生成一个 UUID
 * @param message  用户问题
 * @param profile  学生档案，可以为空
 */
public record ChatRequest(String memoryId, String message, StudentProfile profile) {
}
