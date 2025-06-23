package random.call.domain.feed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.member.Member;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed,Long> {
    Page<Feed> findAll(Pageable pageable);


    @Query("SELECT f FROM Feed f WHERE f.writer.id =:memberId ")
    Page<Feed> findAllByMemberId(@Param("memberId")Long memberId, Pageable sortedPageable);



    @Query("SELECT f FROM Feed f " +
            "LEFT JOIN FETCH f.writer " +  // N+1 문제 방지
            "WHERE f.writer.id NOT IN :blockedMemberIds " +
            "ORDER BY f.createdAt DESC")
    Page<Feed> findAllExcludingBlockedMembers(
            @Param("blockedMemberIds") List<Long> blockedMemberIds,
            Pageable pageable
    );
    @Query("SELECT f FROM Feed f WHERE f.id > :id AND f.writer.id NOT IN :blockedMemberIds ORDER BY f.id ASC")
    Page<Feed> findByIdGreaterThanAndMemberNotInOrderByIdAsc(
            @Param("id") Long id,
            @Param("blockedMemberIds") List<Long> blockedMemberIds,
            Pageable pageable
    );

    // 차단된 사용자 제외 + feedId보다 작은 ID (최신 순)
    @Query("SELECT f FROM Feed f WHERE f.id < :id AND f.writer.id NOT IN :blockedMemberIds ORDER BY f.id DESC")
    Page<Feed> findByIdLessThanAndMemberNotInOrderByIdDesc(
            @Param("id") Long id,
            @Param("blockedMemberIds") List<Long> blockedMemberIds,
            Pageable pageable
    );

    List<Feed> findByWriter(Member member);
}
