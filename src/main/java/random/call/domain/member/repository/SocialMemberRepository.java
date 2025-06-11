package random.call.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.member.SocialMember;

import java.util.Optional;

public interface SocialMemberRepository extends JpaRepository<SocialMember,Long> {
    Optional<SocialMember> findBySocialId(String socialId);

    boolean existsBySocialId(String socialId);
}
