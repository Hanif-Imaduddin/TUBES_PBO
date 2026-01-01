package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.dto.CategoryDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.CategoryService;
import koding_muda_nusantara.koding_muda_belajar.service.CourseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CategoryService categoryService;

    private static final int DEFAULT_PAGE_SIZE = 12;

    /**
     * Halaman utama kursus / jelajahi kursus
     */
    @GetMapping
    public String coursesPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Integer> categoryIds,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session
    ) {
        return searchCourses(keyword, categoryIds, levels, minRating, sort, page, model, session);
    }

    /**
     * Halaman search kursus dengan filter
     */
    @GetMapping("/search")
    public String searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Integer> categoryIds,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session
    ) {
        // Get user dari session
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");

        // Search courses dengan filter
        Page<CourseWithStatsDTO> coursePage = courseService.searchCourses(
                keyword,
                categoryIds,
                levels,
                minRating,
                sort,
                page,
                DEFAULT_PAGE_SIZE
        );

        // Get semua kategori untuk filter sidebar
        List<CategoryDTO> categories = categoryService.getAllCategoriesWithCourseCount();

        // Cek apakah ada filter aktif
        boolean hasActiveFilters = (keyword != null && !keyword.isEmpty()) ||
                (categoryIds != null && !categoryIds.isEmpty()) ||
                (levels != null && !levels.isEmpty()) ||
                minRating != null;

        // Get nama kategori yang dipilih untuk ditampilkan di active filters
        List<String> selectedCategoryNames = null;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            selectedCategoryNames = categories.stream()
                    .filter(cat -> categoryIds.contains(cat.getCategoryId()))
                    .map(CategoryDTO::getName)
                    .toList();
        }

        // Add attributes ke model
        model.addAttribute("user", user);
        model.addAttribute("role", role);
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryIds", categoryIds);
        model.addAttribute("selectedCategoryNames", selectedCategoryNames);
        model.addAttribute("selectedLevels", levels);
        model.addAttribute("minRating", minRating);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("totalElements", coursePage.getTotalElements());
        model.addAttribute("hasActiveFilters", hasActiveFilters);

        return "course-search";
    }

    /**
     * Halaman kursus berdasarkan kategori
     */
    @GetMapping("/category/{slug}")
    public String coursesByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "newest") String sort,
            Model model,
            HttpSession session
    ) {
        // Get user dari session
        User user = (User) session.getAttribute("user");
        String role = (String) session.getAttribute("role");

        // Get category by slug
        CategoryDTO category = categoryService.getCategoryBySlug(slug);

        if (category == null) {
            return "redirect:/courses?error=categorynotfound";
        }

        // Get courses by category
        Page<CourseWithStatsDTO> coursePage = courseService.getCoursesByCategory(
                category.getCategoryId(),
                page,
                DEFAULT_PAGE_SIZE
        );

        // Get semua kategori untuk filter sidebar
        List<CategoryDTO> categories = categoryService.getAllCategoriesWithCourseCount();

        model.addAttribute("user", user);
        model.addAttribute("role", role);
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("currentCategory", category);
        model.addAttribute("selectedCategoryIds", List.of(category.getCategoryId()));
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("totalElements", coursePage.getTotalElements());

        return "course-search";
    }
}