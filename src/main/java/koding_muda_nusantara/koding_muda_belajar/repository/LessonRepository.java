package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Lesson;
import koding_muda_nusantara.koding_muda_belajar.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {

    // Find by section id
    List<Lesson> findBySectionSectionIdOrderBySortOrder(Integer sectionId);

    // Find by section entity ordered by sort order
    List<Lesson> findBySectionOrderBySortOrder(Section section);

    // Count lessons by course
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.section.course.courseId = :courseId")
    int countByCourseId(@Param("courseId") Integer courseId);

    // Sum duration by course
    @Query("SELECT SUM(l.duration) FROM Lesson l WHERE l.section.course.courseId = :courseId")
    Integer sumDurationByCourseId(@Param("courseId") Integer courseId);

    // Update sort order
    @Modifying
    @Query("UPDATE Lesson l SET l.sortOrder = :sortOrder WHERE l.lessonId = :lessonId")
    void updateSortOrder(@Param("lessonId") Integer lessonId, @Param("sortOrder") Integer sortOrder);

    // Delete by section id
    void deleteBySectionSectionId(Integer sectionId);

    // Delete by section entity
    void deleteBySection(Section section);
}