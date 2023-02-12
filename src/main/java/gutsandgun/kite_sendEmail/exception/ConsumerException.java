package gutsandgun.kite_sendEmail.exception;

//rabbit mq consumer에서만 사용하는 오류
public class ConsumerException extends RuntimeException{
    public static final String ERROR_DB = "ERROR_DB";

    public ConsumerException(String message) {
        super(message);
    }
}
