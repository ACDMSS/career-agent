package org.itanna.langchain4jdemo.consultant.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobDocumentLoaderTest {

    @Test
    void shouldSplitFilesIntoParagraphsWithHeader() {
        List<JobDocument> documents = new JobDocumentLoader().documents();

        assertThat(documents).isNotEmpty();
        assertThat(documents).hasSizeGreaterThan(4);
        assertThat(documents).allSatisfy(document -> {
            assertThat(document.fileName()).isNotBlank();
            assertThat(document.text()).isNotBlank();
        });
        assertThat(documents).anySatisfy(document ->
                assertThat(document.text()).contains("岗位职责").contains("教学示例"));
        assertThat(documents).anySatisfy(document ->
                assertThat(document.text()).contains("任职要求"));
        assertThat(documents).anySatisfy(document ->
                assertThat(document.text()).contains("java-backend"));
        assertThat(documents).anySatisfy(document ->
                assertThat(document.text()).contains("ai-application"));
    }

    /** 一个文件里可以用 ==== 分隔多个岗位，每个岗位各自带公共头。 */
    @Test
    void shouldSplitMultipleJobsInOneFile() {
        List<JobDocument> documents = new JobDocumentLoader().parse("multi.txt", """
                岗位名称：岗位A
                来源：来源A

                职责A

                ====

                岗位名称：岗位B
                来源：来源B

                职责B""");

        assertThat(documents).hasSize(2);
        assertThat(documents.get(0).text())
                .contains("岗位名称：岗位A").contains("职责A").doesNotContain("岗位B");
        assertThat(documents.get(1).text())
                .contains("岗位名称：岗位B").contains("职责B");
    }
}
