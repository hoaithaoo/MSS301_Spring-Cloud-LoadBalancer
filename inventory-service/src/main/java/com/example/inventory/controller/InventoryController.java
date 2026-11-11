package com.example.inventory.controller;

import com.example.inventory.model.InventoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * [SERVICE PROVIDER - Chạy ở 3 instance độc lập: Port 8081, 8082, 8083]
 * Cả 3 instance đều chạy chung đoạn code này, chỉ khác nhau giá trị "server.port" lúc khởi động.
 * Khi LoadBalancer ở Order Service chọn port nào, request sẽ đi thẳng vào Controller của port đó.
 */
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory Service", description = "API kiểm tra tồn kho chạy trên các instance 8081, 8082, 8083")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    // Dữ liệu tồn kho giả lập trong bộ nhớ (In-memory) để demo tập trung vào Load Balancing, không cần DB
    private static final Map<String, Integer> STOCK = Map.of(
            "laptop", 50,
            "phone", 100,
            "keyboard", 30,
            "mouse", 75
    );

    @Value("${spring.application.name:inventory-service}")
    private String serviceName;

    // Lấy chính xác port mà instance hiện tại đang chạy (8081, 8082 hoặc 8083)
    // để trả về trong response & in ra log chứng minh instance nào đã xử lý request.
    @Value("${server.port:8081}")
    private String serverPort;

    /**
     * API được Order Service gọi thông qua URL logic:
     * http://inventory-service/api/inventory/check?product=...&quantity=...
     */
    @GetMapping("/check")
    @Operation(
            summary = "Kiểm tra tồn kho sản phẩm",
            description = "Nhận product & quantity, trả về trạng thái còn hàng kèm port/instanceId của instance đang xử lý."
    )
    public InventoryResponse checkStock(
            @RequestParam(defaultValue = "Laptop") String product,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        int available = STOCK.getOrDefault(product.toLowerCase(), 25);
        boolean inStock = quantity > 0 && available >= quantity;
        String instanceId = serviceName + ":" + serverPort;

        // In log ở cửa sổ terminal của từng port (8081 / 8082 / 8083) để đối chiếu trực tiếp lúc demo
        log.info("[Inventory Service - Port {}] Handling stock check -> product={}, quantity={}, inStock={}",
                serverPort, product, quantity, inStock);

        return new InventoryResponse(
                product,
                quantity,
                available,
                inStock,
                serviceName,
                serverPort,
                instanceId
        );
    }

    @GetMapping("/info")
    @Operation(
            summary = "Xem thông tin port & trạng thái của instance hiện tại",
            description = "Kiểm tra nhanh instance Inventory đang chạy ở port nào."
    )
    public Map<String, String> info() {
        log.info("[Inventory Service - Port {}] Health/Info check", serverPort);
        return Map.of(
                "service", serviceName,
                "port", serverPort,
                "status", "UP"
        );
    }
}
