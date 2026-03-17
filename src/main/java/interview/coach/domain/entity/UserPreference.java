package interview.coach.domain.entity;

import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_direction", length = 50)
    private InterviewDirection preferredDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_level", length = 30)
    private InterviewLevel preferredLevel;

    @Column(name = "preferred_language", length = 30)
    private String preferredLanguage;

    @Column(name = "interface_language", length = 10)
    private String interfaceLanguage;

    @Column(length = 20)
    private String theme;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
