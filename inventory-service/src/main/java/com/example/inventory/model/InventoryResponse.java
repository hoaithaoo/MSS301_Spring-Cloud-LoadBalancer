package com.example.inventory.model;

public record InventoryResponse(
        String product,
        int requestedQuantity,
        int availableStock,
        boolean inStock,
        String serviceName,
        String port,
        String instanceId
) {
}

