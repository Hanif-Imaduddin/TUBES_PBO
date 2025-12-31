package koding_muda_nusantara.koding_muda_belajar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO untuk mengubah password user
 */
public class ChangePasswordDTO {

    @NotBlank(message = "Password lama tidak boleh kosong")
    private String currentPassword;

    @NotBlank(message = "Password baru tidak boleh kosong")
    @Size(min = 8, max = 255, message = "Password baru harus minimal 8 karakter")
    private String newPassword;

    @NotBlank(message = "Konfirmasi password tidak boleh kosong")
    private String confirmPassword;

    // Default constructor
    public ChangePasswordDTO() {
    }

    // Getters and Setters
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // Validation helper
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
