param(
    [int]$Count = 6,
    [string]$Product = "Laptop",
    [int]$Quantity = 1
)

$ErrorActionPreference = "Stop"

Write-Host "=== 1. Configured Static Service Instances (SimpleDiscoveryClient) ===" -ForegroundColor Cyan
$instances = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/instances"
$instances | ConvertTo-Json -Depth 4
Write-Host ""

Write-Host "=== 2. Sending $Count Sequential Requests to Order Service (Port 8080) ===" -ForegroundColor Cyan
Write-Host "Target URL: http://localhost:8080/api/orders/place?product=$Product&quantity=$Quantity" -ForegroundColor DarkGray
Write-Host "Order Service calls logical service: http://inventory-service/api/inventory/check" -ForegroundColor DarkGray
Write-Host ""

$results = @()
for ($i = 1; $i -le $Count; $i++) {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/place?product=$Product&quantity=$Quantity"
    $results += [PSCustomObject]@{
        Request      = "#$i"
        OrderId      = $res.orderId
        OrderPort    = $res.orderServicePort
        LogicalCall  = "inventory-service"
        RoutedToPort = $res.handledByInventoryPort
        InstanceId   = $res.inventoryInstanceId
        Status       = $res.status
    }
}

$results | Format-Table -AutoSize

Write-Host "=== 3. Load Distribution Summary ===" -ForegroundColor Green
$results | Group-Object RoutedToPort | Sort-Object Name | ForEach-Object {
    Write-Host ("  Inventory Port {0} handled {1} request(s)" -f $_.Name, $_.Count) -ForegroundColor White
}

