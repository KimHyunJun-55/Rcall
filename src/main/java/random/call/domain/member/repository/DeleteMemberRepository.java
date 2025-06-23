package random.call.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.member.DeleteMemberInfo;

public interface DeleteMemberRepository extends JpaRepository<DeleteMemberInfo,Long> {
}
