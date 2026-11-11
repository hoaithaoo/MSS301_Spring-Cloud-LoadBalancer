package com.example.inventory;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    /**
     * Cấu hình tiêu đề Swagger UI riêng cho từng instance (8081, 8082, 8083)
     * để khi mở http://localhost:8081/swagger-ui.html (hoặc 8082, 8083)
     * sẽ thấy rõ port của từng instance đang chạy độc lập.
     */
    @Bean
    public OpenAPI inventoryServiceOpenAPI(@Value("${server.port:8081}") String port) {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service — Instance Port " + port)
                        .version("1.0.0")
                        .description("Service Provider chạy tại port " + port
                                + ", tiếp nhận request từ Order Service thông qua Spring Cloud LoadBalancer."));
    }
}
