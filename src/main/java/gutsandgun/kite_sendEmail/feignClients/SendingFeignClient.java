package gutsandgun.kite_sendEmail.feignClients;

import gutsandgun.kite_sendEmail.dto.sendMsg.ReplaceSendingBodyDTO;
import gutsandgun.kite_sendEmail.feignClients.sending.FeignSendingConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sendingFeignClient", url = "${feign.url.sending}", configuration = FeignSendingConfig.class)
public interface SendingFeignClient {
	@PostMapping("/sending/replaceSend/Msg")
	ResponseEntity<String> sendSms(@RequestBody ReplaceSendingBodyDTO sendingBodyDTO);
}
