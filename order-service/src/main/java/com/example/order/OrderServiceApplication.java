package com.example.order;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    /**
     * Cấu hình tiêu đề hiển thị trên Swagger UI (http://localhost:8080/swagger-ui.html)
     * để khi chiếu màn hình demo khán giả nhìn thấy ngay đây là Order Service (Client).
     */
    @Bean
    public OpenAPI orderServiceOpenAPI(@Value("${server.port:8080}") String port) {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service (Port " + port + ") — Spring Cloud LoadBalancer Demo")
                        .version("1.0.0")
                        .description("Service Consumer gọi sang logical name 'http://inventory-service' "
                                + "và tự động cân bằng tải Round-Robin qua các port 8081, 8082, 8083."));
    }
}
