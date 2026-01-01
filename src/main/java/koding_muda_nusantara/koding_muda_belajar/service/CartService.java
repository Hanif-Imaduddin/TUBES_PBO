/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.service;

/**
 *
 * @author hanif
 */

import java.math.BigDecimal;
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.repository.*;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;
    
    public List<CartItem> getAllByStudentId(Integer studentId){
        return cartItemRepository.findByStudentUserId(studentId);
    }
    
    public BigDecimal getTotalPriceByStudenId(Integer studentId){
        BigDecimal totalPrice = cartItemRepository.sumTotalPriceByStudentId(studentId);
        if (totalPrice==null){
            return BigDecimal.valueOf(0);
        }
        return totalPrice;
    }
    
    public boolean alreadyExist(Integer studentId, Integer courseId){
        return cartItemRepository.existsByStudentUserIdAndCourseCourseId(studentId, courseId);
    }
    
    @Transactional
    public CartItem addItem(Student student, Course course){
        CartItem cartItem = new CartItem();
        cartItem.setCourse(course);
        cartItem.setStudent(student);
        return cartItemRepository.save(cartItem);
    }
    
    @Transactional
    public void removeItem(Integer itemId){
        cartItemRepository.deleteById(itemId);
    }
}
