# Spring Cloud LoadBalancer Demo (`order-service` + `inventory-service`)

Demo project for **Section 3: System Architecture & Implementation** (Slide 6 & Slide 7).

## Architecture

```
Client / Browser / PowerShell
          │
          ▼  GET /api/orders/place?product=Laptop&quantity=1
┌───────────────────────────────┐
│   Order Service (Port 8080)   │
│  - @LoadBalanced RestClient   │
│  - SimpleDiscoveryClient      │
│    (Static instances config)  │
└──────────────┬────────────────┘
               │  http://inventory-service/api/inventory/check
               │  (Spring Cloud LoadBalancer - Round-Robin)
     ┌─────────┼─────────┐
     ▼         ▼         ▼
┌─────────┐ ┌─────────┐ ┌─────────┐
│ Port    │ │ Port    │ │ Port    │
│ 8081    │ │ 8082    │ │ 8083    │
└─────────┘ └─────────┘ └─────────┘
        Inventory Service (3 Instances)
```

## Project Structure

- `pom.xml`: Parent POM (Spring Boot 3.3.4, Spring Cloud 2023.0.3, Java 21)
- `inventory-service/`: Provider microservice (`spring-boot-starter-web`), run as 3 instances on ports `8081`, `8082`, `8083`
- `order-service/`: Consumer microservice (`spring-boot-starter-web` + `spring-cloud-starter-loadbalancer`) on port `8080`
- `scripts/`:
  - `start-demo.ps1`: Builds (if needed) and starts all 4 services
  - `test-demo.ps1`: Verifies configured instances & sends 6 sequential requests showing Round-Robin distribution
  - `stop-demo.ps1`: Stops all services on ports `8080–8083`
- `PRESENTATION_SCRIPT.md`: Complete 3-minute presentation script & live demo checklist in Vietnamese

## Quick Start

```powershell
# 1. Start 3 Inventory instances (8081, 8082, 8083) + 1 Order Service (8080) in separate windows
.\scripts\start-demo.ps1

# 2. Run live load-balancing verification (6 sequential requests)
.\scripts\test-demo.ps1

# 3. Stop all demo services
.\scripts\stop-demo.ps1
```

## Useful Endpoints & Swagger UI

- **Order Service Swagger UI (Chính để demo):** http://localhost:8080/swagger-ui.html
- **Inventory Service Swagger UI:**
  - Instance 1: http://localhost:8081/swagger-ui.html
  - Instance 2: http://localhost:8082/swagger-ui.html
  - Instance 3: http://localhost:8083/swagger-ui.html
- `GET http://localhost:8080/api/orders/instances` — Show static instances loaded by `SimpleDiscoveryClient`
- `GET http://localhost:8080/api/orders/place?product=Laptop&quantity=1` — Place 1 order via `@LoadBalanced RestClient`
- `POST http://localhost:8080/api/orders` — Place 1 order with JSON body `{"product": "Laptop", "quantity": 2}`
- `GET http://localhost:8080/api/orders/demo?count=6&product=Laptop` — Execute 6 orders in one HTTP call

