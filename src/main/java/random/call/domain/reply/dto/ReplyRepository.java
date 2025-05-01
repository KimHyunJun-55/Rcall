package random.call.domain.reply.dto;


import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.reply.Reply;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByFeedIdAndIsDeletedFalseOrderByCreatedAtAsc(Long feedId);
}

