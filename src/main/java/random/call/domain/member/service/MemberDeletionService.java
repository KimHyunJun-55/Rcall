package random.call.domain.member.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.call.service.CallService;
import random.call.domain.chat.ChatService;
import random.call.domain.feed.service.FeedService;
import random.call.domain.friendList.service.FriendService;
import random.call.domain.like.LikeService;
import random.call.domain.member.DeleteMemberInfo;
import random.call.domain.member.Member;
import random.call.domain.member.repository.DeleteMemberRepository;
import random.call.domain.member.repository.MemberRepository;
import random.call.domain.member.repository.RequiredConsentRepository;
import random.call.domain.reply.service.ReplyService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberDeletionService {

    private final ChatService chatService;
    private final CallService callService;
    private final FeedService feedService;
    private final ReplyService replyService;
    private final LikeService likeService;
    private final FriendService friendService;

    private final MemberRepository memberRepository;
    private final DeleteMemberRepository deleteMemberRepository;
    private final RequiredConsentRepository requiredConsentRepository;

    @Transactional
    public void withdrawnMember(Member member) {
        log.info("[username: {} memberId: {}] 멤버 회원탈퇴 프로세스 시작", member.getUsername(), member.getId());

        try {
            friendService.deleteFriendList(member);
        } catch (Exception e) {
            log.warn("[탈퇴 오류] 친구 삭제 실패", e);
        }

        try {
            chatService.deleteAllChat(member);
        } catch (Exception e) {
            log.warn("[탈퇴 오류] 채팅 삭제 실패", e);
        }

        try {
            callService.deleteAllCallRoom(member);
        } catch (Exception e) {
            log.warn("[탈퇴 오류] 통화 기록 삭제 실패", e);
        }

        try {
            likeService.deleteLikes(member);
            replyService.deleteAllReply(member);
            feedService.deleteAllFeed(member);
        } catch (Exception e) {
            log.warn("[탈퇴 오류] 피드 관련 삭제 실패", e);
        }


        try {
            DeleteMemberInfo deleteMemberInfo = DeleteMemberInfo.builder()
                    .encryptedId(member.getUsername())
                    .encryptedDeviceId(member.getDeviceInfo().getDeviceId())
                    .encryptedBirth(member.getBirthDate())
                    .deletedAt(LocalDateTime.now())
                    .build();

            deleteMemberRepository.save(deleteMemberInfo);
        } catch (Exception e) {
            log.warn("[탈퇴 오류] 삭제 정보 저장 실패", e);
        }

        try {
            requiredConsentRepository.deleteByMemberId(member.getId());
            memberRepository.delete(member);
        } catch (Exception e) {
            log.warn("[탈퇴 오류] Member 삭제 실패", e);
            throw e; // 이건 반드시 실패해야 함
        }

        log.info("[username: {} memberId: {}] 멤버 회원탈퇴 프로세스 종료", member.getUsername(), member.getId());
    }

}
