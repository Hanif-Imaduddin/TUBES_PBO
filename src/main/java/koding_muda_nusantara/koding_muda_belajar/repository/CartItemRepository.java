/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.repository;

/**
 *
 * @author hanif
 */
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author hanif
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    int countByStudentUserId(Integer studentId);
}
