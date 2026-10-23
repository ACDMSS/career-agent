package org.itanna.langchain4jdemo.consultant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.charset.StandardCharsets;

/**
 * 统一异常处理：保证接口出错时返回中文纯文本，而不是堆栈信息。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final MediaType TEXT_PLAIN_UTF8 = new MediaType("text", "plain", StandardCharsets.UTF_8);

    /** 参数校验失败 → 400，返回中文原因。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException error) {
        return ResponseEntity.badRequest()
                .contentType(TEXT_PLAIN_UTF8)
                .body("请求参数有误：" + error.getMessage());
    }

    /** 请求体不是合法 JSON → 400，提示正确格式。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableBody(HttpMessageNotReadableException error) {
        return ResponseEntity.badRequest()
                .contentType(TEXT_PLAIN_UTF8)
                .body("请求体格式不正确，请提交 JSON，例如：{\"memoryId\":\"...\",\"message\":\"...\",\"profile\":{...}}");
    }

    /** 访问不存在的静态资源（如 favicon.ico）→ 404，不当成服务异常。 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNotFound(NoResourceFoundException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(TEXT_PLAIN_UTF8)
                .body("资源不存在：" + error.getResourcePath());
    }

    /** 未预期异常 → 500，向前端返回通用提示，避免泄露内部细节。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception error) {
        log.error("接口处理失败：{}", error.getClass().getName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(TEXT_PLAIN_UTF8)
                .body("服务暂时不可用，请稍后重试；若持续失败，请检查应用日志。");
    }
}
