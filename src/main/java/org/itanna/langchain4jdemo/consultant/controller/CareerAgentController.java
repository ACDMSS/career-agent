package org.itanna.langchain4jdemo.consultant.controller;

import org.itanna.langchain4jdemo.consultant.aiService.CareerAgentService;
import org.itanna.langchain4jdemo.consultant.dto.ChatRequest;
import org.itanna.langchain4jdemo.consultant.dto.StudentProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

/**
 * 单页前端的唯一接口：把学生档案 + 用户问题交给成长规划 Agent，流式返回中文回答。
 */
@RestController
@RequestMapping("/api")
public class CareerAgentController {

    //造一台日志打印机，贴上"CareerAgentController"这个标签，整个类共用这一台，存进 log 变量，以后打日志就用它。
    private static final Logger log = LoggerFactory.getLogger(CareerAgentController.class);

    private static final String PROFILE_EMPTY_HINT = """
            开始规划前请先在页面顶部补充两项信息：
            1. 目标岗位（例如 Java 后端开发、AI 应用开发）；
            2. 已掌握技能（没有可以填“暂无”）。
            填写后我会先检索本地岗位资料，再给出能力差距和分阶段成长路线。""";

    private final CareerAgentService careerAgentService;

    /**
     * 构造器注入，便于单元测试替换 Service 实现。
     *
     * @param careerAgentService 成长规划 Agent
     */
    public CareerAgentController(CareerAgentService careerAgentService) {
        this.careerAgentService = careerAgentService;
    }

    /**
     * 对话接口：校验参数，把档案整理成文本，流式返回中文回答。
     *
     * @param request 会话标识、用户问题、学生档案
     * @return 流式纯文本；档案缺失时直接返回补充提示
     */
    @PostMapping(value = "/chat", produces = "text/plain;charset=UTF-8")
    public Flux<String> chat(@RequestBody ChatRequest request) {
        String memoryId = request == null ? "" : trimToEmpty(request.memoryId());
        String message = request == null ? "" : trimToEmpty(request.message());

        if (memoryId.isBlank()) {
            throw new IllegalArgumentException("memoryId 不能为空，请刷新页面后重试。");
        }
        if (message.isBlank()) {
            throw new IllegalArgumentException("message 不能为空，请输入你想咨询的问题。");
        }

        StudentProfile profile = request.profile();
        if (profile == null || isBlank(profile.targetRole())) {
            return Flux.just(PROFILE_EMPTY_HINT);
        }
        if (profile.weeklyHours() != null && profile.weeklyHours() < 0) {
            throw new IllegalArgumentException("每周可投入时间不能为负数。");
        }

        String prompt = buildPrompt(profile, message);
        return careerAgentService.chat(memoryId, prompt)
                .onErrorResume(error -> Flux.just(friendlyError(error)));
    }

    /**
     * 把学生档案整理成固定文本；“目标岗位：”“已掌握技能：”两个前缀同时被检索器识别。
     */
    private String buildPrompt(StudentProfile profile, String message) {
        return """
                【学生档案】
                专业：%s
                年级：%s
                目标岗位：%s
                已掌握技能：%s
                每周可投入时间：%s
                发展方向：%s

                【学生问题】
                %s""".formatted(
                orDefault(profile.major(), "未填写"),
                orDefault(profile.grade(), "未填写"),
                profile.targetRole().trim(),
                formatSkills(profile.knownSkills()),
                formatWeeklyHours(profile.weeklyHours()),
                orDefault(profile.careerPath(), "未填写"),
                message);
    }

    /**
     * 技能列表拼成“A、B”文本，空列表返回“未填写”。
     *
     * @param knownSkills 已掌握技能
     * @return 用于提示词的技能描述
     */
    private String formatSkills(List<String> knownSkills) {
        if (knownSkills == null) {
            return "未填写";
        }
        String joined = knownSkills.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .reduce((left, right) -> left + "、" + right)
                .orElse("");
        return joined.isEmpty() ? "未填写" : joined;
    }

    /**
     * 每周时间格式化，未填写时返回“未填写”。
     *
     * @param weeklyHours 每周可投入小时数
     * @return 用于提示词的描述
     */
    private String formatWeeklyHours(Integer weeklyHours) {
        return weeklyHours == null ? "未填写" : weeklyHours + " 小时/周";
    }

    /**
     * 流式输出中途出错时无法再改 HTTP 状态码，这里转成中文提示；只暴露异常类型，不含 Key。
     */
    private String friendlyError(Throwable error) {
        log.warn("调用模型失败：{}", error.getClass().getName());
        return """

                [调用模型失败] 抱歉，暂时无法生成回答（%s）。
                常见原因：未设置 OPENAI_API_KEY、Key 无效或已过期、网络无法访问模型服务、模型名称不可用。
                请检查配置后重新发送；如果问题持续存在，可先确认应用日志。""".formatted(error.getClass().getSimpleName());
    }

    /** 值为空白时返回默认值，否则返回去空格后的值。 */
    private String orDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    /** 判断字符串是否为 null 或空白。 */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** null 安全的 trim，null 转成空字符串。 */
    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
