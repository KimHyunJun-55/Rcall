package random.call.domain.call.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.call.CallParticipant;
import random.call.domain.member.Member;

import java.util.List;

public interface CallParticipantRepository extends JpaRepository<CallParticipant,Long> {
    List<CallParticipant> findByMember(Member member);
}
