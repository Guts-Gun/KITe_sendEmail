package gutsandgun.kite_sendEmail.dto;

import gutsandgun.kite_sendEmail.entity.read.Sending;
import gutsandgun.kite_sendEmail.type.SendingRuleType;
import gutsandgun.kite_sendEmail.type.SendingType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class SendingDto  {
    private Long id;
    private String userId;
    private SendingRuleType sendingRuleType;
    private SendingType sendingType;
    private String replaceYn;
    private Long totalSending;

    //private Long inputTime;
    //private Long scheduleTime;

    private String title;
    private String mediaLink;
    private String content;


    public SendingDto(Sending sending){
        this.id = sending.getId();
        this.userId = sending.getUserId();
        this.sendingRuleType = sending.getSendingRuleType();
        this.sendingType = sending.getSendingType();
        this.replaceYn = sending.getReplaceYn();
        this.totalSending = sending.getTotalMessage();
        this.title = sending.getTitle();
        this.mediaLink = sending.getMediaLink();
        this.content = sending.getContent();
    }

}