package random.call.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT m FROM Member m WHERE m.id IN :ids")
    List<Member> findMembersByIdIn(@Param("ids") List<Long> ids);

    Optional<Member> findByPhoneNumber(String number);
}
