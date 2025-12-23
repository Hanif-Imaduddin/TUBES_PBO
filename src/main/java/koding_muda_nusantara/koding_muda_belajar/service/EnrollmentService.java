package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Enrollment;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.repository.CourseRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.EnrollmentRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.enums.EnrollmentStatus;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;
import koding_muda_nusantara.koding_muda_belajar.model.CartItem;
import koding_muda_nusantara.koding_muda_belajar.model.Transaction;
import koding_muda_nusantara.koding_muda_belajar.model.TransactionItem;
import koding_muda_nusantara.koding_muda_belajar.repository.CartItemRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.TransactionItemRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.TransactionRepository;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private TransactionItemRepository transactionItemRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;

    // ==================== CHECK ENROLLMENT ====================

    /**
     * Cek apakah student sudah enroll di course
     */
    public boolean isEnrolled(Integer studentId, Integer courseId) {
        return enrollmentRepository.existsByStudentUserIdAndCourseCourseId(studentId, courseId);
    }

    /**
     * Cek apakah student bisa akses course (enrolled dan aktif)
     */
    public boolean canAccessCourse(Integer studentId, Integer courseId) {
        Optional<Enrollment> enrollment = enrollmentRepository
                .findByStudentUserIdAndCourseCourseId(studentId, courseId);
        
        if (enrollment.isEmpty()) {
            return false;
        }
        
        Enrollment e = enrollment.get();
        return e.getStatus() == EnrollmentStatus.active || 
               e.getStatus() == EnrollmentStatus.completed;
    }

    // ==================== GET ENROLLMENT ====================

    /**
     * Dapatkan enrollment berdasarkan student dan course
     */
    public Optional<Enrollment> getEnrollment(Integer studentId, Integer courseId) {
        return enrollmentRepository.findByStudentUserIdAndCourseCourseId(studentId, courseId);
    }

    /**
     * Dapatkan enrollment by ID
     */
    public Optional<Enrollment> getEnrollmentById(Integer enrollmentId) {
        return enrollmentRepository.findById(enrollmentId);
    }

    /**
     * Dapatkan semua enrollment student
     */
    public List<Enrollment> getStudentEnrollments(Integer studentId) {
        return enrollmentRepository.findByStudentUserIdOrderByEnrolledAtDesc(studentId);
    }

    /**
     * Dapatkan enrollment aktif student
     */
    public List<Enrollment> getActiveEnrollments(Integer studentId) {
        return enrollmentRepository.findActiveEnrollments(studentId);
    }

    /**
     * Dapatkan enrollment yang sudah selesai
     */
    public List<Enrollment> getCompletedEnrollments(Integer studentId) {
        return enrollmentRepository.findCompletedEnrollments(studentId);
    }

    /**
     * Dapatkan enrollment terakhir diakses
     */
    public List<Enrollment> getRecentlyAccessedEnrollments(Integer studentId) {
        return enrollmentRepository.findRecentlyAccessed(studentId);
    }

    // ==================== CREATE ENROLLMENT ====================

    /**
     * Enroll student ke course
     */
    @Transactional
    public Enrollment enrollStudent(Integer studentId, Integer courseId) {
        // Cek apakah sudah enrolled
        if (isEnrolled(studentId, courseId)) {
            throw new RuntimeException("Student sudah terdaftar di kursus ini");
        }

        // Dapatkan student dan course
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student tidak ditemukan"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Kursus tidak ditemukan"));

        // Buat enrollment baru
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgressPercentage(BigDecimal.ZERO);
        enrollment.setStatus(EnrollmentStatus.active);

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Enroll student ke course gratis
     */
    @Transactional
    public Enrollment enrollToFreeCourse(Integer studentId, Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Kursus tidak ditemukan"));

        // Validasi course gratis
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Kursus ini tidak gratis");
        }

        return enrollStudent(studentId, courseId);
    }

    // ==================== UPDATE ENROLLMENT ====================

    /**
     * Update progress enrollment
     */
    @Transactional
    public Enrollment updateProgress(Integer studentId, Integer courseId, BigDecimal progress) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentUserIdAndCourseCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment tidak ditemukan"));

        enrollment.setProgressPercentage(progress);
        enrollment.setLastAccessedAt(LocalDateTime.now());

        // Jika progress 100%, tandai sebagai selesai
        if (progress.compareTo(new BigDecimal("100.00")) >= 0) {
            enrollment.setStatus(EnrollmentStatus.completed);
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Update last accessed time
     */
    @Transactional
    public void updateLastAccessed(Integer studentId, Integer courseId) {
        Optional<Enrollment> enrollment = enrollmentRepository
                .findByStudentUserIdAndCourseCourseId(studentId, courseId);
        
        enrollment.ifPresent(e -> {
            e.setLastAccessedAt(LocalDateTime.now());
            enrollmentRepository.save(e);
        });
    }

    /**
     * Tandai enrollment sebagai selesai
     */
    @Transactional
    public Enrollment markAsCompleted(Integer studentId, Integer courseId) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentUserIdAndCourseCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment tidak ditemukan"));

        enrollment.markAsCompleted();
        return enrollmentRepository.save(enrollment);
    }

    // ==================== STATISTICS ====================

    /**
     * Hitung jumlah student di course
     */
    public long countStudentsInCourse(Integer courseId) {
        return enrollmentRepository.countByCourseCourseId(courseId);
    }

    /**
     * Hitung jumlah course yang diikuti student
     */
    public long countStudentCourses(Integer studentId) {
        return enrollmentRepository.countByStudentUserId(studentId);
    }

    /**
     * Hitung jumlah course yang sudah selesai
     */
    public long countCompletedCourses(Integer studentId) {
        return enrollmentRepository.countByStudentUserIdAndStatus(studentId, EnrollmentStatus.completed);
    }
    
    @Transactional
    public void enrollAllItems(Transaction transaction){
        if (transaction.getPaymentStatus() == PaymentStatus.paid){
            List<TransactionItem> items = transactionItemRepository.findByTransactionId(transaction.getId());
            for (TransactionItem item : items){
                Enrollment enrollment = enrollStudent(transaction.getStudent().getUserId(), item.getCourse().getCourseId());
                
                CartItem cartItem = cartItemRepository.findByStudentUserIdAndCourseCourseId(transaction.getStudent().getUserId(), item.getCourse().getCourseId())
                        .orElseThrow(() -> new RuntimeException("Cart Item tidak ditemukan"));
                cartItemRepository.delete(cartItem);
                System.out.println(enrollment);
            }
        }
    }
}
