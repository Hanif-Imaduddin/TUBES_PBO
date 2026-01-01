package koding_muda_nusantara.koding_muda_belajar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Integer optionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;
    
    @Column(name = "option_text", columnDefinition = "TEXT", nullable = false)
    private String optionText;
    
    @Column(name = "is_correct")
    private Boolean isCorrect = false;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
