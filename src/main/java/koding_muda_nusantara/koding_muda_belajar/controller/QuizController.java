package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/quiz")
public class QuizController {
    
    @Autowired
    private QuizService quizService;
    
    @Autowired
    private UserService userService;
    
    /**
     * GET /quiz/{quizId} - Info quiz sebelum mulai
     */
    @GetMapping("/{quizId}")
    public String showQuizInfo(@PathVariable Integer quizId, Model model, HttpSession session) {
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz == null) {
            return "redirect:/";
        }
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        int remainingAttempts = quizService.getRemainingAttempts(user.getUserId(), quizId);
        List<QuizAttempt> previousAttempts = quizService.getStudentAttempts(user.getUserId(), quizId);
        QuizAttempt bestAttempt = quizService.getBestAttempt(user.getUserId(), quizId);
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("remainingAttempts", remainingAttempts);
        model.addAttribute("canStart", remainingAttempts > 0);
        model.addAttribute("previousAttempts", previousAttempts);
        model.addAttribute("bestAttempt", bestAttempt);
        model.addAttribute("hasPassed", quizService.hasPassed(user.getUserId(), quizId));
        
        return "quiz/info";
    }
    
    /**
     * POST /quiz/{quizId}/start - Mulai quiz
     */
    @PostMapping("/{quizId}/start")
    public String startQuiz(@PathVariable Integer quizId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!quizService.canRetake(user.getUserId(), quizId)) {
            redirectAttributes.addFlashAttribute("error", "Anda sudah mencapai batas maksimal percobaan");
            return "redirect:/quiz/" + quizId;
        }
        
        try {
            QuizAttempt attempt = quizService.startAttempt(quizId, user.getUserId());
            return "redirect:/quiz/attempt/" + attempt.getAttemptId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/quiz/" + quizId;
        }
    }
    
    /**
     * GET /quiz/attempt/{attemptId} - Mengerjakan quiz
     */
    @GetMapping("/attempt/{attemptId}")
    public String takeQuiz(@PathVariable Integer attemptId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        QuizAttempt attempt = quizService.getAttemptById(attemptId);
        if (attempt == null) {
            return "redirect:/";
        }
        
        // Validasi attempt milik user yang sedang login
        if (!attempt.getStudent().getUserId().equals(user.getUserId())) {
            return "redirect:/";
        }
        
        // Cek apakah sudah selesai
        if (attempt.getCompletedAt() != null) {
            return "redirect:/quiz/attempt/" + attemptId + "/result";
        }
        
        Quiz quiz = attempt.getQuiz();
        List<QuizQuestion> questions = quizService.getQuestionsByQuizId(quiz.getQuizId());
        
        // Load options untuk setiap question
        for (QuizQuestion question : questions) {
            List<QuizOption> options = quizService.getOptionsByQuestionId(question.getQuestionId());
            question.setOptions(options);
        }
        
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        
        return "quiz/take";
    }
    
    /**
     * POST /quiz/attempt/{attemptId}/submit - Submit jawaban
     */
    @PostMapping("/attempt/{attemptId}/submit")
    public String submitQuiz(@PathVariable Integer attemptId, 
                            @RequestParam Map<String, String> allParams,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        QuizAttempt attempt = quizService.getAttemptById(attemptId);
        if (attempt == null || !attempt.getStudent().getUserId().equals(user.getUserId())) {
            return "redirect:/";
        }
        
        // Extract answers dari params (format: answer_questionId = optionId)
        Map<Integer, Integer> answers = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("answer_")) {
                try {
                    Integer questionId = Integer.parseInt(entry.getKey().replace("answer_", ""));
                    Integer optionId = Integer.parseInt(entry.getValue());
                    answers.put(questionId, optionId);
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }
        
        try {
            quizService.submitAttempt(attemptId, answers);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/quiz/attempt/" + attemptId + "/result";
    }
    
    /**
     * GET /quiz/attempt/{attemptId}/result - Hasil quiz
     */
    @GetMapping("/attempt/{attemptId}/result")
    public String showResult(@PathVariable Integer attemptId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        QuizAttempt attempt = quizService.getAttemptById(attemptId);
        if (attempt == null || !attempt.getStudent().getUserId().equals(user.getUserId())) {
            return "redirect:/";
        }
        
        // Cek apakah belum selesai
        if (attempt.getCompletedAt() == null) {
            return "redirect:/quiz/attempt/" + attemptId;
        }
        
        Quiz quiz = attempt.getQuiz();
        List<QuizAnswer> answers = quizService.getAnswersByAttemptId(attemptId);
        
        // Load question dan options untuk review
        for (QuizAnswer answer : answers) {
            QuizQuestion question = answer.getQuestion();
            List<QuizOption> options = quizService.getOptionsByQuestionId(question.getQuestionId());
            question.setOptions(options);
        }
        
        int remainingAttempts = quizService.getRemainingAttempts(user.getUserId(), quiz.getQuizId());
        
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", quiz);
        model.addAttribute("answers", answers);
        model.addAttribute("remainingAttempts", remainingAttempts);
        model.addAttribute("canRetake", remainingAttempts > 0 && !attempt.getIsPassed());
        
        return "quiz/result";
    }
}
