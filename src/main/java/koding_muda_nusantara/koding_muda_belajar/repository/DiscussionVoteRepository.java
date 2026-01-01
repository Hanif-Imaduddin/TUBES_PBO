package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.DiscussionVote;
import koding_muda_nusantara.koding_muda_belajar.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscussionVoteRepository extends JpaRepository<DiscussionVote, Integer> {
    
    Optional<DiscussionVote> findByUserUserIdAndDiscussionDiscussionId(Integer userId, Integer discussionId);
    
    Optional<DiscussionVote> findByUserUserIdAndReplyReplyId(Integer userId, Integer replyId);
    
    boolean existsByUserUserIdAndDiscussionDiscussionId(Integer userId, Integer discussionId);
    
    boolean existsByUserUserIdAndReplyReplyId(Integer userId, Integer replyId);
    
    long countByDiscussionDiscussionIdAndVoteType(Integer discussionId, VoteType voteType);
    
    long countByReplyReplyIdAndVoteType(Integer replyId, VoteType voteType);
    
    void deleteByUserUserIdAndDiscussionDiscussionId(Integer userId, Integer discussionId);
    
    void deleteByUserUserIdAndReplyReplyId(Integer userId, Integer replyId);
}
