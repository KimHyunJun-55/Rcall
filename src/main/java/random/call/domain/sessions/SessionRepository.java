package random.call.domain.sessions;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session,Long> {
}
