package org.itanna.langchain4jdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口：启动 Web 服务，加载岗位资料与成长规划 Agent。
 */
@SpringBootApplication
public class CareerAgentApplication {

    /**
     * 启动 Spring Boot 应用，默认监听 8080 端口。
     *
     * @param args 启动参数，一般不需要传
     */
    public static void main(String[] args) {
        SpringApplication.run(CareerAgentApplication.class, args);
    }
}
