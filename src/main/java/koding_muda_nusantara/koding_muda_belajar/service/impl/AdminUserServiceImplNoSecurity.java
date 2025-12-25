package koding_muda_nusantara.koding_muda_belajar.service.impl;

import koding_muda_nusantara.koding_muda_belajar.dto.UserRequestDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserResponseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.exception.ResourceNotFoundException;
import koding_muda_nusantara.koding_muda_belajar.exception.DuplicateResourceException;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;
import koding_muda_nusantara.koding_muda_belajar.model.Lecturer;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.repository.AdminRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.LecturerRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.StudentRepository;
import koding_muda_nusantara.koding_muda_belajar.repository.UserRepository;
import koding_muda_nusantara.koding_muda_belajar.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service implementation untuk manajemen user di admin panel
 * Versi tanpa Spring Security (menggunakan session-based authentication)
 */
@Service
@Transactional
public class AdminUserServiceImplNoSecurity implements AdminUserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public AdminUserServiceImplNoSecurity(UserRepository userRepository,
                                          StudentRepository studentRepository,
                                          LecturerRepository lecturerRepository,
                                          AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAllOrderByJoinDateDesc(pageable);
        return users.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        List<UserResponseDTO> allUsers = new ArrayList<>();

        // Get all students
        studentRepository.findAll().forEach(student -> 
            allUsers.add(mapToResponseDTO(student, "student"))
        );

        // Get all lecturers
        lecturerRepository.findAll().forEach(lecturer -> 
            allUsers.add(mapToResponseDTO(lecturer, "lecturer"))
        );

        // Get all admins
        adminRepository.findAll().forEach(admin -> 
            allUsers.add(mapToResponseDTO(admin, "admin"))
        );

        // Sort by join date descending
        allUsers.sort(Comparator.comparing(
            UserResponseDTO::getJoinDate,
            Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return allUsers;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan dengan ID: " + userId));
        return mapToResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers(pageable);
        }
        Page<User> users = userRepository.searchUsers(keyword.trim(), pageable);
        return users.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getUsersByRole(String role, Pageable pageable) {
        List<UserResponseDTO> filteredUsers;
        long totalCount;

        switch (role.toLowerCase()) {
            case "student":
                Page<Student> studentPage = studentRepository.findAll(pageable);
                filteredUsers = studentPage.map(s -> mapToResponseDTO(s, "student")).getContent();
                totalCount = studentPage.getTotalElements();
                break;
            case "lecturer":
                Page<Lecturer> lecturerPage = lecturerRepository.findAll(pageable);
                filteredUsers = lecturerPage.map(l -> mapToResponseDTO(l, "lecturer")).getContent();
                totalCount = lecturerPage.getTotalElements();
                break;
            case "admin":
                Page<Admin> adminPage = adminRepository.findAll(pageable);
                filteredUsers = adminPage.map(a -> mapToResponseDTO(a, "admin")).getContent();
                totalCount = adminPage.getTotalElements();
                break;
            default:
                return getAllUsers(pageable);
        }

        return new PageImpl<>(filteredUsers, pageable, totalCount);
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        // Validate unique constraints
        if (isUsernameExists(request.getUsername())) {
            throw new DuplicateResourceException("Username sudah digunakan: " + request.getUsername());
        }
        if (isEmailExists(request.getEmail())) {
            throw new DuplicateResourceException("Email sudah digunakan: " + request.getEmail());
        }

        // Validate password for new user
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password wajib diisi untuk user baru");
        }

        String hashedPassword = hashPassword(request.getPassword());
        User savedUser;

        switch (request.getRole().toLowerCase()) {
            case "student":
                Student student = new Student(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getUsername(),
                    request.getEmail(),
                    hashedPassword
                );
                savedUser = studentRepository.save(student);
                break;

            case "lecturer":
                Lecturer lecturer = new Lecturer(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getUsername(),
                    request.getEmail(),
                    hashedPassword
                );
                savedUser = lecturerRepository.save(lecturer);
                break;

            case "admin":
                Admin admin = new Admin(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getUsername(),
                    request.getEmail(),
                    hashedPassword
                );
                savedUser = adminRepository.save(admin);
                break;

            default:
                throw new IllegalArgumentException("Role tidak valid: " + request.getRole());
        }

        return mapToResponseDTO(savedUser, request.getRole().toLowerCase());
    }

    @Override
    public UserResponseDTO updateUser(Integer userId, UserRequestDTO request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan dengan ID: " + userId));

        // Validate unique constraints
        if (isUsernameExists(request.getUsername(), userId)) {
            throw new DuplicateResourceException("Username sudah digunakan: " + request.getUsername());
        }
        if (isEmailExists(request.getEmail(), userId)) {
            throw new DuplicateResourceException("Email sudah digunakan: " + request.getEmail());
        }

        // Update basic info
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPasswordHash(hashPassword(request.getPassword()));
        }

        User savedUser = userRepository.save(existingUser);
        return mapToResponseDTO(savedUser);
    }

    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User tidak ditemukan dengan ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsDTO getUserStats() {
        long totalStudents = studentRepository.count();
        long totalLecturers = lecturerRepository.count();
        long totalAdmins = adminRepository.count();
        return new UserStatsDTO(totalStudents, totalLecturers, totalAdmins);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username, Integer excludeUserId) {
        return userRepository.existsByUsernameAndUserIdNot(username, excludeUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email, Integer excludeUserId) {
        return userRepository.existsByEmailAndUserIdNot(email, excludeUserId);
    }

    @Override
    public UserResponseDTO changeUserRole(Integer userId, String newRole) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan dengan ID: " + userId));

        String currentRole = determineUserRole(existingUser);
        
        if (currentRole.equalsIgnoreCase(newRole)) {
            return mapToResponseDTO(existingUser);
        }

        // Store user data before deletion
        String firstName = existingUser.getFirstName();
        String lastName = existingUser.getLastName();
        String username = existingUser.getUsername();
        String email = existingUser.getEmail();
        String passwordHash = existingUser.getPasswordHash();

        // Delete from current role table
        userRepository.deleteById(userId);
        userRepository.flush();

        // Create in new role table
        User newUser;
        switch (newRole.toLowerCase()) {
            case "student":
                Student student = new Student(firstName, lastName, username, email, passwordHash);
                newUser = studentRepository.save(student);
                break;

            case "lecturer":
                Lecturer lecturer = new Lecturer(firstName, lastName, username, email, passwordHash);
                newUser = lecturerRepository.save(lecturer);
                break;

            case "admin":
                Admin admin = new Admin(firstName, lastName, username, email, passwordHash);
                newUser = adminRepository.save(admin);
                break;

            default:
                throw new IllegalArgumentException("Role tidak valid: " + newRole);
        }

        return mapToResponseDTO(newUser, newRole.toLowerCase());
    }

    // Helper methods
    private UserResponseDTO mapToResponseDTO(User user) {
        String role = determineUserRole(user);
        return mapToResponseDTO(user, role);
    }

    private UserResponseDTO mapToResponseDTO(User user, String role) {
        return new UserResponseDTO(
            user.getUserId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            user.getEmail(),
            role,
            user.getJoinDate()
        );
    }

    private String determineUserRole(User user) {
        if (user instanceof Student) {
            return "student";
        } else if (user instanceof Lecturer) {
            return "lecturer";
        } else if (user instanceof Admin) {
            return "admin";
        }
        return "unknown";
    }

    /**
     * Hash password menggunakan SHA-256
     * Catatan: Untuk production, sebaiknya gunakan BCrypt atau Argon2
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
