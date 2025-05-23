# Advanced Programming Group Project 2024/2025 - A11

## Event Management Architecture Overview

#### Controller Layer
- EventController.java: Menangani endpoint API REST untuk semua operasi terkait event termasuk pembuatan, pembaruan, penampilan, dan penghapusan.

### Service Layer
- Mengimplementasikan logika bisnis dengan interface dan implementasi (mengikuti praktik desain yang baik)
- EventService/Impl: Mengelola logika bisnis untuk operasi event

### Repository Layer
- EventRepository.java: Menangani operasi akses data untuk entitas Event
- UserRepository.java: Mengelola data pengguna yang diperlukan untuk asosiasi event

### DTO Layer
- EventCreateDTO.java: Berisi data yang diperlukan untuk membuat event baru
- EventResponseDTO.java: Memformat data event untuk respons API
- EventUpdateDTO.java: Menangani permintaan pembaruan event
- UserSummaryDTO.java: Berisi informasi pengguna minimal untuk asosiasi event

### Exception Layer
- EventNotFoundException.java: Exception khusus saat event tidak dapat ditemukan
- GlobalExceptionHandler.java: Penanganan exception terpusat untuk respons error yang konsisten
- UnauthorizedAccessException.java: Menangani percobaan modifikasi event yang tidak sah

### Model Layer
- Event.java: Entitas inti event dengan properti seperti judul, deskripsi, tanggal, tempat, dan harga
- User.java: Entitas pengguna untuk asosiasi dengan event (informasi penyelenggara)
- UserRole.java: Enum yang mendefinisikan peran pengguna yang berbeda dan izin mereka

### Design Pattern
1. **Repository Pattern**: 
   - Memisahkan logika akses data dari logika bisnis
   - Mengenkapsulasi operasi database untuk entitas Event

2. **Service Layer Pattern**: 
   - Berisi semua logika bisnis untuk operasi event
   - Mengelola transaksi dan memvalidasi aturan bisnis

3. **DTO Pattern**:
   - Memisahkan representasi API dari model domain
   - Mengontrol eksposur dan validasi data

4. **MVC Pattern**:
   - Memisahkan konsep antara model (data event), controller, dan view (respons API)

5. **Exception Handling Pattern**:
   - Menyediakan respons error yang konsisten melalui GlobalExceptionHandler
   - Menggunakan exception spesifik untuk skenario error yang berbeda

### Kemampuan Fitur
- Penyelenggara dapat membuat event baru dengan informasi detail
- Semua pengguna dapat melihat event yang tersedia
- Penyelenggara dapat memperbarui event mereka sendiri sebelum tenggat waktu tertentu
- Penyelenggara dapat menghapus event yang telah mereka buat

### Component Event Management
![Image](https://github.com/user-attachments/assets/f6a763d5-81b1-42fa-829e-ff639e387711)

### Code Diagram Event Management
![Image](https://github.com/user-attachments/assets/ea23fb3c-1684-402e-9501-5b87a49f3ee7)
![Image](https://github.com/user-attachments/assets/17b80fc1-4bbc-4112-a62e-fb66422360b7)
![Image](https://github.com/user-attachments/assets/df84aa82-51a5-466a-a6c8-30ee69efa23b)
![Image](https://github.com/user-attachments/assets/f522a0d1-3a6f-4043-b0cd-c516844e0989)

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



