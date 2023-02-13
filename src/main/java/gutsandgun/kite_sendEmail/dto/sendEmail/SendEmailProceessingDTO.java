package gutsandgun.kite_sendEmail.dto.sendEmail;

import gutsandgun.kite_sendEmail.dto.SendingDto;
import gutsandgun.kite_sendEmail.type.SendingType;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Data
@Getter
public class SendEmailProceessingDTO {
    private static final Logger log = LoggerFactory.getLogger(SendEmailProceessingDTO.class);
    Long brokerId;
    Long sendingId;
    Long txId;

    SendingType sendingType;

    SendManagerEmailDTO sendManagerEmailDTO;
    SendingDto sendingDto;
    BrokerEmailDTO brokerEmailDTO;

    public SendEmailProceessingDTO(Long brokerId, SendManagerEmailDTO sendManagerEmailDTO) {
        this.brokerId = brokerId;
        this.txId = sendManagerEmailDTO.getId();
        this.sendingId = sendManagerEmailDTO.getSendingId();
        this.sendManagerEmailDTO = sendManagerEmailDTO;
    }

    public void setSendingDto(SendingDto sendingDto) {
        this.sendingDto = sendingDto;
        this.sendingType = sendingDto.getSendingType();
    }

    public void setBrokerEmailDTO() {
        //1.문자열 치환
        //지금은 이름만 message의 %고객명% 부분에 name넣기
        String content = sendingDto.getContent();
        log.info("3. Message: {}",content);
        if(sendManagerEmailDTO.getName()!=null){
            content = content.replace("%고객명%", sendManagerEmailDTO.getName());
        }
        log.info("문자열 치환: {}",content);

        //2. broker email 만들기
        this.brokerEmailDTO = new BrokerEmailDTO(content,sendingDto, sendManagerEmailDTO);
    }


}
