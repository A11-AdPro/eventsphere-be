# Advanced Programming Group Project 2024/2025 - A11

## Payment and Balance Management Architecture Overview

#### Controllers Layer
- TopUpController: Handles REST API endpoints for top-up operations and balance queries
- TransactionController: Manages endpoints for ticket purchases and transaction history

### Service Layer
- Implements business logic with interfaces and implementations (following good design practices)
- TopUpService/Impl: Manages wallet top-up operations
- TransactionService/Impl: Handles ticket purchases and transaction management

### Design Patterns Used
1. Strategy Pattern:
    - TopUpStrategy interface with StandardTopUpStrategy implementation
    - Allows for flexible top-up behavior that can be changed at runtime

2. Factory Pattern:
    - TopUpFactory creates different types of top-ups (Fixed or Custom)
    - Encapsulates creation logic and provides preset top-up amounts

3. DTO Pattern:
    - Clean separation of data transfer objects from domain models
    - Examples: TopUpRequestDTO, TopUpResponseDTO, TransactionDTO

### Component Diagram Payment and Balance Management
![alt text](/diagrams/image.png)

![alt text](/diagrams/image-1.png)

![alt text](/diagrams/image-2.png)

![alt text](/diagrams/image-3.png)

### Code Diagram Diagram Payment and Balance Management
![alt text](/diagrams/image-4.png)
