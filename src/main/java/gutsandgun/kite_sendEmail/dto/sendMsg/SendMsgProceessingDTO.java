package gutsandgun.kite_sendEmail.dto.sendMsg;

import gutsandgun.kite_sendEmail.dto.SendingDto;
import gutsandgun.kite_sendEmail.type.SendingType;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Data
@Getter
public class SendMsgProceessingDTO {
    private static final Logger log = LoggerFactory.getLogger(SendMsgProceessingDTO.class);
    Long brokerId;
    Long sendingId;
    Long txId;

    SendingType sendingType;

    SendManagerMsgDTO sendManagerMsgDTO;
    SendingDto sendingDto;
    BrokerMsgDTO brokerMsgDTO;

    public SendMsgProceessingDTO(Long brokerId, SendManagerMsgDTO sendManagerMsgDTO) {
        this.brokerId = brokerId;
        this.txId = sendManagerMsgDTO.getId();
        this.sendingId = sendManagerMsgDTO.getSendingId();
        this.sendManagerMsgDTO = sendManagerMsgDTO;
    }

    public void setSendingDto(SendingDto sendingDto) {
        this.sendingDto = sendingDto;
        this.sendingType = sendingDto.getSendingType();
    }

    public void setBrokerMsgDTO() {
        //1.문자열 치환
        //지금은 이름만 message의 %고객명% 부분에 name넣기
        String content = sendingDto.getContent();
        log.info("3. Message: {}",content);
        content = content.replace("%고객명%", sendManagerMsgDTO.getName());
        log.info("문자열 치환: {}",content);

        //2.초과 처리
        //sms일때 sms bytes 수 > 80
        if(sendingDto.getSendingType() == SendingType.SMS) {
            content = sliceMsg(content,160);
        }
        //mms일때 lms bytes 수 > 2000
        if(sendingDto.getSendingType()==SendingType.MMS){
            content = sliceMsg(content,2000);
        }
        log.info("초과처리: {}",content);

        //3. broker msg 만들기
        this.brokerMsgDTO = new BrokerMsgDTO(content,sendingDto,sendManagerMsgDTO);
    }
        private String sliceMsg(String msg,int byteLength){
            int bytes = 0;
            if (msg == null) {
                return msg;
            } else {
                char[] strChar = msg.toCharArray();

                int charLength = 0;
                for (int i = 0; i < strChar.length; i++) {
                    int code = strChar[i];

                    // 2bytes
                    if (code >= 128) bytes += 2;
                        // 1bytes
                    else bytes +=1;

                    if(bytes < byteLength){
                        charLength += 1;
                    }
                    else{
                        log.info("문자 자리수: {}",charLength);
                        log.info("문자 bytes: {}",bytes);
                        return msg.substring(0,charLength);
                    }
                }
                log.info("문자 자리수: {}",charLength);
                log.info("문자 bytes: {}",bytes);
                return msg;
            }
        }

}
