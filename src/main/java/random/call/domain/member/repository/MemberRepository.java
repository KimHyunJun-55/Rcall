package random.call.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
//    Optional<Member> findByEmail(@Param("email") String email);

    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);

//    Optional<Member> findBySocialId(String username);

    Optional<Object> findByNickname(String nickname);

    Optional<Member> findByUsername(String username);

    List<String> findInterestsById(Long userId);
}
