package gutsandgun.kite_sendEmail.dto.log;

import gutsandgun.kite_sendEmail.dto.sendMsg.SendMsgProceessingDTO;
import lombok.Data;

import java.util.Date;

@Data
public class MissingSendingIdLogDTO {
    String Service = "Send";
    String type = "missingSendingId";

    Long sendingId;
    //FailReason failReason;
    Long brokerId;
    Long TXId;
    Long time = new Date().getTime();

    public MissingSendingIdLogDTO(SendMsgProceessingDTO sendMsgProceessingDTO){
        this.sendingId = sendMsgProceessingDTO.getSendingId();
        this.brokerId = sendMsgProceessingDTO.getBrokerId();
        this.TXId = sendMsgProceessingDTO.getTxId();
    }

    @Override
    public String toString() {
        return  "Service='" + Service +
                ", type='" + type +
                ", sendingId=" + sendingId +
                ", brokerId=" + brokerId +
                ", TXId=" + TXId +
                ", time=" + time;
    }
}
