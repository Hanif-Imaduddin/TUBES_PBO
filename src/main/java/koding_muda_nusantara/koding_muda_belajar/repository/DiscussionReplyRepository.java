package koding_muda_nusantara.koding_muda_belajar.repository;

import koding_muda_nusantara.koding_muda_belajar.model.DiscussionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Integer> {
    
    List<DiscussionReply> findByDiscussionDiscussionIdAndParentReplyIsNullOrderByCreatedAtAsc(Integer discussionId);
    
    List<DiscussionReply> findByDiscussionDiscussionIdOrderByCreatedAtAsc(Integer discussionId);
    
    List<DiscussionReply> findByParentReplyReplyIdOrderByCreatedAtAsc(Integer parentReplyId);
    
    List<DiscussionReply> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    
    Optional<DiscussionReply> findByDiscussionDiscussionIdAndIsBestAnswerTrue(Integer discussionId);
    
    long countByDiscussionDiscussionId(Integer discussionId);
}
