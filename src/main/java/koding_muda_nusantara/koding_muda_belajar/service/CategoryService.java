package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.model.Category;
import koding_muda_nusantara.koding_muda_belajar.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}