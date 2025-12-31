package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {

    // Find by course ordered by sort order
    List<Section> findByCourseCourseIdOrderBySortOrder(Integer courseId);

    // Find by course entity ordered by sort order
    List<Section> findByCourseOrderBySortOrder(Course course);

    // Count by course
    int countByCourseCourseId(Integer courseId);

    // Update sort order
    @Modifying
    @Query("UPDATE Section s SET s.sortOrder = :sortOrder WHERE s.sectionId = :sectionId")
    void updateSortOrder(@Param("sectionId") Integer sectionId, @Param("sortOrder") Integer sortOrder);

    // Delete by course
    void deleteByCourse(Course course);

    // Delete by course id
    void deleteByCourseCourseId(Integer courseId);
    
    /**
     * Ambil semua section untuk course tertentu, diurutkan berdasarkan sortOrder
     */
    List<Section> findByCourse_CourseIdOrderBySortOrderAsc(Integer courseId);
}