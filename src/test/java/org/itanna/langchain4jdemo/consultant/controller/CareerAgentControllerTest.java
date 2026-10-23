package org.itanna.langchain4jdemo.consultant.controller;

import org.itanna.langchain4jdemo.consultant.aiService.CareerAgentService;
import org.itanna.langchain4jdemo.consultant.dto.ChatRequest;
import org.itanna.langchain4jdemo.consultant.dto.StudentProfile;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CareerAgentControllerTest {

    /** 档案应被拼成文本传给 Agent，并返回其流式结果。 */
    @Test
    void shouldStreamAnswerAndPassProfileAsText() {
        FakeCareerAgentService service = new FakeCareerAgentService();
        CareerAgentController controller = new CareerAgentController(service);

        String answer = join(controller.chat(new ChatRequest("memory-1", "帮我规划", profile("Java后端开发"))));

        assertThat(answer).isEqualTo("测试回答");
        assertThat(service.lastMemoryId).isEqualTo("memory-1");
        assertThat(service.lastMessage)
                .contains("目标岗位：Java后端开发")
                .contains("已掌握技能：Java、Spring Boot")
                .contains("每周可投入时间：8 小时/周")
                .contains("【学生问题】\n帮我规划");
    }

    /** memoryId 或 message 为空白时应抛参数异常。 */
    @Test
    void shouldRejectBlankMessageAndMemoryId() {
        CareerAgentController controller = new CareerAgentController(new FakeCareerAgentService());

        assertThatThrownBy(() -> controller.chat(new ChatRequest("  ", "帮我规划", profile("Java后端开发"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("memoryId");
        assertThatThrownBy(() -> controller.chat(new ChatRequest("memory-1", "   ", profile("Java后端开发"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("message");
    }

    /** 档案为空时应提示补充目标岗位和技能，而不是调用模型。 */
    @Test
    void shouldAskForProfileWhenProfileIsMissing() {
        CareerAgentController controller = new CareerAgentController(new FakeCareerAgentService());

        String answer = join(controller.chat(new ChatRequest("memory-1", "帮我规划", null)));

        assertThat(answer).contains("目标岗位").contains("已掌握技能");
    }

    /** 模型异常时应返回中文提示，且不泄露上游错误详情。 */
    @Test
    void shouldReturnFriendlyErrorWithoutLeakingDetails() {
        CareerAgentService failingService = (memoryId, message) ->
                Flux.error(new IllegalStateException("401 Unauthorized from upstream"));
        CareerAgentController controller = new CareerAgentController(failingService);

        String answer = join(controller.chat(new ChatRequest("memory-1", "帮我规划", profile("Java后端开发"))));

        assertThat(answer).contains("调用模型失败");
        assertThat(answer).doesNotContain("401");
    }

    /** 把流式分片拼成完整字符串，便于断言。 */
    private String join(Flux<String> flux) {
        List<String> chunks = flux.collectList().block();
        assertThat(chunks).isNotNull();
        return String.join("", chunks);
    }

    /** 构造测试用学生档案。 */
    private StudentProfile profile(String targetRole) {
        return new StudentProfile("计算机类", "大二", targetRole,
                List.of("Java", "Spring Boot"), 8, "就业");
    }

    private static final class FakeCareerAgentService implements CareerAgentService {

        private String lastMemoryId;
        private String lastMessage;

        /** 记录入参并返回固定回答，方便断言。 */
        @Override
        public Flux<String> chat(String memoryId, String message) {
            this.lastMemoryId = memoryId;
            this.lastMessage = message;
            return Flux.just("测试回答");
        }
    }
}
