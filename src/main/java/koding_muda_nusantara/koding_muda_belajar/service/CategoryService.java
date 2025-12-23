package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO;

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
}