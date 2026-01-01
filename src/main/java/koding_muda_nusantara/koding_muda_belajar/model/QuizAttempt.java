package koding_muda_nusantara.koding_muda_belajar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Integer attemptId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;
    
    @Column(name = "total_questions")
    private Integer totalQuestions = 0;
    
    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;
    
    @Column(name = "time_taken")
    private Integer timeTaken;
    
    @Column(name = "is_passed")
    private Boolean isPassed = false;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizAnswer> answers;
    
    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }
}
