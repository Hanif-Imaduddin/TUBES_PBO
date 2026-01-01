package koding_muda_nusantara.koding_muda_belajar.controller;

import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.service.*;
import koding_muda_nusantara.koding_muda_belajar.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/discussion")
public class DiscussionController {
    
    @Autowired
    private DiscussionService discussionService;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private CourseService courseService;
    
    /**
     * GET /discussion/lesson/{lessonId} - List diskusi per lesson
     */
    @GetMapping("/lesson/{lessonId}")
    public String listDiscussions(@PathVariable Integer lessonId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return "redirect:/";
        }
        
        Course course = lesson.getSection().getCourse();
        List<Discussion> discussions = discussionService.getDiscussionsByLesson(lessonId);
        long discussionCount = discussionService.getDiscussionCountByLesson(lessonId);
        
        // Check user votes for each discussion
        Map<Integer, Boolean> userVotes = new HashMap<>();
        for (Discussion d : discussions) {
            userVotes.put(d.getDiscussionId(), discussionService.hasUserVotedDiscussion(user.getUserId(), d.getDiscussionId()));
        }
        
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("discussions", discussions);
        model.addAttribute("discussionCount", discussionCount);
        model.addAttribute("userVotes", userVotes);
        model.addAttribute("user", user);
        
        return "discussion/list";
    }
    
    /**
     * GET /discussion/{discussionId} - Detail thread
     */
    @GetMapping("/{discussionId}")
    public String viewDiscussion(@PathVariable Integer discussionId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Discussion discussion = discussionService.getDiscussionById(discussionId);
        if (discussion == null) {
            return "redirect:/";
        }
        
        Lesson lesson = discussion.getLesson();
        Course course = lesson.getSection().getCourse();
        List<DiscussionReply> replies = discussionService.getRepliesByDiscussion(discussionId);
        
        // Check user votes
        boolean hasVotedDiscussion = discussionService.hasUserVotedDiscussion(user.getUserId(), discussionId);
        Map<Integer, Boolean> replyVotes = new HashMap<>();
        for (DiscussionReply reply : replies) {
            replyVotes.put(reply.getReplyId(), discussionService.hasUserVotedReply(user.getUserId(), reply.getReplyId()));
        }
        
        // Check if user is owner or lecturer
        boolean isOwner = discussion.getUser().getUserId().equals(user.getUserId());
        boolean isLecturer = course.getLecturer().getUserId().equals(user.getUserId());
        
        model.addAttribute("discussion", discussion);
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("replies", replies);
        model.addAttribute("hasVotedDiscussion", hasVotedDiscussion);
        model.addAttribute("replyVotes", replyVotes);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isLecturer", isLecturer);
        model.addAttribute("user", user);
        
        return "discussion/detail";
    }
    
    /**
     * GET /discussion/create/{lessonId} - Form buat diskusi baru
     */
    @GetMapping("/create/{lessonId}")
    public String showCreateForm(@PathVariable Integer lessonId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return "redirect:/";
        }
        
        Course course = lesson.getSection().getCourse();
        
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        model.addAttribute("user", user);
        
        return "discussion/create";
    }
    
    /**
     * POST /discussion/create - Buat diskusi baru
     */
    @PostMapping("/create")
    public String createDiscussion(
            @RequestParam Integer lessonId,
            @RequestParam(required = false) String title,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            Discussion discussion = discussionService.createDiscussion(lessonId, user.getUserId(), title, content);
            redirectAttributes.addFlashAttribute("success", "Diskusi berhasil dibuat!");
            return "redirect:/discussion/" + discussion.getDiscussionId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/discussion/create/" + lessonId;
        }
    }
    
    /**
     * POST /discussion/{id}/reply - Balas diskusi
     */
    @PostMapping("/{discussionId}/reply")
    public String createReply(
            @PathVariable Integer discussionId,
            @RequestParam String content,
            @RequestParam(required = false) Integer parentReplyId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            discussionService.createReply(discussionId, user.getUserId(), content, parentReplyId);
            redirectAttributes.addFlashAttribute("success", "Balasan berhasil ditambahkan!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/discussion/" + discussionId;
    }
    
    /**
     * POST /discussion/{id}/upvote - Upvote (AJAX)
     */
    @PostMapping("/{discussionId}/upvote")
    @ResponseBody
    public Map<String, Object> upvoteDiscussion(@PathVariable Integer discussionId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }
        
        try {
            Discussion discussion = discussionService.upvoteDiscussion(discussionId, user.getUserId());
            boolean hasVoted = discussionService.hasUserVotedDiscussion(user.getUserId(), discussionId);
            
            response.put("success", true);
            response.put("upvoteCount", discussion.getUpvoteCount());
            response.put("hasVoted", hasVoted);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * POST /discussion/reply/{replyId}/upvote - Upvote reply (AJAX)
     */
    @PostMapping("/reply/{replyId}/upvote")
    @ResponseBody
    public Map<String, Object> upvoteReply(@PathVariable Integer replyId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }
        
        try {
            DiscussionReply reply = discussionService.upvoteReply(replyId, user.getUserId());
            boolean hasVoted = discussionService.hasUserVotedReply(user.getUserId(), replyId);
            
            response.put("success", true);
            response.put("upvoteCount", reply.getUpvoteCount());
            response.put("hasVoted", hasVoted);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * POST /discussion/{id}/resolve - Mark as resolved
     */
    @PostMapping("/{discussionId}/resolve")
    public String resolveDiscussion(
            @PathVariable Integer discussionId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            discussionService.markAsResolved(discussionId, user.getUserId());
            redirectAttributes.addFlashAttribute("success", "Diskusi ditandai sebagai selesai!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/discussion/" + discussionId;
    }
    
    /**
     * POST /discussion/reply/{replyId}/best - Mark as best answer
     */
    @PostMapping("/reply/{replyId}/best")
    public String markBestAnswer(
            @PathVariable Integer replyId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            DiscussionReply reply = discussionService.markAsBestAnswer(replyId, user.getUserId());
            redirectAttributes.addFlashAttribute("success", "Jawaban terbaik ditandai!");
            return "redirect:/discussion/" + reply.getDiscussion().getDiscussionId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }
    
    /**
     * DELETE /discussion/{id} - Hapus diskusi
     */
    @PostMapping("/{discussionId}/delete")
    public String deleteDiscussion(
            @PathVariable Integer discussionId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            Discussion discussion = discussionService.getDiscussionById(discussionId);
            Integer lessonId = discussion.getLesson().getLessonId();
            
            discussionService.deleteDiscussion(discussionId, user.getUserId());
            redirectAttributes.addFlashAttribute("success", "Diskusi berhasil dihapus!");
            return "redirect:/discussion/lesson/" + lessonId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/discussion/" + discussionId;
        }
    }
    
    /**
     * POST /discussion/reply/{replyId}/delete - Hapus reply
     */
    @PostMapping("/reply/{replyId}/delete")
    public String deleteReply(
            @PathVariable Integer replyId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            DiscussionReply reply = discussionService.getReplyById(replyId);
            Integer discussionId = reply.getDiscussion().getDiscussionId();
            
            discussionService.deleteReply(replyId, user.getUserId());
            redirectAttributes.addFlashAttribute("success", "Balasan berhasil dihapus!");
            return "redirect:/discussion/" + discussionId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }
}
