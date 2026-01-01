package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Integer> {
    
    List<QuizAnswer> findByAttemptAttemptId(Integer attemptId);
    
    Optional<QuizAnswer> findByAttemptAttemptIdAndQuestionQuestionId(Integer attemptId, Integer questionId);
    
    void deleteByAttemptAttemptId(Integer attemptId);
    
    int countByAttemptAttemptIdAndIsCorrectTrue(Integer attemptId);
}
