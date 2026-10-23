package org.itanna.langchain4jdemo.consultant.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 启动时读取 content/jobs 下的 txt，按空行分段，每段一份 {@link JobDocument}。
 * 一个文件可以放多个岗位，用一行 ==== 分隔；每个岗位的第一段是它自己的公共头。
 */
@Component
public class JobDocumentLoader {

    private static final Logger log = LoggerFactory.getLogger(JobDocumentLoader.class);
    private static final String RESOURCE_PATTERN = "classpath*:content/jobs/**/*.txt";

    private final List<JobDocument> documents;

    /** 构造时读取并缓存，之后只读。 */
    public JobDocumentLoader() {
        this.documents = load();
    }

    /** @return 已加载的岗位资料（每段一份） */
    public List<JobDocument> documents() {
        return documents;
    }

    private List<JobDocument> load() {
        List<JobDocument> documents = new ArrayList<>();
        try {
            for (Resource resource : new PathMatchingResourcePatternResolver().getResources(RESOURCE_PATTERN)) {
                try {
                    String text = resource.getContentAsString(StandardCharsets.UTF_8)
                            .replace("\uFEFF", "")
                            .replace("\r\n", "\n")
                            .trim();
                    documents.addAll(parse(resource.getFilename(), text));
                } catch (IOException error) {
                    log.warn("跳过无法读取的岗位资料：{}", resource.getFilename());
                }
            }
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
        log.info("已加载 {} 段岗位资料", documents.size());
        return List.copyOf(documents);
    }

    /** 按 ==== 分成多个岗位；每个岗位第一段作公共头，其余按空行分段。 */
    List<JobDocument> parse(String fileName, String text) {
        List<JobDocument> documents = new ArrayList<>();
        for (String section : text.split("(?m)^={4,}\\s*$")) {
            String block = section.trim();
            if (block.isEmpty()) {
                continue;
            }
            String[] paragraphs = block.split("\\n\\s*\\n");
            if (paragraphs.length == 1) {
                documents.add(new JobDocument(fileName, block));
                continue;
            }
            for (int i = 1; i < paragraphs.length; i++) {
                documents.add(new JobDocument(fileName,
                        paragraphs[0].trim() + "\n\n" + paragraphs[i].trim()));
            }
        }
        return documents;
    }
}
