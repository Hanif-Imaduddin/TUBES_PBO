package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.dto.CourseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.LessonDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.SectionDTO;
import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.repository.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import koding_muda_nusantara.koding_muda_belajar.dto.CourseWithStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseLevel;
import koding_muda_nusantara.koding_muda_belajar.enums.CourseStatus;
import koding_muda_nusantara.koding_muda_belajar.enums.LessonContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TempFileService tempFileService;

    // Pattern untuk membuat slug
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIDASH = Pattern.compile("-+");

    // ==================== SLUG GENERATOR ====================

    /**
     * Generate URL-friendly slug dari title
     * Contoh: "Belajar JavaScript dari Nol" -> "belajar-javascript-dari-nol"
     */
    public String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }

        String nowhitespace = WHITESPACE.matcher(title).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = MULTIDASH.matcher(slug).replaceAll("-");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = slug.replaceAll("^-|-$", ""); // Hapus dash di awal dan akhir

        return slug;
    }

    /**
     * Generate unique slug (tambah suffix jika sudah ada)
     */
    public String generateUniqueSlug(String title, Integer excludeCourseId) {
        String baseSlug = generateSlug(title);
        String slug = baseSlug;
        int counter = 1;

        while (isSlugExists(slug, excludeCourseId)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    /**
     * Cek apakah slug sudah digunakan
     */
    public boolean isSlugExists(String slug, Integer excludeCourseId) {
        if (excludeCourseId != null) {
            return courseRepository.existsBySlugAndCourseIdNot(slug, excludeCourseId);
        }
        return courseRepository.existsBySlug(slug);
    }

    // ==================== CREATE COURSE ====================

    @Transactional
    public Course createCourse(CourseDTO dto, Integer lecturerId) {
        // Dapatkan lecturer
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new RuntimeException("Lecturer tidak ditemukan"));

        // Dapatkan category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        // Buat course baru
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setSlug(generateUniqueSlug(dto.getTitle(), null)); // Generate slug
        course.setDescription(dto.getDescription());
        course.setShortDescription(dto.getShortDescription());
        course.setCategory(category);
        course.setLecturer(lecturer);
        course.setLevel(CourseLevel.valueOf(dto.getLevel()));
        course.setPrice(dto.getPrice());
        course.setDiscountPrice(dto.getDiscountPrice());
        course.setThumbnailUrl(dto.getExistingThumbnailUrl());
        course.setStatus(dto.getStatus() != null ? CourseStatus.valueOf(dto.getStatus()) : CourseStatus.draft);
        course.setRequirements(dto.getRequirements());
        course.setWhatYouLearn(dto.getWhatYouLearn());
        course.setFeatured(false);
        course.setTotalLessons(0);
        course.setTotalDuration(0);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        if ("published".equals(dto.getStatus())) {
            course.setPublishedAt(LocalDateTime.now());
        }

        // Simpan course
        course = courseRepository.save(course);

        // Simpan sections dan lessons
        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
            saveSectionsAndLessons(course, dto.getSections());
        }

        // Update total lessons dan duration
        updateCourseTotals(course);

        return course;
    }

    // ==================== UPDATE COURSE ====================

    @Transactional
    public Course updateCourse(CourseDTO dto, Integer lecturerId) {
        // Dapatkan course existing
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Kursus tidak ditemukan"));

        // Validasi kepemilikan
        if (!course.getLecturer().getUserId().equals(lecturerId)) {
            throw new RuntimeException("Anda tidak memiliki akses ke kursus ini");
        }

        // Dapatkan category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        // Update fields
        String oldTitle = course.getTitle();
        course.setTitle(dto.getTitle());
        
        // Update slug jika title berubah
        if (!oldTitle.equals(dto.getTitle())) {
            course.setSlug(generateUniqueSlug(dto.getTitle(), course.getCourseId()));
        }
        
        course.setDescription(dto.getDescription());
        course.setShortDescription(dto.getShortDescription());
        course.setCategory(category);
        course.setLevel(CourseLevel.valueOf(dto.getLevel()));
        course.setPrice(dto.getPrice());
        course.setDiscountPrice(dto.getDiscountPrice());
        course.setRequirements(dto.getRequirements());
        course.setWhatYouLearn(dto.getWhatYouLearn());
        course.setUpdatedAt(LocalDateTime.now());

        // Update thumbnail jika ada
        if (dto.getExistingThumbnailUrl() != null && !dto.getExistingThumbnailUrl().isEmpty()) {
            course.setThumbnailUrl(dto.getExistingThumbnailUrl());
        }

        // Update status
        String oldStatus = course.getStatus().name();
        course.setStatus(CourseStatus.valueOf(dto.getStatus()));
        if ("published".equals(dto.getStatus()) && !"published".equals(oldStatus)) {
            course.setPublishedAt(LocalDateTime.now());
        }

        // Simpan course
        course = courseRepository.save(course);

        // Update sections dan lessons dengan smart merge (bukan hapus semua)
        if (dto.getSections() != null) {
            updateSectionsAndLessons(course, dto.getSections());
        }

        // Update total lessons dan duration
        updateCourseTotals(course);

        return course;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Update sections dan lessons dengan smart merge:
     * - Section yang sudah ada (by sectionId) -> update
     * - Section baru (tanpa sectionId) -> create
     * - Section yang tidak ada di DTO -> hapus
     * - Sama untuk lessons
     */
    private void updateSectionsAndLessons(Course course, List<SectionDTO> sectionDTOs) {
        // Ambil semua section existing untuk course ini
        List<Section> existingSections = sectionRepository.findByCourseOrderBySortOrder(course);
        
        // Track section IDs yang masih digunakan
        List<Integer> usedSectionIds = new ArrayList<>();
        
        for (int i = 0; i < sectionDTOs.size(); i++) {
            SectionDTO sectionDTO = sectionDTOs.get(i);
            Section section;
            
            // Cek apakah section sudah ada (by sectionId)
            if (sectionDTO.getSectionId() != null && sectionDTO.getSectionId() > 0) {
                // Cari section existing
                Optional<Section> existingSection = existingSections.stream()
                        .filter(s -> s.getSectionId().equals(sectionDTO.getSectionId()))
                        .findFirst();
                
                if (existingSection.isPresent()) {
                    // Update section yang sudah ada
                    section = existingSection.get();
                    section.setTitle(sectionDTO.getTitle());
                    section.setDescription(sectionDTO.getDescription());
                    section.setSortOrder(i);
                    section = sectionRepository.save(section);
                    usedSectionIds.add(section.getSectionId());
                } else {
                    // Section ID tidak ditemukan, buat baru
                    section = createNewSection(course, sectionDTO, i);
                    usedSectionIds.add(section.getSectionId());
                }
            } else {
                // Section baru (tanpa ID)
                section = createNewSection(course, sectionDTO, i);
                usedSectionIds.add(section.getSectionId());
            }
            
            // Update lessons untuk section ini
            if (sectionDTO.getLessons() != null) {
                updateLessonsForSection(section, sectionDTO.getLessons(), course.getCourseId());
            }
        }
        
        // Hapus sections yang tidak ada di DTO (sudah dihapus user)
        for (Section existingSection : existingSections) {
            if (!usedSectionIds.contains(existingSection.getSectionId())) {
                // Hapus lessons dulu, lalu section
                lessonRepository.deleteBySection(existingSection);
                sectionRepository.delete(existingSection);
            }
        }
    }

    /**
     * Buat section baru
     */
    private Section createNewSection(Course course, SectionDTO sectionDTO, int sortOrder) {
        Section section = new Section();
        section.setCourse(course);
        section.setTitle(sectionDTO.getTitle());
        section.setDescription(sectionDTO.getDescription());
        section.setSortOrder(sortOrder);
        section.setCreatedAt(LocalDateTime.now());
        return sectionRepository.save(section);
    }

    /**
     * Update lessons untuk section tertentu dengan smart merge:
     * - Lesson yang sudah ada (by lessonId) -> update sortOrder dan data lainnya
     * - Lesson baru (tanpa lessonId) -> create
     * - Lesson yang tidak ada di DTO -> hapus
     */
    private void updateLessonsForSection(Section section, List<LessonDTO> lessonDTOs, Integer courseId) {
        // Ambil semua lesson existing untuk section ini
        List<Lesson> existingLessons = lessonRepository.findBySectionOrderBySortOrder(section);
        
        // Track lesson IDs yang masih digunakan
        List<Integer> usedLessonIds = new ArrayList<>();
        
        for (int j = 0; j < lessonDTOs.size(); j++) {
            LessonDTO lessonDTO = lessonDTOs.get(j);
            Lesson lesson;
            
            // Cek apakah lesson sudah ada (by lessonId)
            if (lessonDTO.getLessonId() != null && lessonDTO.getLessonId() > 0) {
                // Cari lesson existing
                Optional<Lesson> existingLesson = existingLessons.stream()
                        .filter(l -> l.getLessonId().equals(lessonDTO.getLessonId()))
                        .findFirst();
                
                if (existingLesson.isPresent()) {
                    // Update lesson yang sudah ada (hanya sortOrder dan title)
                    lesson = existingLesson.get();
                    lesson.setTitle(lessonDTO.getTitle());
                    lesson.setSortOrder(j);
                    lesson.setUpdatedAt(LocalDateTime.now());
                    
                    // Update duration jika ada
                    if (lessonDTO.getDuration() != null) {
                        lesson.setDuration(lessonDTO.getDuration());
                    }
                    
                    // Update content type jika ada perubahan
                    if (lessonDTO.getContentType() != null && !lessonDTO.getContentType().isEmpty()) {
                        lesson.setContentType(LessonContentType.valueOf(lessonDTO.getContentType()));
                    }
                    
                    // Update contentText jika ada
                    if (lessonDTO.getContentText() != null) {
                        lesson.setContentText(lessonDTO.getContentText());
                    }
                    
                    // Update contentUrl hanya jika ada nilai baru (file baru diupload)
                    if (lessonDTO.getContentUrl() != null && !lessonDTO.getContentUrl().isEmpty()) {
                        String contentUrl = lessonDTO.getContentUrl();
                        if (tempFileService.isTempPath(contentUrl)) {
                            try {
                                contentUrl = tempFileService.moveFromTemp(contentUrl, courseId, lessonDTO.getContentType());
                            } catch (Exception e) {
                                System.err.println("Gagal memindahkan file dari temp: " + e.getMessage());
                            }
                        }
                        lesson.setContentUrl(contentUrl);
                    }
                    
                    lesson = lessonRepository.save(lesson);
                    usedLessonIds.add(lesson.getLessonId());
                } else {
                    // Lesson ID tidak ditemukan, buat baru
                    lesson = createNewLesson(section, lessonDTO, j, courseId);
                    usedLessonIds.add(lesson.getLessonId());
                }
            } else {
                // Lesson baru (tanpa ID)
                lesson = createNewLesson(section, lessonDTO, j, courseId);
                usedLessonIds.add(lesson.getLessonId());
            }
        }
        
        // Hapus lessons yang tidak ada di DTO (sudah dihapus user)
        for (Lesson existingLesson : existingLessons) {
            if (!usedLessonIds.contains(existingLesson.getLessonId())) {
                lessonRepository.delete(existingLesson);
            }
        }
    }

    /**
     * Buat lesson baru
     */
    private Lesson createNewLesson(Section section, LessonDTO lessonDTO, int sortOrder, Integer courseId) {
        Lesson lesson = new Lesson();
        lesson.setSection(section);
        lesson.setTitle(lessonDTO.getTitle());
        lesson.setContentType(LessonContentType.valueOf(lessonDTO.getContentType()));
        
        // Jika contentUrl adalah temp path, pindahkan ke folder permanent
        String contentUrl = lessonDTO.getContentUrl();
        if (contentUrl != null && tempFileService.isTempPath(contentUrl)) {
            try {
                contentUrl = tempFileService.moveFromTemp(contentUrl, courseId, lessonDTO.getContentType());
            } catch (Exception e) {
                System.err.println("Gagal memindahkan file dari temp: " + e.getMessage());
            }
        }
        lesson.setContentUrl(contentUrl);
        
        lesson.setContentText(lessonDTO.getContentText());
        lesson.setDuration(lessonDTO.getDuration() != null ? lessonDTO.getDuration() : 0);
        lesson.setSortOrder(sortOrder);
        lesson.setPreview(lessonDTO.getIsPreview() != null ? lessonDTO.getIsPreview() : false);
        lesson.setLocked(lessonDTO.getIsLocked() != null ? lessonDTO.getIsLocked() : true);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());
        
        return lessonRepository.save(lesson);
    }

    /**
     * Simpan sections dan lessons untuk course baru
     */
    private void saveSectionsAndLessons(Course course, List<SectionDTO> sectionDTOs) {
        for (int i = 0; i < sectionDTOs.size(); i++) {
            SectionDTO sectionDTO = sectionDTOs.get(i);

            Section section = new Section();
            section.setCourse(course);
            section.setTitle(sectionDTO.getTitle());
            section.setDescription(sectionDTO.getDescription());
            section.setSortOrder(i);
            section.setCreatedAt(LocalDateTime.now());

            section = sectionRepository.save(section);

            // Simpan lessons
            if (sectionDTO.getLessons() != null) {
                for (int j = 0; j < sectionDTO.getLessons().size(); j++) {
                    LessonDTO lessonDTO = sectionDTO.getLessons().get(j);
                    createNewLesson(section, lessonDTO, j, course.getCourseId());
                }
            }
        }
    }

    /**
     * Cleanup temp folder setelah course berhasil disimpan
     */
    public void cleanupLecturerTempFolder(Integer lecturerId) {
        try {
            tempFileService.cleanupTempFolder(lecturerId);
        } catch (Exception e) {
            System.err.println("Gagal membersihkan temp folder: " + e.getMessage());
        }
    }

    private void updateCourseTotals(Course course) {
        // Hitung total lessons
        int totalLessons = lessonRepository.countByCourseId(course.getCourseId());
        course.setTotalLessons(totalLessons);

        // Hitung total duration
        Integer totalDuration = lessonRepository.sumDurationByCourseId(course.getCourseId());
        course.setTotalDuration(totalDuration != null ? totalDuration : 0);

        courseRepository.save(course);
    }

    // ==================== QUERY METHODS ====================

    public Course getCourseById(Integer courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }

    public Course getCourseBySlug(String slug) {
        return courseRepository.findBySlug(slug).orElse(null);
    }

    public List<Course> getCoursesByLecturer(Integer lecturerId, String status, Integer categoryId, String search, int page) {
        // Implementasi dengan filter - sesuaikan dengan repository kamu
        return courseRepository.findByLecturerUserId(lecturerId);
    }

    public long countByLecturer(Integer lecturerId) {
        return courseRepository.countByLecturerUserId(lecturerId);
    }

    public long countByLecturerAndStatus(Integer lecturerId, String status) {
        return courseRepository.countByLecturerUserIdAndStatus(lecturerId, status);
    }

    public boolean isOwnedByLecturer(Integer courseId, Integer lecturerId) {
        return courseRepository.existsByCourseIdAndLecturerUserId(courseId, lecturerId);
    }

    @Transactional
    public void deleteCourse(Integer courseId) {
        courseRepository.deleteById(courseId);
    }

    // ==================== DTO CONVERSION ====================

    public CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setShortDescription(course.getShortDescription());
        dto.setCategoryId(course.getCategory().getCategoryId());
        dto.setLevel(course.getLevel().name());
        dto.setPrice(course.getPrice());
        dto.setDiscountPrice(course.getDiscountPrice());
        dto.setExistingThumbnailUrl(course.getThumbnailUrl());
        dto.setStatus(course.getStatus().name());
        dto.setRequirements(course.getRequirements());
        dto.setWhatYouLearn(course.getWhatYouLearn());

        // Convert sections
        List<SectionDTO> sectionDTOs = new ArrayList<>();
        if (course.getSections() != null) {
            for (Section section : course.getSections()) {
                SectionDTO sectionDTO = new SectionDTO();
                sectionDTO.setSectionId(section.getSectionId());
                sectionDTO.setTitle(section.getTitle());
                sectionDTO.setDescription(section.getDescription());
                sectionDTO.setSortOrder(section.getSortOrder());

                // Convert lessons
                List<LessonDTO> lessonDTOs = new ArrayList<>();
                if (section.getLessons() != null) {
                    for (Lesson lesson : section.getLessons()) {
                        LessonDTO lessonDTO = new LessonDTO();
                        lessonDTO.setLessonId(lesson.getLessonId());
                        lessonDTO.setTitle(lesson.getTitle());
                        lessonDTO.setContentType(lesson.getContentType().name());
                        lessonDTO.setContentUrl(lesson.getContentUrl());
                        lessonDTO.setContentText(lesson.getContentText());
                        lessonDTO.setDuration(lesson.getDuration());
                        lessonDTO.setSortOrder(lesson.getSortOrder());
                        lessonDTO.setIsPreview(lesson.isPreview());
                        lessonDTO.setIsLocked(lesson.isLocked());
                        lessonDTOs.add(lessonDTO);
                    }
                }
                sectionDTO.setLessons(lessonDTOs);
                sectionDTOs.add(sectionDTO);
            }
        }
        dto.setSections(sectionDTOs);

        return dto;
    }

    // ==================== JSON PARSING ====================

    public List<SectionDTO> parseSectionsFromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<SectionDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Gagal parsing sections JSON: " + e.getMessage());
        }
    }

    // ==================== SECTION & LESSON MANAGEMENT ====================

    @Transactional
    public SectionDTO addSection(Integer courseId, SectionDTO sectionDTO) {
        Course course = getCourseById(courseId);
        if (course == null) {
            throw new RuntimeException("Kursus tidak ditemukan");
        }

        Section section = new Section();
        section.setCourse(course);
        section.setTitle(sectionDTO.getTitle());
        section.setDescription(sectionDTO.getDescription());
        section.setSortOrder(sectionDTO.getSortOrder() != null ? sectionDTO.getSortOrder() : 0);
        section.setCreatedAt(LocalDateTime.now());

        section = sectionRepository.save(section);

        sectionDTO.setSectionId(section.getSectionId());
        return sectionDTO;
    }

    @Transactional
    public SectionDTO updateSection(Integer sectionId, SectionDTO sectionDTO) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section tidak ditemukan"));

        section.setTitle(sectionDTO.getTitle());
        section.setDescription(sectionDTO.getDescription());

        sectionRepository.save(section);
        
        sectionDTO.setSectionId(sectionId);
        return sectionDTO;
    }

    @Transactional
    public void deleteSection(Integer sectionId) {
        sectionRepository.deleteById(sectionId);
    }

    @Transactional
    public LessonDTO addLesson(Integer sectionId, LessonDTO lessonDTO) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section tidak ditemukan"));

        Lesson lesson = new Lesson();
        lesson.setSection(section);
        lesson.setTitle(lessonDTO.getTitle());
        lesson.setContentType(LessonContentType.valueOf(lessonDTO.getContentType()));
        lesson.setContentUrl(lessonDTO.getContentUrl());
        lesson.setContentText(lessonDTO.getContentText());
        lesson.setDuration(lessonDTO.getDuration() != null ? lessonDTO.getDuration() : 0);
        lesson.setSortOrder(lessonDTO.getSortOrder() != null ? lessonDTO.getSortOrder() : 0);
        lesson.setPreview(lessonDTO.getIsPreview() != null ? lessonDTO.getIsPreview() : false);
        lesson.setLocked(lessonDTO.getIsLocked() != null ? lessonDTO.getIsLocked() : true);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        lesson = lessonRepository.save(lesson);

        // Update course totals
        updateCourseTotals(section.getCourse());

        lessonDTO.setLessonId(lesson.getLessonId());
        return lessonDTO;
    }

    @Transactional
    public LessonDTO updateLesson(Integer lessonId, LessonDTO lessonDTO) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson tidak ditemukan"));

        lesson.setTitle(lessonDTO.getTitle());
        lesson.setContentType(LessonContentType.valueOf(lessonDTO.getContentType()));
        lesson.setDuration(lessonDTO.getDuration());
        lesson.setPreview(lessonDTO.getIsPreview());
        lesson.setUpdatedAt(LocalDateTime.now());

        lessonRepository.save(lesson);

        // Update course totals
        updateCourseTotals(lesson.getSection().getCourse());

        lessonDTO.setLessonId(lessonId);
        return lessonDTO;
    }

    @Transactional
    public void deleteLesson(Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson tidak ditemukan"));
        
        Course course = lesson.getSection().getCourse();
        lessonRepository.deleteById(lessonId);
        
        // Update course totals
        updateCourseTotals(course);
    }

    @Transactional
    public void updateLessonContent(Integer lessonId, String contentUrl, String contentType) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson tidak ditemukan"));

        lesson.setContentUrl(contentUrl);
        lesson.setContentType(LessonContentType.valueOf(contentType));
        lesson.setUpdatedAt(LocalDateTime.now());

        lessonRepository.save(lesson);
    }

    @Transactional
    public void reorderSections(Integer courseId, List<Integer> sectionIds) {
        for (int i = 0; i < sectionIds.size(); i++) {
            sectionRepository.updateSortOrder(sectionIds.get(i), i);
        }
    }

    @Transactional
    public void reorderLessons(Integer sectionId, List<Integer> lessonIds) {
        for (int i = 0; i < lessonIds.size(); i++) {
            lessonRepository.updateSortOrder(lessonIds.get(i), i);
        }
    }
    
//    ====================================
    
    public double getAverageRating(int courseId){
        Double average = reviewRepository.getAverageRatingByCourseId(courseId);
        if (average==null){
            return 0;
        }else{
            return average;
        }
    }
    
    public int getTotalReviews(int courseId){
        return reviewRepository.countByCourseCourseId(courseId);
    }
    public long getTotalEnrollments(int courseId){
        return enrollmentRepository.countByCourseCourseId(courseId);
    }
    
    public List<Review> getAllReviews(int courseId){
        return reviewRepository.findByCourseCourseId(courseId);
    }
    
    public List<Section> getAllSections(int courseId){
        return sectionRepository.findByCourseCourseIdOrderBySortOrder(courseId);
    }
    
    public long getTotalCourses(){
        return courseRepository.count();
    }
    
    public List<CourseWithStatsDTO> getPopularCourse(){
        return courseRepository.findPopularCoursesWithStatsAndLecturer(PageRequest.of(0,4));
    }
    
    public List<CourseWithStatsDTO> getRecentCourse(){
        return courseRepository.findRecentCoursesWithStatsAndLecturer(PageRequest.of(0,4));
    }
    
    /**
     * Mendapatkan kursus populer (berdasarkan jumlah student)
     */
    public List<CourseWithStatsDTO> getPopularCourses(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return courseRepository.findPopularCoursesWithStats(pageable);
    }

    /**
     * Mendapatkan kursus terbaru
     */
    public List<CourseWithStatsDTO> getNewestCourses(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return courseRepository.findNewestCoursesWithStats(pageable);
    }

    /**
     * Search kursus dengan filter dan pagination
     */
    public Page<CourseWithStatsDTO> searchCourses(
            String keyword,
            List<Integer> categoryIds,
            List<String> levels,
            Double minRating,
            String sort,
            int page,
            int size
    ) {
        // Convert level strings ke enum
        List<CourseLevel> courseLevels = null;
        if (levels != null && !levels.isEmpty()) {
            courseLevels = levels.stream()
                    .map(level -> {
                        try {
                            return CourseLevel.valueOf(level.toLowerCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(level -> level != null)
                    .collect(Collectors.toList());
            
            if (courseLevels.isEmpty()) {
                courseLevels = null;
            }
        }

        switch (sort) {
            case "popular" -> {
                Pageable pageable = PageRequest.of(page, size);
                
                // Handle empty lists
                List<Integer> catIds = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds : null;
                String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
                
                return courseRepository.searchPopularCoursesWithStats(
                        searchKeyword,
                        catIds,
                        courseLevels,
                        minRating,
                        pageable
                );
            }
            case "rating" -> {
                Pageable pageable = PageRequest.of(page, size);
                
                // Handle empty lists
                List<Integer> catIds = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds : null;
                String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
                
                return courseRepository.searchAvgRatingCoursesWithStats(
                        searchKeyword,
                        catIds,
                        courseLevels,
                        minRating,
                        pageable
                );
            }
            default -> {
                // Buat Sort berdasarkan parameter
                Sort sortOrder = createSort(sort);
                Pageable pageable = PageRequest.of(page, size, sortOrder);
                
                // Handle empty lists
                List<Integer> catIds = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds : null;
                String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
                
                return courseRepository.searchCoursesWithStats(
                        searchKeyword,
                        catIds,
                        courseLevels,
                        minRating,
                        pageable
                );
            }
        }
    }

    /**
     * Membuat Sort object berdasarkan parameter sort
     */
    private Sort createSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        switch (sort) {
            case "price-low":
                return Sort.by(Sort.Direction.ASC, "price");
            case "price-high":
                return Sort.by(Sort.Direction.DESC, "price");
            case "newest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    /**
     * Mendapatkan kursus berdasarkan kategori
     */
    public Page<CourseWithStatsDTO> getCoursesByCategory(Integer categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Integer> categoryIds = List.of(categoryId);
        return courseRepository.searchCoursesWithStats(null, categoryIds, null, null, pageable);
    }

    public long countPublishedCourse(){
        return courseRepository.countByStatus(CourseStatus.published);
    }
    
    public long countDraftCourse(){
        return courseRepository.countByStatus(CourseStatus.draft);
    }
}