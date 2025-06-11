package random.call.domain.call.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.call.CallParticipant;

public interface CallParticipantRepository extends JpaRepository<CallParticipant,Long> {
}
