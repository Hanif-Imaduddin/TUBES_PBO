package koding_muda_nusantara.koding_muda_belajar.service;

import koding_muda_nusantara.koding_muda_belajar.model.*;
import koding_muda_nusantara.koding_muda_belajar.repository.*;
import koding_muda_nusantara.koding_muda_belajar.enums.VoteType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DiscussionService {
    
    @Autowired
    private DiscussionRepository discussionRepository;
    
    @Autowired
    private DiscussionReplyRepository replyRepository;
    
    @Autowired
    private DiscussionVoteRepository voteRepository;
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ==================== DISCUSSION METHODS ====================
    
    public List<Discussion> getDiscussionsByLesson(Integer lessonId) {
        return discussionRepository.findByLessonLessonIdOrderByIsPinnedDescCreatedAtDesc(lessonId);
    }
    
    public List<Discussion> getUnresolvedDiscussions(Integer lessonId) {
        return discussionRepository.findByLessonLessonIdAndIsResolvedFalseOrderByCreatedAtDesc(lessonId);
    }
    
    public Discussion getDiscussionById(Integer discussionId) {
        return discussionRepository.findById(discussionId).orElse(null);
    }
    
    @Transactional
    public Discussion createDiscussion(Integer lessonId, Integer userId, String title, String content) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson tidak ditemukan"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        
        Discussion discussion = new Discussion();
        discussion.setLesson(lesson);
        discussion.setUser(user);
        discussion.setTitle(title);
        discussion.setContent(content);
        discussion.setIsPinned(false);
        discussion.setIsResolved(false);
        discussion.setUpvoteCount(0);
        discussion.setReplyCount(0);
        
        return discussionRepository.save(discussion);
    }
    
    @Transactional
    public void deleteDiscussion(Integer discussionId, Integer userId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Diskusi tidak ditemukan"));
        
        // Cek apakah user adalah pembuat diskusi atau lecturer dari course
        boolean isOwner = discussion.getUser().getUserId().equals(userId);
        boolean isLecturer = discussion.getLesson().getSection().getCourse().getLecturer().getUserId().equals(userId);
        
        if (!isOwner && !isLecturer) {
            throw new RuntimeException("Anda tidak memiliki izin untuk menghapus diskusi ini");
        }
        
        discussionRepository.delete(discussion);
    }
    
    @Transactional
    public Discussion markAsResolved(Integer discussionId, Integer userId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Diskusi tidak ditemukan"));
        
        // Hanya pembuat diskusi atau lecturer yang bisa mark as resolved
        boolean isOwner = discussion.getUser().getUserId().equals(userId);
        boolean isLecturer = discussion.getLesson().getSection().getCourse().getLecturer().getUserId().equals(userId);
        
        if (!isOwner && !isLecturer) {
            throw new RuntimeException("Anda tidak memiliki izin untuk menandai diskusi ini sebagai selesai");
        }
        
        discussion.setIsResolved(true);
        discussion.setUpdatedAt(LocalDateTime.now());
        
        return discussionRepository.save(discussion);
    }
    
    @Transactional
    public Discussion pinDiscussion(Integer discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Diskusi tidak ditemukan"));
        
        discussion.setIsPinned(!discussion.getIsPinned());
        discussion.setUpdatedAt(LocalDateTime.now());
        
        return discussionRepository.save(discussion);
    }
    
    public long getDiscussionCountByLesson(Integer lessonId) {
        return discussionRepository.countByLessonLessonId(lessonId);
    }
    
    // ==================== REPLY METHODS ====================
    
    public List<DiscussionReply> getRepliesByDiscussion(Integer discussionId) {
        return replyRepository.findByDiscussionDiscussionIdAndParentReplyIsNullOrderByCreatedAtAsc(discussionId);
    }
    
    public DiscussionReply getReplyById(Integer replyId) {
        return replyRepository.findById(replyId).orElse(null);
    }
    
    @Transactional
    public DiscussionReply createReply(Integer discussionId, Integer userId, String content, Integer parentReplyId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Diskusi tidak ditemukan"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        
        DiscussionReply reply = new DiscussionReply();
        reply.setDiscussion(discussion);
        reply.setUser(user);
        reply.setContent(content);
        reply.setIsBestAnswer(false);
        reply.setUpvoteCount(0);
        
        // Set parent reply jika ada (untuk nested reply)
        if (parentReplyId != null) {
            DiscussionReply parentReply = replyRepository.findById(parentReplyId)
                    .orElseThrow(() -> new RuntimeException("Parent reply tidak ditemukan"));
            reply.setParentReply(parentReply);
        }
        
        reply = replyRepository.save(reply);
        
        // Update reply count di discussion
        discussion.setReplyCount(discussion.getReplyCount() + 1);
        discussionRepository.save(discussion);
        
        return reply;
    }
    
    @Transactional
    public void deleteReply(Integer replyId, Integer userId) {
        DiscussionReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply tidak ditemukan"));
        
        // Cek apakah user adalah pembuat reply atau lecturer dari course
        boolean isOwner = reply.getUser().getUserId().equals(userId);
        boolean isLecturer = reply.getDiscussion().getLesson().getSection().getCourse().getLecturer().getUserId().equals(userId);
        
        if (!isOwner && !isLecturer) {
            throw new RuntimeException("Anda tidak memiliki izin untuk menghapus balasan ini");
        }
        
        Discussion discussion = reply.getDiscussion();
        
        // Hitung jumlah reply yang akan dihapus (termasuk child replies)
        long childCount = countChildReplies(replyId);
        
        replyRepository.delete(reply);
        
        // Update reply count di discussion
        discussion.setReplyCount(Math.max(0, discussion.getReplyCount() - (int)(1 + childCount)));
        discussionRepository.save(discussion);
    }
    
    private long countChildReplies(Integer parentReplyId) {
        List<DiscussionReply> children = replyRepository.findByParentReplyReplyIdOrderByCreatedAtAsc(parentReplyId);
        long count = children.size();
        for (DiscussionReply child : children) {
            count += countChildReplies(child.getReplyId());
        }
        return count;
    }
    
    @Transactional
    public DiscussionReply markAsBestAnswer(Integer replyId, Integer userId) {
        DiscussionReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply tidak ditemukan"));
        
        Discussion discussion = reply.getDiscussion();
        
        // Hanya pembuat diskusi atau lecturer yang bisa mark as best answer
        boolean isOwner = discussion.getUser().getUserId().equals(userId);
        boolean isLecturer = discussion.getLesson().getSection().getCourse().getLecturer().getUserId().equals(userId);
        
        if (!isOwner && !isLecturer) {
            throw new RuntimeException("Anda tidak memiliki izin untuk menandai jawaban terbaik");
        }
        
        // Hapus best answer lama jika ada
        Optional<DiscussionReply> existingBest = replyRepository.findByDiscussionDiscussionIdAndIsBestAnswerTrue(discussion.getDiscussionId());
        if (existingBest.isPresent()) {
            DiscussionReply oldBest = existingBest.get();
            oldBest.setIsBestAnswer(false);
            replyRepository.save(oldBest);
        }
        
        // Set reply ini sebagai best answer
        reply.setIsBestAnswer(true);
        reply.setUpdatedAt(LocalDateTime.now());
        
        // Mark discussion as resolved
        discussion.setIsResolved(true);
        discussionRepository.save(discussion);
        
        return replyRepository.save(reply);
    }
    
    // ==================== VOTE METHODS ====================
    
    @Transactional
    public Discussion upvoteDiscussion(Integer discussionId, Integer userId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Diskusi tidak ditemukan"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        
        // Cek apakah sudah pernah vote
        Optional<DiscussionVote> existingVote = voteRepository.findByUserUserIdAndDiscussionDiscussionId(userId, discussionId);
        
        if (existingVote.isPresent()) {
            // Jika sudah upvote, hapus vote (toggle)
            voteRepository.delete(existingVote.get());
            discussion.setUpvoteCount(Math.max(0, discussion.getUpvoteCount() - 1));
        } else {
            // Buat vote baru
            DiscussionVote vote = new DiscussionVote();
            vote.setUser(user);
            vote.setDiscussion(discussion);
            vote.setVoteType(VoteType.UPVOTE);
            voteRepository.save(vote);
            discussion.setUpvoteCount(discussion.getUpvoteCount() + 1);
        }
        
        return discussionRepository.save(discussion);
    }
    
    @Transactional
    public DiscussionReply upvoteReply(Integer replyId, Integer userId) {
        DiscussionReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply tidak ditemukan"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        
        // Cek apakah sudah pernah vote
        Optional<DiscussionVote> existingVote = voteRepository.findByUserUserIdAndReplyReplyId(userId, replyId);
        
        if (existingVote.isPresent()) {
            // Jika sudah upvote, hapus vote (toggle)
            voteRepository.delete(existingVote.get());
            reply.setUpvoteCount(Math.max(0, reply.getUpvoteCount() - 1));
        } else {
            // Buat vote baru
            DiscussionVote vote = new DiscussionVote();
            vote.setUser(user);
            vote.setReply(reply);
            vote.setVoteType(VoteType.UPVOTE);
            voteRepository.save(vote);
            reply.setUpvoteCount(reply.getUpvoteCount() + 1);
        }
        
        return replyRepository.save(reply);
    }
    
    @Transactional
    public void removeDiscussionVote(Integer discussionId, Integer userId) {
        Optional<DiscussionVote> vote = voteRepository.findByUserUserIdAndDiscussionDiscussionId(userId, discussionId);
        if (vote.isPresent()) {
            Discussion discussion = vote.get().getDiscussion();
            voteRepository.delete(vote.get());
            discussion.setUpvoteCount(Math.max(0, discussion.getUpvoteCount() - 1));
            discussionRepository.save(discussion);
        }
    }
    
    @Transactional
    public void removeReplyVote(Integer replyId, Integer userId) {
        Optional<DiscussionVote> vote = voteRepository.findByUserUserIdAndReplyReplyId(userId, replyId);
        if (vote.isPresent()) {
            DiscussionReply reply = vote.get().getReply();
            voteRepository.delete(vote.get());
            reply.setUpvoteCount(Math.max(0, reply.getUpvoteCount() - 1));
            replyRepository.save(reply);
        }
    }
    
    public boolean hasUserVotedDiscussion(Integer userId, Integer discussionId) {
        return voteRepository.existsByUserUserIdAndDiscussionDiscussionId(userId, discussionId);
    }
    
    public boolean hasUserVotedReply(Integer userId, Integer replyId) {
        return voteRepository.existsByUserUserIdAndReplyReplyId(userId, replyId);
    }
}
