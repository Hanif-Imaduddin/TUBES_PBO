package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, Integer> {
    
    List<QuizOption> findByQuestionQuestionIdOrderBySortOrderAsc(Integer questionId);
    
    Optional<QuizOption> findByQuestionQuestionIdAndIsCorrectTrue(Integer questionId);
    
    void deleteByQuestionQuestionId(Integer questionId);
}
