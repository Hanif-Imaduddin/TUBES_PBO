/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package koding_muda_nusantara.koding_muda_belajar.controller;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentMethod;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;
import koding_muda_nusantara.koding_muda_belajar.model.CartItem;
import koding_muda_nusantara.koding_muda_belajar.model.Course;
import koding_muda_nusantara.koding_muda_belajar.model.Student;
import koding_muda_nusantara.koding_muda_belajar.model.Transaction;
import koding_muda_nusantara.koding_muda_belajar.model.TransactionItem;
import koding_muda_nusantara.koding_muda_belajar.service.CartService;
import koding_muda_nusantara.koding_muda_belajar.service.CourseService;
import koding_muda_nusantara.koding_muda_belajar.service.EnrollmentService;
import koding_muda_nusantara.koding_muda_belajar.service.TransactionService;
import koding_muda_nusantara.koding_muda_belajar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author hanif
 */

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("")
    public String showCheckoutDetail(
            @RequestParam(name="successMessage",required = false) String successMessage,
            @RequestParam(name="errorMessage",required = false) String errorMessage,
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
        
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("checkoutItems", cartItems);
        model.addAttribute("total", total);
        return "student/checkout";
    }
    
    @PostMapping("/process")
    public String processsCheckout(
            @RequestParam(name = "courseIds") List<Integer> courseIds,
            @RequestParam(name = "paymentMethod") String paymentMethod,
            @RequestParam(name = "totalAmount") BigDecimal totalAmount,
            HttpSession session
    ){
        Student student = userService.getStudentFromSession(session);
        if (student == null) {
            System.out.println("Student == null");
            return "redirect:/logout";
        }
        Transaction transaction = transactionService.createTransaction(courseIds, paymentMethod, student);
        
        if (transaction.getPaymentStatus() == PaymentStatus.paid){
            enrollmentService.enrollAllItems(transaction);
        }else{
            return "redirect:/checkout?errorMessage=pembayaranGagal";
        }
        return "redirect:/checkout/"+transaction.getTransactionCode()+"/success";
    }
    
    @GetMapping("/{transactionCode}/success")
    public String showTransactionSuccess(
            @PathVariable String transactionCode,
            Model model,
            HttpSession session
    ){
        Transaction transaction = transactionService.getByTransactionId(transactionCode);
        model.addAttribute("transactionCode", transaction.getTransactionCode());
        model.addAttribute("transactionDate", transaction.getCreatedAt());
        String strPaymentMethod = "";
        if (transaction.getPaymentMethod()==PaymentMethod.bank_transfer){
            strPaymentMethod = "Transfer Bank";
        }else if (transaction.getPaymentMethod() == PaymentMethod.ewallet){
            strPaymentMethod = "E-Wallet";
        }
        model.addAttribute("paymentMethod", strPaymentMethod);
        model.addAttribute("totalAmount", transaction.getAmount());
        
        List<TransactionItem> items = transactionService.getAllItemsById(transaction.getId());
        List<Course> purchasedCourses = new ArrayList<>();
        for (TransactionItem item: items){
            purchasedCourses.add(item.getCourse());
        }
        model.addAttribute("purchasedCourses", purchasedCourses);
        return "/student/checkout-success";
    }
}
