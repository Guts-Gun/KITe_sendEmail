package gutsandgun.kite_sendEmail.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
//rabbit mq consumer에서만 사용하는 이메일 대체 전송용 body dto
public class BrokerResponseErrorDto {
    String code;
    String message;
}
