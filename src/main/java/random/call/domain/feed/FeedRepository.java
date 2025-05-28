package random.call.domain.feed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed,Long> {
    Page<Feed> findAll(Pageable pageable);


    @Query("SELECT f FROM Feed f WHERE f.writer.id =:memberId ")
    Page<Feed> findAllByMemberId(@Param("memberId")Long memberId, Pageable sortedPageable);

    Page<Feed> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    Page<Feed> findByIdLessThanOrderByIdDesc(Long id, Pageable pageable);
}
