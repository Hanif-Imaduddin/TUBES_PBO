/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.repository;

import java.util.List;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hanif
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByIsActiveTrue();
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO(" +
           "c.name, COUNT(co)) " +
           "FROM Category c " +
           "LEFT JOIN c.courses co ON co.status = 'published' " +
           "WHERE c.isActive = true " +
           "GROUP BY c.categoryId, c.name " +
           "ORDER BY COUNT(co) DESC")
    List<CategoryDTO> findCategoryWithCourseCount();
    // Mendapatkan kategori aktif saja
    List<Category> findByIsActiveTrueOrderByNameAsc();

    // Cari kategori berdasarkan slug
    Optional<Category> findBySlug(String slug);
    
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO(" +
       "c.name, COUNT(co)) " +
       "FROM Category c " +
       "LEFT JOIN c.courses co ON co.status = 'published' " +
       "WHERE c.slug = :slug AND c.isActive = true " +
       "GROUP BY c.categoryId, c.name")
    Optional<CategoryDTO> findCategoryWithCourseCountBySlug(@Param("slug") String slug);

    // Cek apakah slug sudah ada
    boolean existsBySlug(String slug);
    
    /**
     * Mendapatkan semua kategori dengan jumlah kursus (hanya yang published)
     * @return 
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO(" +
           "c.categoryId, c.name, c.slug, c.icon, COUNT(co)) " +
           "FROM Category c " +
           "LEFT JOIN c.courses co ON co.status = 'published' " +
           "WHERE c.isActive = true " +
           "GROUP BY c.categoryId, c.name, c.slug, c.icon " +
           "ORDER BY c.name ASC")
    List<CategoryDTO> findAllCategoriesWithStats();

    /**
     * Mendapatkan kategori berdasarkan slug dengan jumlah kursus
     * @param slug
     * @return 
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO(" +
           "c.categoryId, c.name, c.slug, c.icon, COUNT(co)) " +
           "FROM Category c " +
           "LEFT JOIN c.courses co ON co.status = 'published' " +
           "WHERE c.slug = :slug AND c.isActive = true " +
           "GROUP BY c.categoryId, c.name, c.slug, c.icon")
    Optional<CategoryDTO> findCategoryWithStatsBySlug(@Param("slug") String slug);

    /**
     * Mendapatkan kategori berdasarkan ID dengan jumlah kursus
     * @param categoryId
     * @return 
     */
    @Query("SELECT new koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO(" +
           "c.categoryId, c.name, c.slug, c.icon, COUNT(co)) " +
           "FROM Category c " +
           "LEFT JOIN c.courses co ON co.status = 'published' " +
           "WHERE c.categoryId = :categoryId AND c.isActive = true " +
           "GROUP BY c.categoryId, c.name, c.slug, c.icon")
    Optional<CategoryDTO> findCategoryWithStatsById(@Param("categoryId") Integer categoryId);

    
    // Find by name
    Optional<Category> findByName(String name);
    
    // Check if name exists
    boolean existsByName(String name);
    
    /**
     * Get all categories with course count
     * @return 
     */
    @Query("SELECT c, COUNT(course.courseId) as courseCount " +
           "FROM Category c " +
           "LEFT JOIN Course course ON course.category.categoryId = c.categoryId " +
           "GROUP BY c.categoryId " +
           "ORDER BY c.name ASC")
    List<Object[]> findAllWithCourseCount();
    
    /**
     * Get active categories with course count
     * @return 
     */
    @Query("SELECT c, COUNT(course.courseId) as courseCount " +
           "FROM Category c " +
           "LEFT JOIN Course course ON course.category.categoryId = c.categoryId " +
           "WHERE c.isActive = true " +
           "GROUP BY c.categoryId " +
           "ORDER BY c.name ASC")
    List<Object[]> findActiveWithCourseCount();
    
    // Cari berdasarkan nama (case insensitive)
    Optional<Category> findByNameIgnoreCase(String name);
    
    // Cari semua kategori diurutkan berdasarkan nama
    List<Category> findAllByOrderByNameAsc();
    
    // Cek apakah slug sudah ada selain ID tertentu (untuk update)
    boolean existsBySlugAndCategoryIdNot(String slug, Integer categoryId);
    
    // Cek apakah nama sudah ada
    boolean existsByNameIgnoreCase(String name);
    
    // Cek apakah nama sudah ada selain ID tertentu (untuk update)
    boolean existsByNameIgnoreCaseAndCategoryIdNot(String name, Integer categoryId);
    
    // Query untuk mendapatkan jumlah kursus per kategori
    @Query("SELECT c.categoryId, COUNT(co) FROM Category c LEFT JOIN c.courses co GROUP BY c.categoryId")
    List<Object[]> findCategoriesWithCourseCount();
    
    // Query untuk mendapatkan satu kategori dengan jumlah kursus
    @Query("SELECT COUNT(co) FROM Course co WHERE co.category.categoryId = :categoryId")
    Long countCoursesByCategoryId(@Param("categoryId") Integer categoryId);
    
    // Cari kategori berdasarkan nama (partial match)
    List<Category> findByNameContainingIgnoreCase(String name);
    
    // Query untuk mendapatkan kategori dengan jumlah kursus published
    @Query("SELECT c, COUNT(co) FROM Category c LEFT JOIN c.courses co ON co.status = 'published' GROUP BY c ORDER BY c.name ASC")
    List<Object[]> findAllWithPublishedCourseCount();
}