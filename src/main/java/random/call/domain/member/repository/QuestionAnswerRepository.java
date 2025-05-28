package random.call.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.member.Member;
import random.call.domain.member.QuestionAnswer;

import java.util.List;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer,Long> {

    List<QuestionAnswer> findByMember(Member member);

    void deleteByMember(Member member);
}
