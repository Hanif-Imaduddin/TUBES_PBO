package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.service.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/learn")
public class StudentLearnController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private ProgressService progressService;
    
    @Autowired
    private QuizService quizService;
    
    @Autowired
    private DiscussionService discussionService;

    // ==================== HALAMAN BELAJAR ====================

    /**
     * Redirect ke lesson pertama
     * URL: /learn/{courseSlug}
     */
    @GetMapping("/{courseSlug}")
    public String learnCourse(
            @PathVariable String courseSlug,
            HttpSession session
    ) {
        // Cek authentication
        Student student = getStudentFromSession(session);
        if (student == null) {
            return "redirect:/login?redirect=/learn/" + courseSlug;
        }

        // Dapatkan course
        Course course = courseService.getCourseBySlug(courseSlug);
        if (course == null) {
            return "redirect:/courses?error=notfound";
        }

        // Cek enrollment
        if (!enrollmentService.canAccessCourse(student.getUserId(), course.getCourseId())) {
            return "redirect:/courses/" + courseSlug + "?error=notEnrolled";
        }

        // Dapatkan lesson pertama
        Lesson firstLesson = getFirstLesson(course);
        if (firstLesson == null) {
            return "redirect:/courses/" + courseSlug + "?error=noContent";
        }

        // Redirect ke lesson pertama
        return "redirect:/learn/" + courseSlug + "/" + firstLesson.getLessonId();
    }

    /**
     * Halaman belajar lesson
     * URL: /learn/{courseSlug}/{lessonId}
     */
    @GetMapping("/{courseSlug}/{lessonId}")
    public String learnLesson(
            @PathVariable String courseSlug,
            @PathVariable Integer lessonId,
            HttpSession session,
            Model model
    ) {
        // Cek authentication
        Student student = getStudentFromSession(session);
        if (student == null) {
            return "redirect:/login?redirect=/learn/" + courseSlug + "/" + lessonId;
        }

        // Dapatkan course
        Course course = courseService.getCourseBySlug(courseSlug);
        if (course == null) {
            return "redirect:/courses?error=notfound";
        }

        // Cek enrollment
        if (!enrollmentService.canAccessCourse(student.getUserId(), course.getCourseId())) {
            return "redirect:/courses/" + courseSlug + "?error=notEnrolled";
        }

        // Dapatkan lesson
        Lesson lesson = findLessonInCourse(course, lessonId);
        if (lesson == null) {
            return "redirect:/learn/" + courseSlug;
        }

        // Update last accessed
        enrollmentService.updateLastAccessed(student.getUserId(), course.getCourseId());

        // Dapatkan progress
        int progress = progressService.calculateCourseProgress(student.getUserId(), course.getCourseId());
        boolean isCompleted = progressService.isLessonCompleted(student.getUserId(), lessonId);
        List<Integer> completedLessonIds = progressService.getCompletedLessonIds(student.getUserId(), course.getCourseId());

        // Dapatkan video position jika video
        int lastPosition = 0;
        if ("video".equals(lesson.getContentType())) {
            Optional<LessonProgress> lessonProgress = progressService.getLessonProgress(student.getUserId(), lessonId);
            if (lessonProgress.isPresent()) {
                lastPosition = lessonProgress.get().getLastPosition();
            }
        }

        // Build sections with completion status
        List<SectionWithProgress> sectionsWithProgress = buildSectionsWithProgress(course, completedLessonIds, lessonId);

        // Dapatkan prev/next lesson
        Lesson prevLesson = getPreviousLesson(course, lesson);
        Lesson nextLesson = getNextLesson(course, lesson);

        // Dapatkan quiz jika contentType adalah quiz
        Quiz quiz = null;
        if ("quiz".equalsIgnoreCase(lesson.getContentType().name())) {
            quiz = quizService.getQuizByLessonId(lessonId);
        }
        
        // Dapatkan diskusi untuk lesson ini
        List<Discussion> recentDiscussions = discussionService.getDiscussionsByLesson(lessonId);
        // Ambil maksimal 3 diskusi terbaru untuk ditampilkan
        if (recentDiscussions.size() > 3) {
            recentDiscussions = recentDiscussions.subList(0, 3);
        }
        long discussionCount = discussionService.getDiscussionCountByLesson(lessonId);

        // Set model attributes
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("sections", sectionsWithProgress);
        model.addAttribute("progress", progress);
        model.addAttribute("isCompleted", isCompleted);
        model.addAttribute("lastPosition", lastPosition);
        model.addAttribute("prevLesson", prevLesson);
        model.addAttribute("nextLesson", nextLesson);
        model.addAttribute("user", student);
        model.addAttribute("quiz", quiz);
        model.addAttribute("recentDiscussions", recentDiscussions);
        model.addAttribute("discussionCount", discussionCount);

        return "student/learn";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * Update progress lesson (mark complete/incomplete)
     * POST /api/learn/progress
     */
    @PostMapping("/api/progress")
    @ResponseBody
    public Map<String, Object> updateProgress(
            @RequestBody Map<String, Object> request,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Student student = getStudentFromSession(session);
        if (student == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            Integer lessonId = (Integer) request.get("lessonId");
            Boolean completed = (Boolean) request.get("completed");

            if (lessonId == null) {
                response.put("success", false);
                response.put("message", "lessonId diperlukan");
                return response;
            }

            // Toggle atau set completed
            LessonProgress progress;
            if (completed != null) {
                progress = progressService.updateLessonProgress(student.getUserId(), lessonId, completed);
            } else {
                progress = progressService.toggleLessonComplete(student.getUserId(), lessonId);
            }

            // Hitung course progress baru
            Integer courseId = progress.getLesson().getSection().getCourse().getCourseId();
            int courseProgress = progressService.calculateCourseProgress(student.getUserId(), courseId);

            response.put("success", true);
            response.put("isCompleted", progress.isCompleted());
            response.put("progress", courseProgress);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Update video progress (position)
     * POST /api/learn/video-progress
     */
    @PostMapping("/api/video-progress")
    @ResponseBody
    public Map<String, Object> updateVideoProgress(
            @RequestBody Map<String, Object> request,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Student student = getStudentFromSession(session);
        if (student == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            Integer lessonId = (Integer) request.get("lessonId");
            Integer currentTime = request.get("currentTime") != null ? 
                    ((Number) request.get("currentTime")).intValue() : 0;
            Integer duration = request.get("duration") != null ? 
                    ((Number) request.get("duration")).intValue() : 0;

            if (lessonId == null) {
                response.put("success", false);
                response.put("message", "lessonId diperlukan");
                return response;
            }

            LessonProgress progress = progressService.updateVideoPosition(
                    student.getUserId(), lessonId, currentTime, duration);

            // Hitung course progress jika auto-completed
            int courseProgress = 0;
            if (progress.isCompleted()) {
                Integer courseId = progress.getLesson().getSection().getCourse().getCourseId();
                courseProgress = progressService.calculateCourseProgress(student.getUserId(), courseId);
            }

            response.put("success", true);
            response.put("lastPosition", progress.getLastPosition());
            response.put("isCompleted", progress.isCompleted());
            response.put("progress", courseProgress);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Get video progress (last position)
     * GET /api/learn/video-progress/{lessonId}
     */
    @GetMapping("/api/video-progress/{lessonId}")
    @ResponseBody
    public Map<String, Object> getVideoProgress(
            @PathVariable Integer lessonId,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();

        Student student = getStudentFromSession(session);
        if (student == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        try {
            Optional<LessonProgress> progress = progressService.getLessonProgress(student.getUserId(), lessonId);

            if (progress.isPresent()) {
                response.put("success", true);
                response.put("currentTime", progress.get().getLastPosition());
                response.put("watchTime", progress.get().getWatchTime());
                response.put("isCompleted", progress.get().isCompleted());
            } else {
                response.put("success", true);
                response.put("currentTime", 0);
                response.put("watchTime", 0);
                response.put("isCompleted", false);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Dapatkan student dari session
     */
    private Student getStudentFromSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user instanceof Student) {
            return (Student) user;
        }
        return null;
    }

    /**
     * Dapatkan lesson pertama dari course
     */
    private Lesson getFirstLesson(Course course) {
        if (course.getSections() == null || course.getSections().isEmpty()) {
            System.out.println("Tidak ada sections");
            return null;
        }

        // Sort sections by sortOrder
        List<Section> sections = new ArrayList<>(course.getSections());
        sections.sort(Comparator.comparingInt(s -> s.getSortOrder() != null ? s.getSortOrder() : 0));

        for (Section section : sections) {
            if (section.getLessons() != null && !section.getLessons().isEmpty()) {
                // Sort lessons by sortOrder
                List<Lesson> lessons = new ArrayList<>(section.getLessons());
                lessons.sort(Comparator.comparingInt(l -> l.getSortOrder() != null ? l.getSortOrder() : 0));
                return lessons.get(0);
            }
        }

        return null;
    }

    /**
     * Cari lesson dalam course
     */
    private Lesson findLessonInCourse(Course course, Integer lessonId) {
        if (course.getSections() == null) {
            return null;
        }

        for (Section section : course.getSections()) {
            if (section.getLessons() != null) {
                for (Lesson lesson : section.getLessons()) {
                    if (lesson.getLessonId().equals(lessonId)) {
                        return lesson;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Dapatkan lesson sebelumnya
     */
    private Lesson getPreviousLesson(Course course, Lesson currentLesson) {
        List<Lesson> allLessons = getAllLessonsSorted(course);
        
        for (int i = 0; i < allLessons.size(); i++) {
            if (allLessons.get(i).getLessonId().equals(currentLesson.getLessonId())) {
                return i > 0 ? allLessons.get(i - 1) : null;
            }
        }
        
        return null;
    }

    /**
     * Dapatkan lesson selanjutnya
     */
    private Lesson getNextLesson(Course course, Lesson currentLesson) {
        List<Lesson> allLessons = getAllLessonsSorted(course);
        
        for (int i = 0; i < allLessons.size(); i++) {
            if (allLessons.get(i).getLessonId().equals(currentLesson.getLessonId())) {
                return i < allLessons.size() - 1 ? allLessons.get(i + 1) : null;
            }
        }
        
        return null;
    }

    /**
     * Dapatkan semua lesson dalam course, sorted
     */
    private List<Lesson> getAllLessonsSorted(Course course) {
        List<Lesson> allLessons = new ArrayList<>();
        
        if (course.getSections() == null) {
            return allLessons;
        }

        // Sort sections
        List<Section> sections = new ArrayList<>(course.getSections());
        sections.sort(Comparator.comparingInt(s -> s.getSortOrder() != null ? s.getSortOrder() : 0));

        for (Section section : sections) {
            if (section.getLessons() != null) {
                // Sort lessons
                List<Lesson> lessons = new ArrayList<>(section.getLessons());
                lessons.sort(Comparator.comparingInt(l -> l.getSortOrder() != null ? l.getSortOrder() : 0));
                allLessons.addAll(lessons);
            }
        }

        return allLessons;
    }

    /**
     * Build sections with progress info for sidebar
     */
    private List<SectionWithProgress> buildSectionsWithProgress(Course course, List<Integer> completedLessonIds, Integer activeLessonId) {
        List<SectionWithProgress> result = new ArrayList<>();

        if (course.getSections() == null) {
            return result;
        }

        // Sort sections
        List<Section> sections = new ArrayList<>(course.getSections());
        sections.sort(Comparator.comparingInt(s -> s.getSortOrder() != null ? s.getSortOrder() : 0));

        for (Section section : sections) {
            SectionWithProgress sectionWithProgress = new SectionWithProgress();
            sectionWithProgress.setSectionId(section.getSectionId());
            sectionWithProgress.setTitle(section.getTitle());
            sectionWithProgress.setDescription(section.getDescription());
            sectionWithProgress.setSortOrder(section.getSortOrder());

            List<LessonWithProgress> lessonsWithProgress = new ArrayList<>();
            boolean hasActiveLesson = false;

            if (section.getLessons() != null) {
                // Sort lessons
                List<Lesson> lessons = new ArrayList<>(section.getLessons());
                lessons.sort(Comparator.comparingInt(l -> l.getSortOrder() != null ? l.getSortOrder() : 0));

                for (Lesson lesson : lessons) {
                    LessonWithProgress lessonWithProgress = new LessonWithProgress();
                    lessonWithProgress.setLessonId(lesson.getLessonId());
                    lessonWithProgress.setTitle(lesson.getTitle());
                    lessonWithProgress.setContentType(lesson.getContentType().name());
                    lessonWithProgress.setDuration(lesson.getDuration());
                    lessonWithProgress.setSortOrder(lesson.getSortOrder());
                    lessonWithProgress.setIsPreview(lesson.isPreview());
                    lessonWithProgress.setCompleted(completedLessonIds.contains(lesson.getLessonId()));

                    if (lesson.getLessonId().equals(activeLessonId)) {
                        hasActiveLesson = true;
                    }

                    lessonsWithProgress.add(lessonWithProgress);
                }
            }

            sectionWithProgress.setLessons(lessonsWithProgress);
            sectionWithProgress.setHasActiveLesson(hasActiveLesson);
            result.add(sectionWithProgress);
        }

        return result;
    }

    // ==================== INNER CLASSES ====================

    /**
     * Section dengan info progress
     */
    public static class SectionWithProgress {
        private Integer sectionId;
        private String title;
        private String description;
        private Integer sortOrder;
        private List<LessonWithProgress> lessons;
        private boolean hasActiveLesson;

        // Getters and Setters
        public Integer getSectionId() { return sectionId; }
        public void setSectionId(Integer sectionId) { this.sectionId = sectionId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public List<LessonWithProgress> getLessons() { return lessons; }
        public void setLessons(List<LessonWithProgress> lessons) { this.lessons = lessons; }

        public boolean isHasActiveLesson() { return hasActiveLesson; }
        public void setHasActiveLesson(boolean hasActiveLesson) { this.hasActiveLesson = hasActiveLesson; }
    }

    /**
     * Lesson dengan info progress
     */
    public static class LessonWithProgress {
        private Integer lessonId;
        private String title;
        private String contentType;
        private Integer duration;
        private Integer sortOrder;
        private Boolean isPreview;
        private boolean isCompleted;

        // Getters and Setters
        public Integer getLessonId() { return lessonId; }
        public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public Boolean getIsPreview() { return isPreview; }
        public void setIsPreview(Boolean isPreview) { this.isPreview = isPreview; }

        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
    }
}
