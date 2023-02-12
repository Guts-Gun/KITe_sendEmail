package gutsandgun.kite_sendEmail.dto.log;

import gutsandgun.kite_sendEmail.dto.sendMsg.SendMsgProceessingDTO;
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

    Long time = new Date().getTime();

    String content;

    //객체 용
    public BrokerRequestLogDTO( Long brokerId,SendMsgProceessingDTO sendMsgProceessingDTO){
        this.brokerId = brokerId;

        this.sendingId = sendMsgProceessingDTO.getSendingId();
        this.sendingType = sendMsgProceessingDTO.getSendingType();
        this.TXId = sendMsgProceessingDTO.getTxId();
        this.content = sendMsgProceessingDTO.getBrokerMsgDTO().getContent();
    }

    @Override
    public String toString() {
        return "Service=" + Service +
                ", type=" + type +
                ", sendingId=" + sendingId +
                ", sendingType=" + sendingType +
                ", brokerId=" + brokerId +
                ", TXId=" + TXId +
                ", time=" + time;
    }
}
