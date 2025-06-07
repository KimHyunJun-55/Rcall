//package random.call.domain.sessions;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import random.call.global.agora.AgoraTokenService;
//
//import java.time.LocalDate;
//import java.util.UUID;
//@Service
//@RequiredArgsConstructor
//public class SessionService {
//
//    private final SessionRepository sessionRepository;
//    private final AgoraTokenService agoraTokenService;
//
//    public Session createSession(Long talkerId, Long listenerId) {
//        // 1. 채널 이름 생성 (예: session_20250429_12345)
//        String channelName = "session_" + LocalDate.now() + "_" + UUID.randomUUID().toString().substring(0, 6);
//
//        // 2. Agora 토큰 생성 (각 사용자용)
//        // Long 값을 int로 변환 (범위 체크 필요)
//        if (talkerId > Integer.MAX_VALUE || listenerId > Integer.MAX_VALUE) {
//            throw new IllegalArgumentException("User ID exceeds the max value for an int.");
//        }
//
//        int talkerIdInt = talkerId.intValue();
//        int listenerIdInt = listenerId.intValue();
//
////        String talkerToken = agoraTokenService.generateToken(channelName, talkerIdInt);
////        String listenerToken = agoraTokenService.generateToken(channelName, listenerIdInt);
//
//        // 3. Session 객체 생성 및 저장
//        Session session = new Session();
//        session.setTalkerId(talkerId);
//        session.setListenerId(listenerId);
//        session.setChannelName(channelName);
//        session.setTalkerToken(talkerToken);
//        session.setListenerToken(listenerToken);
//
//        sessionRepository.save(session);
//
//        return session;
//    }
//}
