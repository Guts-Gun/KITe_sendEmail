package gutsandgun.kite_sendEmail.feignClients.sending;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeignSendingError implements ErrorDecoder {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Exception decode(String s, Response response) {
        switch (response.status()) {
            case 400:
                log.info("email 대체 발송  에러");
                log.info(String.valueOf(response.status()));
        }
        return null;
    }
}
