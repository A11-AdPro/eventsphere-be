package id.ac.ui.cs.advprog.eventsphere.authentication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor 
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String fullName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(columnDefinition = "integer default 0")
    @Builder.Default
    private Integer balance = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public void topUp(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }
        if (this.balance == null) {
            this.balance = 0;
        }
        this.balance += amount;
    }

    public boolean deductBalance(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deduction amount must be positive");
        }
        if (this.balance == null || this.balance < amount) {
            return false;
        }
        this.balance -= amount;
        return true;
    }

    public int getBalance() {
        return this.balance != null ? this.balance : 0;
    }
    
    public void setBalance(int balance) {
        this.balance = balance;
    }
    
    public String getUsername() {
        if (this.fullName != null && !this.fullName.isEmpty()) {
            return this.fullName;
        }
        return this.email;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", fullName='" + fullName + '\'' +
                ", balance=" + balance +
                '}';
    }
}
