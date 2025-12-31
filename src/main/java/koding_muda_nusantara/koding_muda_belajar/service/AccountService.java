package koding_muda_nusantara.koding_muda_belajar.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import koding_muda_nusantara.koding_muda_belajar.dto.AccountInfoDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.ChangePasswordDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UpdateProfileDTO;
import koding_muda_nusantara.koding_muda_belajar.exception.BadRequestException;
import koding_muda_nusantara.koding_muda_belajar.exception.ResourceNotFoundException;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.repository.UserRepository;

@Service
@Transactional
public class AccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    // Repositories untuk stats (opsional)
    @Autowired(required = false)
    private koding_muda_nusantara.koding_muda_belajar.repository.EnrollmentRepository enrollmentRepository;

    @Autowired(required = false)
    private koding_muda_nusantara.koding_muda_belajar.repository.CourseRepository courseRepository;

    /**
     * Mendapatkan informasi akun lengkap
     */
    @Transactional(readOnly = true)
    public AccountInfoDTO getAccountInfo(Integer userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        AccountInfoDTO accountInfo = new AccountInfoDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getJoinDate(),
                role
        );

        // Set balance jika ada
        if (user.getBalance() != null) {
            accountInfo.setBalance(BigDecimal.valueOf(user.getBalance().getAmount()));
        } else {
            accountInfo.setBalance(BigDecimal.ZERO);
        }

        // Tambahkan stats berdasarkan role
        enrichWithRoleStats(accountInfo, userId, role);

        return accountInfo;
    }

    /**
     * Update profil user
     */
    public User updateProfile(Integer userId, UpdateProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // Validasi username unik
        if (!user.getUsername().equals(dto.getUsername())) {
            if (userRepository.existsByUsernameAndUserIdNot(dto.getUsername(), userId)) {
                throw new BadRequestException("Username sudah digunakan");
            }
        }

        // Validasi email unik
        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.existsByEmailAndUserIdNot(dto.getEmail(), userId)) {
                throw new BadRequestException("Email sudah digunakan");
            }
        }

        // Update data
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        return userRepository.save(user);
    }

    /**
     * Mengubah password user
     */
    public void changePassword(Integer userId, ChangePasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // Validasi password lama
        if (!verifyPassword(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Password lama tidak sesuai");
        }

        // Validasi konfirmasi password
        if (!dto.isPasswordMatch()) {
            throw new BadRequestException("Konfirmasi password tidak sesuai");
        }

        // Validasi password baru tidak sama dengan yang lama
        if (verifyPassword(dto.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Password baru tidak boleh sama dengan password lama");
        }

        // Update password
        String hashedPassword = hashPassword(dto.getNewPassword());
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
    }

    /**
     * Mendapatkan user berdasarkan ID
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    /**
     * Verifikasi password
     */
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (passwordEncoder != null) {
            return passwordEncoder.matches(rawPassword, hashedPassword);
        }
        // Fallback jika tidak ada password encoder (untuk development)
        return rawPassword.equals(hashedPassword);
    }

    /**
     * Hash password
     */
    private String hashPassword(String rawPassword) {
        if (passwordEncoder != null) {
            return passwordEncoder.encode(rawPassword);
        }
        // Fallback jika tidak ada password encoder (untuk development)
        return rawPassword;
    }

    /**
     * Menambahkan statistik berdasarkan role
     */
    private void enrichWithRoleStats(AccountInfoDTO accountInfo, Integer userId, String role) {
        try {
            switch (role) {
                case "Student":
                    enrichStudentStats(accountInfo, userId);
                    break;
                case "Lecturer":
                    enrichLecturerStats(accountInfo, userId);
                    break;
                case "Admin":
                    // Admin tidak memerlukan stats khusus
                    break;
            }
        } catch (Exception e) {
            // Jika ada error saat mengambil stats, abaikan saja
        }
    }

    /**
     * Menambahkan statistik untuk Student
     */
    private void enrichStudentStats(AccountInfoDTO accountInfo, Integer studentId) {
        if (enrollmentRepository != null) {
            try {
                long enrolled = enrollmentRepository.countByStudent_UserId(studentId);
                long completed = enrollmentRepository.countByStudent_UserIdAndStatus(
                        studentId, 
                        koding_muda_nusantara.koding_muda_belajar.enums.EnrollmentStatus.completed
                );
                accountInfo.setEnrolledCourses(enrolled);
                accountInfo.setCompletedCourses(completed);
            } catch (Exception e) {
                accountInfo.setEnrolledCourses(0L);
                accountInfo.setCompletedCourses(0L);
            }
        }
    }

    /**
     * Menambahkan statistik untuk Lecturer
     */
    private void enrichLecturerStats(AccountInfoDTO accountInfo, Integer lecturerId) {
        if (courseRepository != null) {
            try {
                long totalCourses = courseRepository.countByLecturerUserId(lecturerId);
                accountInfo.setTotalCourses(totalCourses);
                
                // Total students bisa dihitung dari enrollments di semua course lecturer
                // Ini adalah contoh sederhana, bisa dikembangkan lebih lanjut
                accountInfo.setTotalStudents(0L);
                accountInfo.setTotalEarnings(BigDecimal.ZERO);
            } catch (Exception e) {
                accountInfo.setTotalCourses(0L);
                accountInfo.setTotalStudents(0L);
                accountInfo.setTotalEarnings(BigDecimal.ZERO);
            }
        }
    }
}
