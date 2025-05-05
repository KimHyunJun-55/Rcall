package random.call.domain.friendList;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRepository extends JpaRepository<Friend,Long> {
    boolean existsByMemberAAndMemberB(Long minId, Long maxId);
}
