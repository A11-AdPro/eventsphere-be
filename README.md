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