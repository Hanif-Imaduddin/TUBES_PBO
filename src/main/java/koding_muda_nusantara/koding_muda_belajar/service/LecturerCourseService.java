/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;
import koding_muda_nusantara.koding_muda_belajar.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LecturerCourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    public Page<CourseWithStatsDTO> getLecturerCourses(Integer lecturerId, 
                                                        String status, 
                                                        String category,
                                                        String search,
                                                        int page, 
                                                        int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "courseId"));
        
        // Jika ada search
        if (search != null && !search.trim().isEmpty()) {
            return courseRepository.findCoursesWithStatsByLecturerIdAndSearch(lecturerId, search.trim(), pageable);
        }
        
        // Filter berdasarkan status dan kategori
        CourseStatus courseStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                courseStatus = CourseStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        boolean hasStatus = courseStatus != null;
        boolean hasCategory = category != null && !category.isEmpty();
        
        if (hasStatus && hasCategory) {
            return courseRepository.findCoursesWithStatsByLecturerIdAndStatusAndCategory(
                    lecturerId, courseStatus, category, pageable);
        } else if (hasStatus) {
            return courseRepository.findCoursesWithStatsByLecturerIdAndStatus(
                    lecturerId, courseStatus, pageable);
        } else if (hasCategory) {
            return courseRepository.findCoursesWithStatsByLecturerIdAndCategory(
                    lecturerId, category, pageable);
        } else {
            return courseRepository.findCoursesWithStatsByLecturerId(lecturerId, pageable);
        }
    }
    
    public long getTotalCourses(Integer lecturerId) {
        return courseRepository.countByLecturerUserId(lecturerId);
    }
    
    public long getPublishedCourses(Integer lecturerId) {
        return courseRepository.countByLecturerUserIdAndStatus(lecturerId, CourseStatus.published);
    }
    
    public long getDraftCourses(Integer lecturerId) {
        return courseRepository.countByLecturerUserIdAndStatus(lecturerId, CourseStatus.draft);
    }
}
