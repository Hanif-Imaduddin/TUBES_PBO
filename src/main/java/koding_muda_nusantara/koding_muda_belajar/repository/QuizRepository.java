package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Quiz;
import koding_muda_nusantara.koding_muda_belajar.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    
    Optional<Quiz> findByLesson(Lesson lesson);
    
    Optional<Quiz> findByLessonLessonId(Integer lessonId);
    
    List<Quiz> findByIsActiveTrue();
    
    boolean existsByLessonLessonId(Integer lessonId);
}
