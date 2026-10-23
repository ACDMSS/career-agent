package org.itanna.langchain4jdemo.consultant.rag;

/**
 * 一份岗位资料：文件名 + 一段文本。
 *
 * @param fileName 文件名，用于区分资料
 * @param text     资料文本（正文段，前面带着文件开头的来源信息）
 */
public record JobDocument(String fileName, String text) {

    /** 交给模型的文本：文件名 + 原文。 */
    public String toPromptText() {
        return "【岗位资料】\n文件：" + fileName + "\n\n" + text;
    }
}
