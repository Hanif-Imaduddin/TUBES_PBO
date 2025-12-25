/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.repository;

/**
 *
 * @author hanif
 */
import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.model.Lecturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {
    Optional<Lecturer> findByUsername(String username);

    Optional<Lecturer> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Search lecturers
    @Query("SELECT l FROM Lecturer l WHERE " +
           "LOWER(l.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Lecturer> searchLecturers(@Param("keyword") String keyword, Pageable pageable);
}
