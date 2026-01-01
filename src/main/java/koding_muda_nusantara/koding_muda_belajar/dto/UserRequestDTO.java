package koding_muda_nusantara.koding_muda_belajar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO untuk request create/update user di admin panel
 */
public class UserRequestDTO {

    private Integer userId;

    @NotBlank(message = "Nama depan wajib diisi")
    @Size(min = 2, max = 100, message = "Nama depan harus antara 2-100 karakter")
    private String firstName;

    @Size(max = 100, message = "Nama belakang maksimal 100 karakter")
    private String lastName;

    @NotBlank(message = "Username wajib diisi")
    @Size(min = 3, max = 50, message = "Username harus antara 3-50 karakter")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username hanya boleh mengandung huruf, angka, dan underscore")
    private String username;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    @Size(max = 150, message = "Email maksimal 150 karakter")
    private String email;

    @Size(min = 6, max = 100, message = "Password harus antara 6-100 karakter")
    private String password;

    @NotBlank(message = "Role wajib dipilih")
    @Pattern(regexp = "^(student|lecturer|admin)$", message = "Role harus student, lecturer, atau admin")
    private String role;

    public UserRequestDTO() {
    }

    public UserRequestDTO(String firstName, String lastName, String username, 
                          String email, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
