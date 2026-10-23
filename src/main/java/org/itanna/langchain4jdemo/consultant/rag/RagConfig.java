package org.itanna.langchain4jdemo.consultant.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 基础 RAG：启动时用本地中文 embedding 把岗位资料写入内存向量库，请求时按语义取前 5 份。
 */
@Configuration
public class RagConfig {

    private static final String NO_DOCUMENT_HINT = """
            【岗位资料检索结果】
            本地岗位资料库为空：src/main/resources/content/jobs 下没有可用的 .txt 文件。
            请明确告知学生：当前岗位资料不足，不能给出带来源的能力要求和成长路线，也不得编造岗位信息。""";

    /**
     * 构建向量检索器（容器里唯一的 ContentRetriever）。
     *
     * @param loader 启动时已加载的岗位资料
     * @return 语义检索器；资料库为空时返回“资料不足”提示
     */
    @Bean
    public ContentRetriever contentRetriever(JobDocumentLoader loader) {
        if (loader.documents().isEmpty()) {
            return query -> List.of(Content.from(NO_DOCUMENT_HINT));
        }

        EmbeddingModel model = new BgeSmallZhV15QuantizedEmbeddingModel();
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        // 每段资料单独入库（每段都带着来源头）
        for (JobDocument document : loader.documents()) {
            String text = document.toPromptText();
            store.add(model.embed(text).content(), TextSegment.from(text));
        }

        ContentRetriever vectorRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(model)
                .maxResults(5)
                .build();

        // 没有达到阈值的结果时，同样返回“资料不足”提示
        return query -> {
            List<Content> contents = vectorRetriever.retrieve(query);
            return contents.isEmpty() ? List.of(Content.from(NO_DOCUMENT_HINT)) : contents;
        };
    }
}
