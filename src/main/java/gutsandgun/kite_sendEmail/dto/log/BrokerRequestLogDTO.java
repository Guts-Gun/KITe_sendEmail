package gutsandgun.kite_sendEmail.dto.log;

import gutsandgun.kite_sendEmail.dto.sendEmail.SendEmailProceessingDTO;
import gutsandgun.kite_sendEmail.type.SendingType;
import lombok.Data;

import java.util.Date;

@Data
public class BrokerRequestLogDTO {
    String Service = "Send";
    String type = "sendBroker";

    Long sendingId;
    SendingType sendingType;
    Long brokerId;
    Long TXId;

    String sender;
    String receiver;
    String content;

    Long time = new Date().getTime();


    //객체 용
    public BrokerRequestLogDTO(Long brokerId, SendEmailProceessingDTO sendEmailProceessingDTO){
        this.brokerId = brokerId;

        this.sendingId = sendEmailProceessingDTO.getSendingId();
        this.sendingType = sendEmailProceessingDTO.getSendingType();
        this.TXId = sendEmailProceessingDTO.getTxId();
        this.sender = sendEmailProceessingDTO.getBrokerEmailDTO().getSender();
        this.receiver = sendEmailProceessingDTO.getBrokerEmailDTO().getReceiver();
        this.content = sendEmailProceessingDTO.getBrokerEmailDTO().getContent();
    }

    @Override
    public String toString() {
        return "Service: " + Service +
                ", type: " + type +
                ", sendingId: " + sendingId +
                ", sendingType: " + sendingType +
                ", brokerId: " + brokerId +
                ", TXId: " + TXId +
                ", sender: " + sender +
                ", receiver: " + receiver +
                ", content: " + content +
                ", time: " + time +
                "@";
    }
}
