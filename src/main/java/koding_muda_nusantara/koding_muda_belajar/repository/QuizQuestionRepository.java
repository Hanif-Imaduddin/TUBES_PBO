package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Integer> {
    
    List<QuizQuestion> findByQuizQuizIdOrderBySortOrderAsc(Integer quizId);
    
    int countByQuizQuizId(Integer quizId);
    
    void deleteByQuizQuizId(Integer quizId);
}
