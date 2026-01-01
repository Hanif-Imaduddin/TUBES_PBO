package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Integer> {
    
    List<Discussion> findByLessonLessonIdOrderByIsPinnedDescCreatedAtDesc(Integer lessonId);
    
    List<Discussion> findByLessonLessonIdAndIsResolvedFalseOrderByCreatedAtDesc(Integer lessonId);
    
    List<Discussion> findByLessonLessonIdAndIsResolvedTrueOrderByCreatedAtDesc(Integer lessonId);
    
    List<Discussion> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    
    long countByLessonLessonId(Integer lessonId);
    
    long countByLessonLessonIdAndIsResolvedFalse(Integer lessonId);
}
