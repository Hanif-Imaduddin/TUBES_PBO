/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.service;

/**
 *
 * @author hanif
 */
import jakarta.servlet.http.HttpSession;
import koding_muda_nusantara.koding_muda_belajar.dto.LoginRequest;
import koding_muda_nusantara.koding_muda_belajar.dto.RegisterRequest;
import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Transactional
    public User register(RegisterRequest request) {
        // Validasi email dan username unik
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email sudah terdaftar");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username sudah digunakan");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user;

        // Buat user berdasarkan role
        if ("lecturer".equalsIgnoreCase(request.getRole())) {
            Lecturer lecturer = new Lecturer(
                request.getFirstName(),
                request.getLastName(),
                request.getUsername(),
                request.getEmail(),
                hashedPassword
            );
            user = lecturerRepository.save(lecturer);
        } else if ("student".equalsIgnoreCase(request.getRole())) {
            Student student = new Student(
                request.getFirstName(),
                request.getLastName(),
                request.getUsername(),
                request.getEmail(),
                hashedPassword
            );
            user = studentRepository.save(student);
        } else {
            throw new RuntimeException("Role tidak valid");
        }

        // Buat balance untuk user baru
        Balance balance = new Balance(user);
        user.setBalance(balance);

        // Save lagi untuk update balance
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        // Cari user berdasarkan email
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Email tidak ditemukan");
        }

        User user = optionalUser.get();

        // Verifikasi password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Password salah");
        }

        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public String getUserRole(User user) {
        if (user instanceof Lecturer) {
            return "Lecturer";
        } else if (user instanceof Student) {
            return "Student";
        }
        return "Unknown";
    }
    
    public Student getStudentFromSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user instanceof Student student) {
            return student;
        }
        return null;
    }
    
    public int getCartCount(User user){
        return cartItemRepository.countByStudentUserId(user.getUserId());
    }
}
