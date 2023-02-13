package gutsandgun.kite_sendEmail.dto.log;

import gutsandgun.kite_sendEmail.dto.sendEmail.SendEmailProceessingDTO;
import gutsandgun.kite_sendEmail.type.FailReason;
import gutsandgun.kite_sendEmail.type.SendingStatus;
import gutsandgun.kite_sendEmail.type.SendingType;
import lombok.Data;

import java.util.Date;

@Data
public class BrokerResponseLogDTO {
    String Service = "Send";
    String type = "receiveBroker";

    SendingStatus success;
    FailReason failReason;

    Long sendingId;
    SendingType sendingType;
    Long brokerId;
    Long TXId;

    Long time = new Date().getTime();

    //객체 용
    public BrokerResponseLogDTO( Long brokerId, SendingStatus success, SendEmailProceessingDTO sendEmailProceessingDTO){
        this.success = success;
        this.brokerId = brokerId;

        this.sendingId = sendEmailProceessingDTO.getSendingId();
        this.sendingType = sendEmailProceessingDTO.getSendingType();
        this.TXId = sendEmailProceessingDTO.getTxId();
    }

    public void setFailReason(FailReason failReason) {
        this.failReason = failReason;
    }

    @Override
    public String toString() {
        return  "Service=" + Service +
                ", type=" + type +
                ", success=" + success +
                ", failReason=" + failReason +
                ", sendingId=" + sendingId +
                ", sendingType=" + sendingType +
                ", brokerId=" + brokerId +
                ", TXId=" + TXId +
                ", time=" + time;
    }



}



