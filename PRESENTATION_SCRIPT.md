# Kịch bản Thuyết trình & Live Demo — Member 3 (3 phút)

**Section 3: System Architecture & Implementation**
- **Slide 6:** Demo System Architecture
- **Slide 7:** Spring Cloud LoadBalancer Implementation

---

## 1. Checklist chuẩn bị TRƯỚC khi lên thuyết trình (làm trước 2 phút)

1. Mở terminal tại `D:\SE_SEMESTER_8\MSS301\demo` và chạy lệnh khởi động 4 service:
   ```powershell
   .\scripts\start-demo.ps1
   ```
   > Lệnh này tự mở **4 cửa sổ PowerShell riêng biệt** có tiêu đề rõ ràng:
   > - `Inventory Service - Port 8081`
   > - `Inventory Service - Port 8082`
   > - `Inventory Service - Port 8083`
   > - `Order Service - Port 8080`
   > *(Mẹo: Nếu chỉ muốn chạy nền cho gọn màn hình và dùng 1 terminal duy nhất để demo, chạy `.\scripts\start-demo.ps1 -Background`)*

2. Mở sẵn trong IDE (VS Code / IntelliJ) các file để chuyển tab nhanh cho Slide 7:
   - `order-service/pom.xml`
   - `order-service/src/main/resources/application.yml`
   - `order-service/src/main/java/com/example/order/config/RestClientConfig.java`
   - `order-service/src/main/java/com/example/order/service/OrderService.java`

3. Mở sẵn trình duyệt ở **Swagger UI** của Order Service (hoặc thêm `-OpenSwagger` khi chạy `start-demo.ps1 -OpenSwagger`):
   - `http://localhost:8080/swagger-ui.html`
   *(Và tùy chọn mở thêm 1 tab terminal chạy sẵn `.\scripts\test-demo.ps1` nếu muốn show bảng tổng hợp).*

---

## 2. Lời thoại thuyết trình chi tiết (Tổng 3 phút = 180 giây)

### Phần A — Slide 6: Demo System Architecture (0:00 – 0:50 | ~50 giây)

> "Tiếp nối phần lý thuyết vừa rồi, mình xin đại diện nhóm trình bày **Section 3: Kiến trúc hệ thống demo và cách triển khai với Spring Cloud LoadBalancer**.
>
> Trên **Slide 6** là kiến trúc tổng quan của bài demo mà nhóm xây dựng, gồm 2 microservice:
> 1. **Order Service (Port 8080):** Đóng vai trò là **Client / Service Consumer**, tiếp nhận yêu cầu đặt hàng từ người dùng.
> 2. **Inventory Service (Ports 8081, 8082, 8083):** Đóng vai trò là **Service Provider**, cùng một mã nguồn nhưng được chạy thành **3 instance song song** trên 3 port `8081`, `8082` và `8083` để kiểm tra tồn kho.
>
> **Về luồng xử lý Request Flow từ Client tới Service:**
> - Đầu tiên, Client gửi HTTP request đặt hàng tới `Order Service` ở port `8080`.
> - Tiếp theo, `Order Service` cần gọi sang `Inventory Service` để kiểm tra tồn kho. Thay vì gọi cứng một địa chỉ cố định như `localhost:8081`, `Order Service` gọi qua **tên logic (logical name)** là `http://inventory-service`.
> - Lúc này, **Spring Cloud LoadBalancer** tích hợp sẵn trong `Order Service` sẽ chặn request, lấy danh sách 3 instance đã cấu hình và tự động phân phối tải lần lượt sang `8081`, `8082`, `8083` theo thuật toán **Round-Robin**.
>
> Về phần **Static Service Instance Configuration**: Trong bài demo này, để tập trung làm rõ cơ chế Client-Side Load Balancing của Spring Cloud LoadBalancer, nhóm dùng cấu hình danh sách instance tĩnh ngay trong file YAML (`SimpleDiscoveryClient`) thay vì phải dựng thêm Eureka Server."

---

### Phần B — Slide 7: Spring Cloud LoadBalancer Implementation (0:50 – 1:40 | ~50 giây)

> "Chuyển sang **Slide 7**, chúng ta cùng xem cách triển khai cụ thể trong code Spring Boot qua 4 bước chính:
>
> 1. **Project Structure & Dependencies:**
>    Project gồm 2 module là `inventory-service` và `order-service`. Trong file `pom.xml`, `inventory-service` chỉ cần `spring-boot-starter-web`, còn **`order-service` mới là nơi thêm dependency `spring-cloud-starter-loadbalancer`**. Điều này thể hiện đúng bản chất **Client-Side Load Balancing**: logic cân bằng tải nằm ở phía service đi gọi.
>
> 2. **Configuring Service Instances:**
>    Trong `order-service/src/main/resources/application.yml`, nhóm khai báo danh sách instance tĩnh dưới key `spring.cloud.discovery.client.simple.instances.inventory-service` trỏ tới 3 URI: `localhost:8081`, `localhost:8082`, và `localhost:8083`.
>
> 3. **`@LoadBalanced RestClient`:**
>    Trong class `RestClientConfig`, nhóm tạo một `@Bean RestClient.Builder` và gắn annotation **`@LoadBalanced`**. Annotation này báo cho Spring Cloud tự động gắn `LoadBalancerInterceptor` vào `RestClient`, giúp nó hiểu được tên dịch vụ logic thay vì hostname vật lý.
>
> 4. **Calling Services by Logical Name:**
>    Cuối cùng, trong `OrderService`, khi gọi API kiểm tra tồn kho, ta chỉ cần truyền URI là `http://inventory-service/api/inventory/check?...`. Không hề có port `8081`, `8082` hay `8083` bị hardcode trong Java code."

---

### Phần C — Live Demo (1:40 – 3:00 | ~80 giây)

**Bước 1: Chứng minh 4 service đang chạy & danh sách static instances (~20 giây)**
- Chỉ nhanh qua 4 cửa sổ đang chạy (`8080`, `8081`, `8082`, `8083`) hoặc chuyển sang terminal demo.
- Nói: *"Bây giờ mình sẽ demo trực tiếp. Hiện tại mình đã khởi chạy sẵn 3 instance Inventory Service ở port 8081, 8082, 8083 và Order Service ở port 8080."*

**Bước 2: Demo trực tiếp trên Swagger UI (~40 giây)**
- Mở tab trình duyệt đang bật **`http://localhost:8080/swagger-ui.html`** và lần lượt bấm **Try it out → Execute**:
  1. Bấm **`1. Xem danh sách 3 instance tĩnh`** (`/api/orders/instances`):
     *"Đầu tiên, khi gọi `/api/orders/instances`, Order Service đọc đúng 3 instance tĩnh `8081`, `8082`, `8083` của logical service `inventory-service`."*
  2. Bấm **`2. Đặt 1 đơn hàng`** (`/api/orders/place`) **3–4 lần liên tiếp** vào nút **Execute**:
     *"Mỗi lần mình bấm Execute gửi request tới cùng cổng `Order Service :8080`, mọi người chú ý trường `handledByInventoryPort` ở khung Response bên dưới: lần 1 vào `8081`, lần 2 nhảy sang `8082`, lần 3 sang `8083` — xoay vòng đều đặn theo Round-Robin!"*
  3. *(Tùy chọn)* Bấm **`4. Chạy hàng loạt 6 request`** (`/api/orders/demo?count=6`) hoặc chạy `.\scripts\test-demo.ps1` để show tổng hợp cả 6 request được chia đều 2-2-2 cho 3 port, rồi click lướt qua log của 3 cửa sổ Inventory `8081 / 8082 / 8083`.

**Bước 3: Chốt & chuyển tiếp (~20 giây)**
> *"Như vậy, chỉ với dependency `spring-cloud-starter-loadbalancer`, cấu hình instance tĩnh và `@LoadBalanced RestClient`, `Order Service` có thể gọi dịch vụ bằng tên logic và tự động cân bằng tải qua nhiều instance mà không cần thay đổi code khi tăng giảm số lượng instance. Phần trình bày của mình xin kết thúc tại đây, tiếp theo mời bạn Thành viên 4 trình bày phần tiếp theo ạ."*

---

## 3. Dọn dẹp sau khi demo xong

Chạy lệnh sau để tắt toàn bộ 4 tiến trình trên các port `8080–8083`:
```powershell
.\scripts\stop-demo.ps1
```

---

## 4. Câu hỏi phản biện thường gặp (Q&A Backup)

1. **Tại sao Request #1 không phải lúc nào cũng vào port `8081` mà có thể bắt đầu từ `8082` hoặc `8083` rồi mới xoay vòng?**
   - **Trả lời:** Trong Spring Cloud LoadBalancer, class `RoundRobinLoadBalancer` khởi tạo biến đếm `AtomicInteger position` bằng một giá trị ngẫu nhiên (`new Random().nextInt(1000)`) rồi mới cộng thêm `1` sau mỗi request. Thiết kế này giúp tránh việc nếu có nhiều client khởi động cùng lúc thì tất cả đều dồn request đầu tiên vào instance đầu tiên (`8081`). Sau request đầu tiên, thứ tự xoay vòng luôn đều đặn `1 -> 2 -> 3`.

2. **Tại sao phải gắn `@LoadBalanced` lên `RestClient.Builder` chứ không phải trực tiếp lên `RestClient`?**
   - **Trả lời:** Vì trong `spring-cloud-commons`, class `LoadBalancerRestClientBuilderBeanPostProcessor` kiểm tra các bean kiểu `RestClient.Builder` có annotation `@LoadBalanced` để chèn `LoadBalancerInterceptor` vào pipeline trước khi gọi `.build()` tạo `RestClient`.

3. **Nếu dùng `Static Service Instance Configuration` như demo thì có nhược điểm gì so với Service Discovery (Eureka/Consul)?**
   - **Trả lời:** Static configuration phù hợp cho demo hoặc môi trường cố định nhỏ, nhưng nếu muốn autoscale thêm instance `8084` hoặc có instance bị crash bất ngờ, ta phải sửa config hoặc restart client; còn khi kết hợp với Service Discovery (như Eureka), danh sách instance sẽ được cập nhật động hoàn toàn.

