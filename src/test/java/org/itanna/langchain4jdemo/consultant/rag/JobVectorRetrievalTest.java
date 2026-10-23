package org.itanna.langchain4jdemo.consultant.rag;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobVectorRetrievalTest {

    /** 向量检索应把目标岗位对应方向的资料排在最前。 */
    @Test
    void shouldRankDocumentsBySemanticSimilarity() {
        ContentRetriever retriever = new RagConfig().contentRetriever(new JobDocumentLoader());

        List<Content> javaResult = retriever.retrieve(Query.from(
                "目标岗位：Java后端开发\n已掌握技能：Java、Spring Boot"));
        List<Content> aiResult = retriever.retrieve(Query.from(
                "目标岗位：AI应用开发\n已掌握技能：Java、Spring Boot"));

        assertThat(javaResult).isNotEmpty();
        assertThat(javaResult).hasSizeLessThanOrEqualTo(5);
        assertThat(javaResult.get(0).textSegment().text()).contains("java-backend");

        assertThat(aiResult).isNotEmpty();
        assertThat(aiResult.get(0).textSegment().text()).contains("ai-application");
    }
}
