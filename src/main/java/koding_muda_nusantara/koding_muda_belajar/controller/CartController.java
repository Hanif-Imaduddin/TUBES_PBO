/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.controller;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.model.CartItem;
import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.service.CartService;
import koding_muda_nusantara.koding_muda_belajar.service.CourseService;
import koding_muda_nusantara.koding_muda_belajar.service.EnrollmentService;
import koding_muda_nusantara.koding_muda_belajar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author hanif
 */
@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @GetMapping("")
    public String showCartDetail(
            @RequestParam(name="error",required = false) String err,
            HttpSession session,
            Model model
    ){
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            System.out.println("Student == null");
            return "redirect:/logout";
        }
        
        List<CartItem> cartItems = cartService.getAllByStudentId(student.getUserId());
        BigDecimal total = cartService.getTotalPriceByStudenId(student.getUserId());
        
        model.addAttribute("error", err);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("student", student);
        model.addAttribute("total", total);
        
        return "student/cart";
    }
    
    @PostMapping("/add")
    public String addItemToCart(
            @RequestParam(name = "courseId") Integer courseId,
            HttpSession session
    ){
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            System.out.println("Student == null");
            return "redirect:/logout";
        }
        Course course = courseService.getCourseById(courseId);
        // Cek apakah item sudah ada di cart
        if (cartService.alreadyExist(student.getUserId(), courseId)){
            return "redirect:/courses/"+course.getSlug()+"?error=itemIsAlreadyInCart";
        }
        
        if (enrollmentService.isEnrolled(student.getUserId(), courseId)){
            return "redirect:/courses/"+course.getSlug()+"?error=itemIsEnrolled";
        }
        // Cek apakah item sudah dimiliki
        CartItem cartItem = cartService.addItem(student, course);
        System.out.println(cartItem);
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeItemFromCart(
            @RequestParam(name = "itemId") Integer itemId,
            HttpSession session
    ){
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            System.out.println("Student == null");
            return "redirect:/logout";
        }
        cartService.removeItem(itemId);
        return "redirect:/cart";
    }
}
