package koding_muda_nusantara.koding_muda_belajar.repository;

import java.util.Optional;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}