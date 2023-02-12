package gutsandgun.kite_sendEmail.dto.sendMsg;

import lombok.*;

@Getter
@Setter
@ToString
@Data
@AllArgsConstructor
public class ReplaceSendingBodyDTO {
    Long sendingId;
    Long txId;
}
