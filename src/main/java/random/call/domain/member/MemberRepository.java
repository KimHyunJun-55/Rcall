package random.call.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
//    Optional<Member> findByEmail(@Param("email") String email);

    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);

//    Optional<Member> findBySocialId(String username);

    Optional<Object> findByNickname(String nickname);

    Optional<Member> findByUsername(String username);

}
