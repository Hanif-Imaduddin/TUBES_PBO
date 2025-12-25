package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.dto.UserRequestDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserResponseDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.UserStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface untuk manajemen user di admin panel
 */
public interface AdminUserService {

    /**
     * Mendapatkan semua user dengan pagination
     */
    Page<UserResponseDTO> getAllUsers(Pageable pageable);

    /**
     * Mendapatkan semua user tanpa pagination
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Mendapatkan user berdasarkan ID
     */
    UserResponseDTO getUserById(Integer userId);

    /**
     * Mencari user berdasarkan keyword
     */
    Page<UserResponseDTO> searchUsers(String keyword, Pageable pageable);

    /**
     * Mendapatkan user berdasarkan role
     */
    Page<UserResponseDTO> getUsersByRole(String role, Pageable pageable);

    /**
     * Membuat user baru
     */
    UserResponseDTO createUser(UserRequestDTO request);

    /**
     * Mengupdate user yang sudah ada
     */
    UserResponseDTO updateUser(Integer userId, UserRequestDTO request);

    /**
     * Menghapus user
     */
    void deleteUser(Integer userId);

    /**
     * Mendapatkan statistik user
     */
    UserStatsDTO getUserStats();

    /**
     * Mengecek apakah username sudah ada
     */
    boolean isUsernameExists(String username);

    /**
     * Mengecek apakah username sudah ada (kecuali user tertentu)
     */
    boolean isUsernameExists(String username, Integer excludeUserId);

    /**
     * Mengecek apakah email sudah ada
     */
    boolean isEmailExists(String email);

    /**
     * Mengecek apakah email sudah ada (kecuali user tertentu)
     */
    boolean isEmailExists(String email, Integer excludeUserId);

    /**
     * Mengubah role user
     */
    UserResponseDTO changeUserRole(Integer userId, String newRole);
}
