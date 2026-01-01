package koding_muda_nusantara.koding_muda_belajar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import koding_muda_nusantara.koding_muda_belajar.enums.VoteType;

import java.time.LocalDateTime;

@Entity
@Table(name = "discussion_votes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionVote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Integer voteId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id")
    private Discussion discussion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id")
    private DiscussionReply reply;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type")
    private VoteType voteType = VoteType.UPVOTE;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
