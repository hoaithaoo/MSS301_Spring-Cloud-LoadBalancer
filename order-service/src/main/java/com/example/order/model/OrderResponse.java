package com.example.order.model;

/**
 * Dữ liệu Order Service trả về cho người dùng sau khi đặt hàng xong.
 * Được thiết kế thêm các trường thông tin định tuyến (orderServicePort, calledLogicalUri,
 * handledByInventoryPort, inventoryInstanceId) để khi demo nhìn vào JSON là hiểu ngay
 * request đi từ port 8080 qua tên logic "inventory-service" và đáp xuống port nào (8081/8082/8083).
 */
public record OrderResponse(
        String orderId,
        String product,
        int quantity,
        String status,
        String orderServicePort,
        String calledLogicalUri,
        String handledByInventoryPort,
        String inventoryInstanceId,
        int availableStock
) {
}
