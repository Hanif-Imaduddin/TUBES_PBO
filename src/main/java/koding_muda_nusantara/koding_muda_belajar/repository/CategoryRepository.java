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
}