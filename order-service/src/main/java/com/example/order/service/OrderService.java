package com.example.order.service;

import com.example.order.model.InventoryResponse;
import com.example.order.model.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [SLIDE 6 & SLIDE 7: Luồng xử lý chính của Client-Side Load Balancing]
 * OrderService chịu trách nhiệm xử lý đơn hàng và gọi sang Inventory Service
 * thông qua "tên logic" (Logical Service Name) thay vì địa chỉ IP/Port cố định.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // Tên logic phải TRÙNG KHỚP với key đã khai báo trong application.yml:
    // spring.cloud.discovery.client.simple.instances.inventory-service
    private static final String INVENTORY_SERVICE_NAME = "inventory-service";

    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;

    // Bộ đếm tự tăng để tạo mã đơn hàng đẹp khi demo: ORD-1001, ORD-1002, ...
    private final AtomicInteger orderSequence = new AtomicInteger(1000);

    @Value("${server.port:8080}")
    private String orderServicePort;

    /**
     * [BƯỚC KHỞI TẠO]
     * - @LoadBalanced RestClient.Builder: Lấy đúng Builder đã được Spring Cloud gắn LoadBalancerInterceptor
     *   từ class RestClientConfig, rồi .build() thành RestClient dùng cho các lần gọi HTTP sau này.
     * - DiscoveryClient: Công cụ của Spring Cloud giúp đọc danh sách các instance đã cấu hình
     *   (ở đây là SimpleDiscoveryClient đọc 3 địa chỉ 8081, 8082, 8083 từ application.yml).
     */
    public OrderService(
            @LoadBalanced RestClient.Builder restClientBuilder,
            DiscoveryClient discoveryClient
    ) {
        this.restClient = restClientBuilder.build();
        this.discoveryClient = discoveryClient;
    }

    /**
     * LUỒNG ĐI KHI CÓ 1 REQUEST ĐẶT HÀNG (Client-to-Service Request Flow):
     *
     * [BƯỚC 1] OrderController (port 8080) nhận request từ người dùng và gọi vào hàm placeOrder() này.
     * [BƯỚC 2] Ta tạo URL chứa TÊN LOGIC: "http://inventory-service/api/inventory/check"
     *          -> Không hề hardcode localhost:8081, 8082 hay 8083 trong code Java!
     * [BƯỚC 3] Khi gọi restClient.get()...retrieve(), Spring Cloud LoadBalancerInterceptor chặn request lại:
     *          a) Tách lấy tên service: "inventory-service".
     *          b) Hỏi danh sách instance đang có: [localhost:8081, localhost:8082, localhost:8083].
     *          c) Dùng thuật toán mặc định ROUND-ROBIN chọn 1 instance cho lượt gọi này
     *             (xoay vòng lần lượt từng port để chia đều tải cho cả 3 server).
     *             * Mẹo giải thích Q&A: Request đầu tiên có thể vào 8082 hoặc 8083 thay vì 8081
     *               vì RoundRobinLoadBalancer khởi tạo vị trí bắt đầu bằng số ngẫu nhiên (random seed),
     *               nhưng từ request thứ 2 trở đi luôn xoay vòng đều 1 -> 2 -> 3!
     *          d) Viết lại (rewrite) URL từ "http://inventory-service/..." thành "http://localhost:808x/..."
     *             rồi mới gửi HTTP request thật sang Inventory Service.
     * [BƯỚC 4] Inventory Service ở port được chọn xử lý xong và trả JSON (kèm chính port của nó).
     * [BƯỚC 5] Order Service đóng gói kết quả vào OrderResponse để người dùng thấy rõ request vừa đi qua port nào.
     */
    public OrderResponse placeOrder(String product, int quantity) {
        // [BƯỚC 2] Gọi dịch vụ bằng tên logic (Calling Services by Logical Name)
        String logicalUri = "http://" + INVENTORY_SERVICE_NAME + "/api/inventory/check";

        // [BƯỚC 3 & 4] RestClient đã có @LoadBalanced sẽ tự phân giải logicalUri -> 8081 / 8082 / 8083
        InventoryResponse inventory = restClient.get()
                .uri(logicalUri + "?product={product}&quantity={quantity}", product, quantity)
                .retrieve()
                .body(InventoryResponse.class);

        if (inventory == null) {
            throw new IllegalStateException("Empty response from inventory-service");
        }

        // [BƯỚC 5] Tạo kết quả trả về cho Client, ghi rõ port Inventory nào vừa xử lý để dễ quan sát lúc demo
        String orderId = "ORD-" + orderSequence.incrementAndGet();
        String status = inventory.inStock() ? "CREATED" : "OUT_OF_STOCK";

        log.info("[Order Service - Port {}] {} -> Called '{}' -> Routed to {} (Port {})",
                orderServicePort, orderId, logicalUri, inventory.instanceId(), inventory.port());

        return new OrderResponse(
                orderId,
                product,
                quantity,
                status,
                orderServicePort,
                logicalUri,
                inventory.port(),
                inventory.instanceId(),
                inventory.availableStock()
        );
    }

    /**
     * Hàm phụ trợ cho endpoint /api/orders/instances:
     * Dùng DiscoveryClient đọc trực tiếp danh sách instance của "inventory-service"
     * để chứng minh khi demo rằng Order Service đã nạp đúng 3 địa chỉ tĩnh từ application.yml.
     */
    public List<String> getConfiguredInventoryInstances() {
        return discoveryClient.getInstances(INVENTORY_SERVICE_NAME)
                .stream()
                .map(ServiceInstance::getUri)
                .map(Object::toString)
                .toList();
    }
}
