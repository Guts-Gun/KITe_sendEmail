package gutsandgun.kite_sendEmail.feignClients.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import gutsandgun.kite_sendEmail.exception.CustomException;
import gutsandgun.kite_sendEmail.exception.ErrorCode;
import gutsandgun.kite_sendEmail.exception.BrokerResponseErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class FeignBrokerError implements ErrorDecoder {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Exception decode(String s, Response response) {

        switch (response.status()) {
            case 400:
                log.info("borker response 에러");
                log.info(String.valueOf(response.status()));

                BrokerResponseErrorDto message = null;
                try (InputStream bodyIs = response.body()
                        .asInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    message = mapper.readValue(bodyIs, BrokerResponseErrorDto.class);
                } catch (IOException e) {
                    return new Exception(e.getMessage());
                }
                log.info(message.toString());
                for(ErrorCode c : ErrorCode.values()){
                    if(message.getCode().equals(c.getCode())){
                        log.info("error code : {}" , c.getCode());
                        return new CustomException(c);
                    }
                }
                default :
                    log.info("뭔가 오류가 나는중");
                    log.info(String.valueOf(response.status()));
                    log.info(String.valueOf(response));
                    return new CustomException(ErrorCode.BAD_REQUEST);
        }
    }
}
