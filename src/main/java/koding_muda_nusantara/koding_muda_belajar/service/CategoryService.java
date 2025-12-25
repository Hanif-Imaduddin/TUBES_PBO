package koding_muda_nusantara.koding_muda_belajar.service;

import java.util.HashMap;
import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Mengambil semua kategori yang statusnya aktif (is_active = true)
     */
    public List<Category> getAllActiveCategories() {
        // Asumsi di repo ada method findByIsActiveTrue()
        return categoryRepository.findByIsActiveTrue();
    }
    
    public List<CategoryDTO> getAllCategoryWithPublishedCourseCount(){
        return categoryRepository.findCategoryWithCourseCount();
    }
    
    public CategoryDTO getCategoryWithPublishedCourseCount(String categorySlug){
        return categoryRepository.findCategoryWithCourseCountBySlug(categorySlug).orElseThrow();
    }
    
    public List<CategoryDTO> getAllCategoriesWithCourseCount() {
        return categoryRepository.findAllCategoriesWithStats();
    }

    /**
     * Mendapatkan kategori berdasarkan slug
     */
    public CategoryDTO getCategoryBySlug(String slug) {
        return categoryRepository.findCategoryWithStatsBySlug(slug).orElse(null);
    }

    /**
     * Mendapatkan kategori berdasarkan ID
     */
    public CategoryDTO getCategoryById(Integer categoryId) {
        return categoryRepository.findCategoryWithStatsById(categoryId).orElse(null);
    }
    
    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAllWithCourseCount() {
        // Ambil semua kategori
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        
        // Ambil jumlah kursus per kategori
        Map<Integer, Long> courseCountMap = new HashMap<>();
        List<Object[]> courseCounts = categoryRepository.findCategoriesWithCourseCount();
        for (Object[] row : courseCounts) {
            Integer categoryId = (Integer) row[0];
            Long count = (Long) row[1];
            courseCountMap.put(categoryId, count);
        }
        
        // Konversi ke DTO dengan course count
        return categories.stream()
                .map(category -> {
                    Long count = courseCountMap.getOrDefault(category.getCategoryId(), 0L);
                    return CategoryDTO.fromEntity(category, count);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CategoryDTO> findById(Integer id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    Long courseCount = categoryRepository.countCoursesByCategoryId(id);
                    return CategoryDTO.fromEntity(category, courseCount);
                });
    }
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(category -> {
                    Long courseCount = categoryRepository.countCoursesByCategoryId(category.getCategoryId());
                    return CategoryDTO.fromEntity(category, courseCount);
                });
    }

    public CategoryDTO save(CategoryDTO categoryDTO) {
        // Validasi slug unik
        if (categoryRepository.existsBySlug(categoryDTO.getSlug())) {
            throw new IllegalArgumentException("Slug sudah digunakan: " + categoryDTO.getSlug());
        }
        
        // Validasi nama unik
        if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new IllegalArgumentException("Nama kategori sudah ada: " + categoryDTO.getName());
        }
        
        Category category = categoryDTO.toEntity();
        Category savedCategory = categoryRepository.save(category);
        
        return CategoryDTO.fromEntity(savedCategory, 0L);
    }

    public CategoryDTO update(Integer id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan dengan ID: " + id));
        
        // Validasi slug unik (kecuali untuk kategori ini sendiri)
        if (categoryRepository.existsBySlugAndCategoryIdNot(categoryDTO.getSlug(), id)) {
            throw new IllegalArgumentException("Slug sudah digunakan: " + categoryDTO.getSlug());
        }
        
        // Validasi nama unik (kecuali untuk kategori ini sendiri)
        if (categoryRepository.existsByNameIgnoreCaseAndCategoryIdNot(categoryDTO.getName(), id)) {
            throw new IllegalArgumentException("Nama kategori sudah ada: " + categoryDTO.getName());
        }
        
        // Update field
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setSlug(categoryDTO.getSlug());
        existingCategory.setDescription(categoryDTO.getDescription());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setActive(categoryDTO.isActive());
        
        Category updatedCategory = categoryRepository.save(existingCategory);
        Long courseCount = categoryRepository.countCoursesByCategoryId(id);
        
        return CategoryDTO.fromEntity(updatedCategory, courseCount);
    }

    public void deleteById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan dengan ID: " + id));
        
        // Cek apakah kategori memiliki kursus
        Long courseCount = categoryRepository.countCoursesByCategoryId(id);
        if (courseCount > 0) {
            throw new IllegalStateException("Tidak dapat menghapus kategori yang masih memiliki " + courseCount + " kursus");
        }
        
        categoryRepository.delete(category);
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAllActive() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> searchByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countCoursesByCategoryId(Integer categoryId) {
        return categoryRepository.countCoursesByCategoryId(categoryId);
    }

    // ========================================================================
    // VALIDATION OPERATIONS
    // ========================================================================

    @Transactional(readOnly = true)
    public boolean isSlugExists(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    @Transactional(readOnly = true)
    public boolean isSlugExistsExcludingId(String slug, Integer categoryId) {
        return categoryRepository.existsBySlugAndCategoryIdNot(slug, categoryId);
    }

    @Transactional(readOnly = true)
    public boolean isNameExists(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public boolean isNameExistsExcludingId(String name, Integer categoryId) {
        return categoryRepository.existsByNameIgnoreCaseAndCategoryIdNot(name, categoryId);
    }

    @Transactional(readOnly = true)
    public boolean canDelete(Integer categoryId) {
        Long courseCount = categoryRepository.countCoursesByCategoryId(categoryId);
        return courseCount == 0;
    }

    // ========================================================================
    // STATUS OPERATIONS
    // ========================================================================

    public CategoryDTO toggleActiveStatus(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan dengan ID: " + id));
        
        category.setActive(!category.isActive());
        Category updatedCategory = categoryRepository.save(category);
        Long courseCount = categoryRepository.countCoursesByCategoryId(id);
        
        return CategoryDTO.fromEntity(updatedCategory, courseCount);
    }

    public CategoryDTO setActiveStatus(Integer id, boolean isActive) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan dengan ID: " + id));
        
        category.setActive(isActive);
        Category updatedCategory = categoryRepository.save(category);
        Long courseCount = categoryRepository.countCoursesByCategoryId(id);
        
        return CategoryDTO.fromEntity(updatedCategory, courseCount);
    }
}