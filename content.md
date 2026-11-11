SEMINAR OUTLINE – SPRING CLOUD LOADBALANCER
Topic: Spring Cloud LoadBalancer – Client-Side Load Balancing in Microservices
Duration: 15 minutes | Team: 5 members
Section 1: Introduction to Load Balancing
Member 1 – 2 minutes
Slide 1: Introduction & Problem Statement
Challenges in Microservices Communication
Single Instance Bottleneck
Why Do We Need Load Balancing?
Slide 2: Load Balancing Approaches
Server-Side Load Balancing
Client-Side Load Balancing
Key Differences Between Two Approaches
Section 2: Spring Cloud LoadBalancer Fundamentals
Member 2 – 3 minutes
Slide 3: Overview of Spring Cloud LoadBalancer
What is Spring Cloud LoadBalancer?
Key Features and Benefits
Service Discovery: Static vs Dynamic
Slide 4: How Spring Cloud LoadBalancer Works
Request Flow
ServiceInstanceListSupplier
LoadBalancer Instance Selection
Service-to-Service Communication
Slide 5: Load Balancing Algorithms
Round Robin (Default)
Random Selection
Round Robin Example
Section 3: System Architecture & Implementation
Member 3 – 3 minutes
Slide 6: Demo System Architecture
Order Service (Port 8080)
Inventory Service (Ports 8081, 8082, 8083)
Client-to-Service Request Flow
Static Service Instance Configuration
Slide 7: Spring Cloud LoadBalancer Implementation
Project Structure & Dependencies
Configuring Service Instances
@LoadBalanced RestClient
Calling Services by Logical Name
Section 4: Live Demonstration
Member 4 – 5 minutes
Slide 8: Round Robin Load Balancing Demo
Starting Multiple Service Instances
Sending Multiple HTTP Requests
Observing Request Distribution
Verifying Round Robin Behavior
Slide 9: Instance Failure & Recovery Demo
Simulating Instance Failure
Observing LoadBalancer Behavior
Health Check Mechanism
Instance Recovery and Redistribution
Section 5: Evaluation & Conclusion
Member 5 – 2 minutes
Slide 10: Evaluation & Conclusion
Advantages of Spring Cloud LoadBalancer
Limitations and Challenges
Real-World Applications
Key Takeaways
Future Improvements: Eureka, Retry, Circuit Breaker

