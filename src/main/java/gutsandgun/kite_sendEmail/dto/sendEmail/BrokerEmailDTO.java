package gutsandgun.kite_sendEmail.dto.sendEmail;

import gutsandgun.kite_sendEmail.dto.SendingDto;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class BrokerEmailDTO {
    private String sender;
    private String receiver;
    private String title;
    private String content;
    private String mediaLink;

    public BrokerEmailDTO(String content, SendingDto sendingDto, SendManagerEmailDTO sendManagerEmailDTO){
        this.sender = sendManagerEmailDTO.getSender();
        this.receiver = sendManagerEmailDTO.getReceiver();
        this.title = sendingDto.getTitle();
        //CONTENT는 문자열 치환후!
        this.content = content;
        this.mediaLink = sendingDto.getMediaLink();
    }
}
