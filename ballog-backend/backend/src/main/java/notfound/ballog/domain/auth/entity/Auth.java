package notfound.ballog.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import notfound.ballog.domain.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_seq_generator")
    @SequenceGenerator(
            name = "auth_seq_generator",
            sequenceName = "auth_seq",
            allocationSize = 1
    )
    private Integer authId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String refreshToken;

    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    public void changeRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void changeIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void reactivate(User user, String email, String password) {
        this.user = user;
        this.email = email;
        this.password = password;
        this.isActive = true;
    }
}
