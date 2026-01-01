package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizService {
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private QuizQuestionRepository questionRepository;
    
    @Autowired
    private QuizOptionRepository optionRepository;
    
    @Autowired
    private QuizAttemptRepository attemptRepository;
    
    @Autowired
    private QuizAnswerRepository answerRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    // ==================== QUIZ METHODS ====================
    
    public Quiz getQuizByLessonId(Integer lessonId) {
        return quizRepository.findByLessonLessonId(lessonId).orElse(null);
    }
    
    public Quiz getQuizById(Integer quizId) {
        return quizRepository.findById(quizId).orElse(null);
    }
    
    public boolean hasQuiz(Integer lessonId) {
        return quizRepository.existsByLessonLessonId(lessonId);
    }
    
    public List<QuizQuestion> getQuestionsByQuizId(Integer quizId) {
        return questionRepository.findByQuizQuizIdOrderBySortOrderAsc(quizId);
    }
    
    public List<QuizOption> getOptionsByQuestionId(Integer questionId) {
        return optionRepository.findByQuestionQuestionIdOrderBySortOrderAsc(questionId);
    }
    
    // ==================== ATTEMPT METHODS ====================
    
    @Transactional
    public QuizAttempt startAttempt(Integer quizId, Integer studentId) {
        // Validasi quiz
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null || !quiz.getIsActive()) {
            throw new RuntimeException("Quiz tidak ditemukan atau tidak aktif");
        }
        
        // Cek apakah masih bisa attempt
        if (!canRetake(studentId, quizId)) {
            throw new RuntimeException("Anda telah mencapai batas maksimal percobaan");
        }
        
        // Cek apakah ada attempt yang belum selesai
        Optional<QuizAttempt> unfinishedAttempt = attemptRepository
                .findFirstByStudentUserIdAndQuizQuizIdAndCompletedAtIsNullOrderByStartedAtDesc(studentId, quizId);
        if (unfinishedAttempt.isPresent()) {
            return unfinishedAttempt.get(); // Return attempt yang belum selesai
        }
        
        // Dapatkan student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student tidak ditemukan"));
        
        // Buat QuizAttempt baru
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setTotalQuestions(questionRepository.countByQuizQuizId(quizId));
        attempt.setStartedAt(LocalDateTime.now());
        
        return attemptRepository.save(attempt);
    }
    
    @Transactional
    public QuizAttempt submitAttempt(Integer attemptId, Map<Integer, Integer> answers) {
        // Get attempt
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt tidak ditemukan"));
        
        // Cek apakah sudah disubmit
        if (attempt.getCompletedAt() != null) {
            throw new RuntimeException("Quiz sudah disubmit sebelumnya");
        }
        
        Quiz quiz = attempt.getQuiz();
        List<QuizQuestion> questions = questionRepository.findByQuizQuizIdOrderBySortOrderAsc(quiz.getQuizId());
        
        int totalPoints = 0;
        int earnedPoints = 0;
        int correctCount = 0;
        
        // Save semua jawaban dan hitung score
        for (QuizQuestion question : questions) {
            totalPoints += question.getPoints();
            
            QuizAnswer answer = new QuizAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            
            Integer selectedOptionId = answers.get(question.getQuestionId());
            
            if (selectedOptionId != null) {
                QuizOption selectedOption = optionRepository.findById(selectedOptionId).orElse(null);
                if (selectedOption != null) {
                    answer.setSelectedOption(selectedOption);
                    
                    // Cek jawaban benar (untuk multiple choice dan true/false)
                    if (selectedOption.getIsCorrect()) {
                        answer.setIsCorrect(true);
                        answer.setPointsEarned(question.getPoints());
                        earnedPoints += question.getPoints();
                        correctCount++;
                    } else {
                        answer.setIsCorrect(false);
                        answer.setPointsEarned(0);
                    }
                }
            } else {
                answer.setIsCorrect(false);
                answer.setPointsEarned(0);
            }
            
            answerRepository.save(answer);
        }
        
        // Hitung score dalam persen
        BigDecimal score = BigDecimal.ZERO;
        if (totalPoints > 0) {
            score = BigDecimal.valueOf(earnedPoints)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalPoints), 2, RoundingMode.HALF_UP);
        }
        
        // Hitung waktu yang dihabiskan (dalam detik)
        long timeTaken = ChronoUnit.SECONDS.between(attempt.getStartedAt(), LocalDateTime.now());
        
        // Update attempt
        attempt.setScore(score);
        attempt.setCorrectAnswers(correctCount);
        attempt.setTimeTaken((int) timeTaken);
        attempt.setIsPassed(score.compareTo(BigDecimal.valueOf(quiz.getPassingScore())) >= 0);
        attempt.setCompletedAt(LocalDateTime.now());
        
        return attemptRepository.save(attempt);
    }
    
    // ==================== HELPER METHODS ====================
    
    public boolean canRetake(Integer studentId, Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return false;

        long attempts = attemptRepository.countByStudentUserIdAndQuizQuizId(studentId, quizId);
        return attempts < quiz.getMaxAttempts();
    }
    
    public int getRemainingAttempts(Integer studentId, Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz == null) return 0;

        long attempts = attemptRepository.countByStudentUserIdAndQuizQuizId(studentId, quizId);
        return Math.max(0, quiz.getMaxAttempts() - (int) attempts);
    }
    
    public List<QuizAttempt> getStudentAttempts(Integer studentId, Integer quizId) {
        return attemptRepository.findByStudentUserIdAndQuizQuizIdOrderByStartedAtDesc(studentId, quizId);
    }
    
    public QuizAttempt getBestAttempt(Integer studentId, Integer quizId) {
        return attemptRepository.findFirstByStudentUserIdAndQuizQuizIdOrderByScoreDesc(studentId, quizId)
                .orElse(null);
    }
    
    public QuizAttempt getAttemptById(Integer attemptId) {
        return attemptRepository.findById(attemptId).orElse(null);
    }
    
    public List<QuizAnswer> getAnswersByAttemptId(Integer attemptId) {
        return answerRepository.findByAttemptAttemptId(attemptId);
    }
    
    public boolean hasPassed(Integer studentId, Integer quizId) {
        return attemptRepository.existsByStudentUserIdAndQuizQuizIdAndIsPassedTrue(studentId, quizId);
    }
    
    public long getAttemptCount(Integer studentId, Integer quizId) {
        return attemptRepository.countByStudentUserIdAndQuizQuizId(studentId, quizId);
    }
}
