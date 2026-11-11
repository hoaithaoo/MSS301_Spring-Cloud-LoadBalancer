param(
    [switch]$Rebuild,
    [switch]$Background,
    [switch]$OpenSwagger
)

$ErrorActionPreference = "Stop"
$rootDir = Split-Path -Parent $PSScriptRoot
$invJar = Join-Path $rootDir "inventory-service\target\inventory-service-1.0.0.jar"
$ordJar = Join-Path $rootDir "order-service\target\order-service-1.0.0.jar"

# Dung cac service cu TRUOC khi rebuild de tranh loi Windows khoa file .jar
$existing = Get-NetTCPConnection -LocalPort 8080,8081,8082,8083 -State Listen -ErrorAction SilentlyContinue
if ($existing -or $Rebuild) {
    & (Join-Path $PSScriptRoot "stop-demo.ps1")
    Start-Sleep -Milliseconds 800
}

if ($Rebuild -or -not (Test-Path $invJar) -or -not (Test-Path $ordJar)) {
    Write-Host "[1/3] Building Maven modules..." -ForegroundColor Cyan
    Push-Location $rootDir
    try {
        mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            throw "Maven build failed!"
        }
    }
    finally {
        Pop-Location
    }
}
else {
    Write-Host "[1/3] Using existing JAR builds (pass -Rebuild to recompile)." -ForegroundColor DarkGray
}

Write-Host "[2/3] Starting Inventory Service (8081, 8082, 8083) & Order Service (8080)..." -ForegroundColor Cyan

$logDir = Join-Path $rootDir "logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$pidFile = Join-Path $logDir "demo-pids.txt"
$pids = @()

foreach ($port in 8081, 8082, 8083) {
    if ($Background) {
        $proc = Start-Process -FilePath "java" `
            -ArgumentList "-jar", $invJar, "--server.port=$port" `
            -RedirectStandardOutput (Join-Path $logDir "inventory-$port.log") `
            -RedirectStandardError (Join-Path $logDir "inventory-$port.err.log") `
            -PassThru -WindowStyle Hidden
    }
    else {
        $cmd = "`$Host.UI.RawUI.WindowTitle = 'Inventory Service - Port $port'; Write-Host 'Starting Inventory Service on port $port...' -ForegroundColor Green; java -jar `"$invJar`" --server.port=$port"
        $proc = Start-Process -FilePath "powershell" `
            -ArgumentList "-NoExit", "-Command", $cmd `
            -PassThru
    }
    $pids += $proc.Id
}

if ($Background) {
    $procOrd = Start-Process -FilePath "java" `
        -ArgumentList "-jar", $ordJar `
        -RedirectStandardOutput (Join-Path $logDir "order-8080.log") `
        -RedirectStandardError (Join-Path $logDir "order-8080.err.log") `
        -PassThru -WindowStyle Hidden
}
else {
    $cmdOrd = "`$Host.UI.RawUI.WindowTitle = 'Order Service - Port 8080'; Write-Host 'Starting Order Service on port 8080...' -ForegroundColor Cyan; java -jar `"$ordJar`""
    $procOrd = Start-Process -FilePath "powershell" `
        -ArgumentList "-NoExit", "-Command", $cmdOrd `
        -PassThru
}
$pids += $procOrd.Id
$pids | Set-Content -Path $pidFile

Write-Host "[3/3] Waiting for all 4 services to become ready..." -ForegroundColor Cyan
$deadline = (Get-Date).AddSeconds(30)
$ready = $false
while ((Get-Date) -lt $deadline) {
    $listeningPorts = (Get-NetTCPConnection -LocalPort 8080,8081,8082,8083 -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty LocalPort -Unique)
    if ($listeningPorts.Count -eq 4) {
        $ready = $true
        break
    }
    Start-Sleep -Milliseconds 500
}

if (-not $ready) {
    Write-Host "Timed out waiting for all 4 ports (8080, 8081, 8082, 8083)." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "ALL SERVICES ARE UP!" -ForegroundColor Green
Write-Host "  Order Service Swagger UI : http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  Inventory 8081 Swagger   : http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host "  Inventory 8082 Swagger   : http://localhost:8082/swagger-ui.html" -ForegroundColor White
Write-Host "  Inventory 8083 Swagger   : http://localhost:8083/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "Next steps for demo:" -ForegroundColor Yellow
Write-Host "  Open http://localhost:8080/swagger-ui.html in browser (or pass -OpenSwagger)"
Write-Host "  .\scripts\test-demo.ps1    -> Run 6 load-balanced requests & show distribution"
Write-Host "  .\scripts\stop-demo.ps1    -> Stop all 4 services"

if ($OpenSwagger) {
    Start-Process "http://localhost:8080/swagger-ui.html"
}
