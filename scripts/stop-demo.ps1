$ErrorActionPreference = "SilentlyContinue"
$rootDir = Split-Path -Parent $PSScriptRoot
$pidFile = Join-Path $rootDir "logs\demo-pids.txt"

$stopped = 0

if (Test-Path $pidFile) {
    $savedPids = Get-Content $pidFile | Where-Object { $_ -match '^\d+$' }
    foreach ($procId in $savedPids) {
        $p = Get-Process -Id ([int]$procId) -ErrorAction SilentlyContinue
        if ($p) {
            Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue
            $stopped++
        }
    }
    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
}

$conns = Get-NetTCPConnection -LocalPort 8080,8081,8082,8083 -State Listen -ErrorAction SilentlyContinue
if ($conns) {
    $ownerIds = $conns | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($procId in $ownerIds) {
        if ($procId -gt 0) {
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
            $stopped++
        }
    }
}

# Dam bao dung moi tien trinh java dang giu file jar cua project demo
$demoJavaProcs = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" |
    Where-Object { $_.CommandLine -like "*inventory-service-1.0.0.jar*" -or $_.CommandLine -like "*order-service-1.0.0.jar*" }
foreach ($jp in $demoJavaProcs) {
    Stop-Process -Id $jp.ProcessId -Force -ErrorAction SilentlyContinue
    $stopped++
}

Write-Host "Stopped demo processes on ports 8080-8083 (count: $stopped)." -ForegroundColor Yellow
