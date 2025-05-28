package random.call.domain.reply.dto;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.reply.Reply;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    Page<Reply> findByFeedIdAndIsDeletedFalseOrderByCreatedAtAsc(Long feedId, Pageable pageable);

    Page<Reply> findByFeedIdAndIsDeletedFalseOrderByCreatedAtDesc(Long feedId, Pageable pageable);
}

