package com.example.order.controller;

import com.example.order.model.OrderResponse;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [ĐIỂM VÀO CỦA CLIENT - Cổng 8080]
 * Người dùng (Swagger UI, Browser, Postman hoặc PowerShell) chỉ cần biết và gọi vào địa chỉ
 * duy nhất http://localhost:8080, hoàn toàn không cần biết phía sau có bao nhiêu Inventory Service.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Service (Port 8080)", description = "Các API demo Client-Side Load Balancing từ Order Service sang Inventory Service")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Endpoint đặt 1 đơn hàng bằng GET (tiện bấm Execute liên tiếp trên Swagger UI hoặc F5 trình duyệt).
     * Mỗi lần bấm Execute, trường "handledByInventoryPort" sẽ xoay vòng qua 8081, 8082, 8083.
     */
    @GetMapping("/place")
    @Operation(
            summary = "2. Đặt 1 đơn hàng (GET - Bấm Execute nhiều lần để xem Round-Robin)",
            description = "Order Service (8080) dùng @LoadBalanced RestClient gọi http://inventory-service/api/inventory/check. "
                    + "Xem trường handledByInventoryPort trong response nhảy lần lượt qua 8081, 8082, 8083."
    )
    public OrderResponse placeOrderViaGet(
            @RequestParam(defaultValue = "Laptop") String product,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        return orderService.placeOrder(product, quantity);
    }

    /**
     * Endpoint đặt hàng chuẩn REST POST (dùng nếu muốn test JSON body trên Swagger UI / Postman).
     */
    @PostMapping
    @Operation(
            summary = "3. Đặt 1 đơn hàng (POST JSON body)",
            description = "Tương tự /place nhưng nhận JSON body {\"product\": \"Laptop\", \"quantity\": 2}."
    )
    public OrderResponse placeOrderViaPost(@RequestBody OrderRequest request) {
        String product = request.product() != null ? request.product() : "Laptop";
        int quantity = request.quantity() > 0 ? request.quantity() : 1;
        return orderService.placeOrder(product, quantity);
    }

    /**
     * Endpoint chạy nhanh N request liên tiếp (mặc định 6 lần) chỉ trong 1 lần bấm Execute trên Swagger.
     * Giúp khán giả nhìn thấy ngay 6 đơn hàng được chia đều cho 3 port 8081, 8082, 8083 (mỗi port 2 đơn).
     */
    @GetMapping("/demo")
    @Operation(
            summary = "4. Chạy hàng loạt N request (mặc định 6 lần) để chứng minh phân tải đều",
            description = "Gọi placeOrder() liên tiếp N lần trong 1 cú click để thấy ngay Round-Robin chia đều cho 8081, 8082, 8083."
    )
    public List<OrderResponse> runDemoBatch(
            @RequestParam(defaultValue = "6") int count,
            @RequestParam(defaultValue = "Laptop") String product
    ) {
        int safeCount = Math.min(Math.max(count, 1), 20);
        List<OrderResponse> responses = new ArrayList<>();
        for (int i = 0; i < safeCount; i++) {
            responses.add(orderService.placeOrder(product, 1));
        }
        return responses;
    }

    /**
     * Endpoint kiểm tra danh sách static instance mà Order Service đã đọc được từ application.yml.
     * Dùng để chứng minh phần "Static Service Instance Configuration" trên Slide 6 & Slide 7.
     */
    @GetMapping("/instances")
    @Operation(
            summary = "1. Xem danh sách 3 instance tĩnh (Static Service Instance Configuration)",
            description = "Đọc từ SimpleDiscoveryClient (application.yml) để kiểm chứng Order Service đã biết đủ 8081, 8082, 8083."
    )
    public Map<String, Object> getInstances() {
        List<String> instances = orderService.getConfiguredInventoryInstances();
        return Map.of(
                "logicalServiceName", "inventory-service",
                "instanceCount", instances.size(),
                "instances", instances
        );
    }

    public record OrderRequest(String product, int quantity) {
    }
}
