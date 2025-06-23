package random.call.global.sms;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Balance;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final DefaultMessageService messageService;

    public MessageService(
            @Value("${coolsms.api.key}") String apiKey,
            @Value("${coolsms.api.secret}") String apiSecret,
            @Value("${coolsms.api.url}") String apiUrl) {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, apiUrl);
    }

    // 단일 메시지 발송
    public SingleMessageSentResponse sendSms(String from, String to, String text) {
        Message message = new Message();
        message.setFrom(from);
        message.setTo(to);
        message.setText(text);

        return messageService.sendOne(new SingleMessageSendingRequest(message));
    }


    // 잔액 조회
    public Balance getBalance() {
        return messageService.getBalance();
    }
}