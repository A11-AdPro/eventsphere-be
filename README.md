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
## [Muhammad Almerazka Yocendra](https://github.com/almerazka) - 2306241745

### Laporan dan Pengajuan Bantuan (🙋 / 💻 / 🕺) 

#### Component Diagram
![image](https://github.com/user-attachments/assets/50fb23b0-4680-46d2-be7e-e1a64ae7b5cb)

#### Code Diagram

![image](https://github.com/user-attachments/assets/56871382-1702-428b-b473-e723301d0b57)

![image](https://github.com/user-attachments/assets/0c77adfe-69c4-451d-8efd-a9a8a7525424)

![image](https://github.com/user-attachments/assets/c647ce1b-c3fe-4c54-8596-5db0ef37a48d)

![image](https://github.com/user-attachments/assets/905de7d4-b17f-466d-9ef1-e16e94d84b7e)

![image](https://github.com/user-attachments/assets/16ffaaea-80d4-411b-9e4d-45ef415a1c72)

![image](https://github.com/user-attachments/assets/b8a0a822-dfe0-48f0-90cb-8a55e58a458c)



