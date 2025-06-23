package random.call.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.member.RequiredConsent;

public interface RequiredConsentRepository extends JpaRepository<RequiredConsent,Long> {
    void deleteByMemberId(Long id);
}
