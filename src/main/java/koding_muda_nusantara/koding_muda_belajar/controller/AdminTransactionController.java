package koding_muda_nusantara.koding_muda_belajar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionDTO;
import koding_muda_nusantara.koding_muda_belajar.dto.TransactionStatsDTO;
import koding_muda_nusantara.koding_muda_belajar.enums.PaymentStatus;
import koding_muda_nusantara.koding_muda_belajar.enums.TransactionType;
import koding_muda_nusantara.koding_muda_belajar.model.Admin;
import koding_muda_nusantara.koding_muda_belajar.model.User;
import koding_muda_nusantara.koding_muda_belajar.service.AdminTransactionService;

@Controller
@RequestMapping("/admin")
public class AdminTransactionController {

    @Autowired
    private AdminTransactionService adminTransactionService;

    private static final int PAGE_SIZE = 15;

    /**
     * Check if user is admin
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user instanceof Admin;
    }

    /**
     * Get logged in user
     */
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    /**
     * Add common attributes to model
     */
    private void addCommonAttributes(Model model, HttpSession session) {
        model.addAttribute("user", getLoggedInUser(session));
        model.addAttribute("role", session.getAttribute("role"));
    }

    /**
     * Transaction list page
     */
    @GetMapping("/transactions")
    public String transactionsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            Model model,
            HttpSession session) {
        
        // Check admin access
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        addCommonAttributes(model, session);

        // Get transactions based on filters
        Page<TransactionDTO> transactionsPage;
        
        if (search != null && !search.isEmpty()) {
            // Search by keyword
            transactionsPage = adminTransactionService.searchTransactions(search, page, PAGE_SIZE);
            model.addAttribute("search", search);
        } else if (status != null && !status.isEmpty()) {
            // Filter by status
            try {
                PaymentStatus paymentStatus = PaymentStatus.valueOf(status);
                transactionsPage = adminTransactionService.getTransactionsByStatus(paymentStatus, page, PAGE_SIZE);
                model.addAttribute("statusFilter", status);
            } catch (IllegalArgumentException e) {
                transactionsPage = adminTransactionService.getAllTransactions(page, PAGE_SIZE);
            }
        } else if (type != null && !type.isEmpty()) {
            // Filter by type
            try {
                TransactionType transactionType = TransactionType.valueOf(type);
                transactionsPage = adminTransactionService.getTransactionsByType(transactionType, page, PAGE_SIZE);
                model.addAttribute("typeFilter", type);
            } catch (IllegalArgumentException e) {
                transactionsPage = adminTransactionService.getAllTransactions(page, PAGE_SIZE);
            }
        } else {
            // All transactions
            transactionsPage = adminTransactionService.getAllTransactions(page, PAGE_SIZE);
        }
        
        // Get statistics
        TransactionStatsDTO stats = adminTransactionService.getTransactionStats();
        
        // Add to model
        model.addAttribute("transactions", transactionsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionsPage.getTotalPages());
        model.addAttribute("totalElements", transactionsPage.getTotalElements());
        
        // Stats
        model.addAttribute("totalTransactions", stats.getTotalTransactions());
        model.addAttribute("paidTransactions", stats.getPaidTransactions());
        model.addAttribute("pendingTransactions", stats.getPendingTransactions());
        model.addAttribute("totalRevenue", stats.getTotalRevenue());
        
        // Payment statuses and types for filters
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        model.addAttribute("transactionTypes", TransactionType.values());
        
        return "admin/admin-transactions";
    }
}
