package gutsandgun.kite_sendEmail.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import gutsandgun.kite_sendEmail.entity.read.Sending;
import gutsandgun.kite_sendEmail.type.SendingRuleType;
import gutsandgun.kite_sendEmail.type.SendingType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
public class SendingDto implements Serializable {
    private Long id;

    private String userId;

    private SendingRuleType sendingRuleType; // 중계사 규칙 타입

    private SendingType sendingType;        // 발송 타입

    private String replaceYn;               // 대체발송 여부

    private Long totalMessage;              // 메세지 갯수

    private Long inputTime;               // 등록 시간

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservationTime;  // 예약 시간

    private Long scheduleTime;              // 예약 시간

    private String title;                   // 제목

    private String mediaLink;               // 미디어 링크

    private String content;                 // 메세지 내용

    private String sender;                  // 발신자

    private String regId;

    private String modId;


    public SendingDto(Sending sending){
        this.id = sending.getId();
        this.userId = sending.getUserId();
        this.sendingRuleType = sending.getSendingRuleType();
        this.sendingType = sending.getSendingType();
        this.replaceYn = sending.getReplaceYn();
        this.totalMessage = sending.getTotalMessage();
        this.title = sending.getTitle();
        this.mediaLink = sending.getMediaLink();
        this.content = sending.getContent();
    }


}