package random.call.domain.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Likes, Long> {
    Optional<Likes> findByMemberIdAndFeedId(Long memberId, Long feedId);

    boolean existsByMemberIdAndFeedIdAndIsLikeTrue(Long memberId, Long id);

    boolean existsByMemberIdAndFeedId(Long memberId, Long id);
}