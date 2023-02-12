package gutsandgun.kite_sendEmail.feignClients;

import gutsandgun.kite_sendEmail.dto.sendMsg.BrokerMsgDTO;
import gutsandgun.kite_sendEmail.feignClients.broker.FeignBrokerConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "smsFeignClient", url = "${feign.url.broker-dummy}", configuration = FeignBrokerConfig.class)
public interface SmsFeignClient {
	@PostMapping("/broker/all/{brokerName}/send/sms")
	ResponseEntity<Long> sendSms(@PathVariable("brokerName") String name,@RequestBody BrokerMsgDTO brokerMsgDTO);

}
