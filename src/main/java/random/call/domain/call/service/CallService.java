package random.call.domain.call.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import random.call.domain.call.CallParticipant;
import random.call.domain.call.CallRoom;
import random.call.domain.call.repository.CallParticipantRepository;
import random.call.domain.call.repository.CallRoomRepository;
import random.call.domain.member.Member;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallService {

    private final CallRoomRepository callRoomRepository;
    private final CallParticipantRepository callParticipantRepository;


    public void deleteAllCallRoom(Member member){
        List<CallParticipant> callParticipants =callParticipantRepository.findByMember(member);
        List<CallRoom> callRoomList=callParticipants.stream().map(CallParticipant::getCallRoom).toList();
        
        callRoomRepository.deleteAll(callRoomList);
        
        callParticipantRepository.deleteAll(callParticipants);
        log.info("{} 회원의 좋아요 통화내역 프로세스 완료",member.getId());


    }
}
