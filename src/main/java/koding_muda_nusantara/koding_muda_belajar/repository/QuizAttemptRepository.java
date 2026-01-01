package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {
    
    List<QuizAttempt> findByStudentUserIdAndQuizQuizIdOrderByStartedAtDesc(Integer studentId, Integer quizId);

    long countByStudentUserIdAndQuizQuizId(Integer studentId, Integer quizId);

    Optional<QuizAttempt> findFirstByStudentUserIdAndQuizQuizIdOrderByScoreDesc(Integer studentId, Integer quizId);

    Optional<QuizAttempt> findFirstByStudentUserIdAndQuizQuizIdAndCompletedAtIsNullOrderByStartedAtDesc(Integer studentId, Integer quizId);

    List<QuizAttempt> findByStudentUserIdOrderByStartedAtDesc(Integer studentId);

    List<QuizAttempt> findByQuizQuizIdOrderByScoreDesc(Integer quizId);

    boolean existsByStudentUserIdAndQuizQuizIdAndIsPassedTrue(Integer studentId, Integer quizId);
}
