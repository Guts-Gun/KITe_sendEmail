package gutsandgun.kite_sendEmail.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    //msg / email
    BAD_REQUEST(400, "S000", "잘못된 요청입니다."),
    //msg
    INVALID_PHONE(400, "ERR404", "잘못된 전화번호입니다.");

    private int status;
    private String code;
    private String message;

}
