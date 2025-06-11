package random.call.domain.call.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.call.CallRoom;

public interface CallRoomRepository extends JpaRepository<CallRoom,Long> {
}
