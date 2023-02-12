package gutsandgun.kite_sendEmail.dto.sendMsg;

import gutsandgun.kite_sendEmail.dto.SendingDto;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class BrokerMsgDTO {
    private String sender;
    private String receiver;
    private String title;
    private String content;
    private String mediaLink;

    public BrokerMsgDTO(String content,SendingDto sendingDto, SendManagerMsgDTO sendManagerMsgDTO){
        this.sender = sendManagerMsgDTO.getSender();
        this.receiver = sendManagerMsgDTO.getReceiver();
        this.title = sendingDto.getTitle();
        //CONTENT는 문자열 치환후!
        this.content = content;
        this.mediaLink = sendingDto.getMediaLink();
    }
}
