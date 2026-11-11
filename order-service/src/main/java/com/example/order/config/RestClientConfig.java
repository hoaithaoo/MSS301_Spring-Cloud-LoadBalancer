package com.example.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * [SLIDE 7: @LoadBalanced RestClient]
 * Class cấu hình công cụ gọi HTTP giữa các microservice (Order Service -> Inventory Service).
 */
@Configuration
public class RestClientConfig {

    /**
     * Tại sao phải gắn @LoadBalanced lên RestClient.Builder (chứ không phải RestClient)?
     *
     * 1. RestClient là HTTP Client mới của Spring Boot 3.2+ (thay thế dần RestTemplate).
     * 2. Khi ta gắn annotation @LoadBalanced lên bean RestClient.Builder:
     *    - Lúc ứng dụng khởi động, Spring Cloud (thông qua LoadBalancerRestClientBuilderBeanPostProcessor)
     *      sẽ phát hiện bean này và tự động gắn thêm một bộ chặn tên là "LoadBalancerInterceptor".
     * 3. Cơ chế hoạt động của LoadBalancerInterceptor:
     *    - Nếu KHÔNG có @LoadBalanced: gọi "http://inventory-service/..." sẽ bị lỗi
     *      UnknownHostException vì hệ điều hành không hiểu "inventory-service" là IP nào.
     *    - Khi CÓ @LoadBalanced: Interceptor sẽ chặn URL lại TRƯỚC KHI gửi ra mạng,
     *      lấy tên "inventory-service" -> tra danh sách (8081, 8082, 8083) trong application.yml
     *      -> chọn 1 port theo Round-Robin -> đổi thành "http://localhost:8082/..." rồi mới gọi thật.
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}
