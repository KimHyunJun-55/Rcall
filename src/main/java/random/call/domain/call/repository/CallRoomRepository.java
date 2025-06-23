package random.call.domain.call.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.call.CallRoom;
import random.call.domain.member.Member;

import java.util.List;

public interface CallRoomRepository extends JpaRepository<CallRoom,Long> {
}
