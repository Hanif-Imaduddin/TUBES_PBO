/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.model;

/**
 *
 * @author hanif
 */
import jakarta.persistence.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "admin_id")
public class Admin extends User {

    public Admin() {}

    public Admin(String firstName, String lastName, String username, String email, String passwordHash) {
        super(firstName, lastName, username, email, passwordHash);
    }
}
