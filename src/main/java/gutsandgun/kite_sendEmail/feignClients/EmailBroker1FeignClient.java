package gutsandgun.kite_sendEmail.feignClients;

import gutsandgun.kite_sendEmail.dto.sendEmail.BrokerEmailDTO;
import gutsandgun.kite_sendEmail.feignClients.broker.FeignBrokerConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "emailFeignClient", url = "${feign.url.broker-dummy4}", configuration = FeignBrokerConfig.class)
public interface EmailBroker1FeignClient {
	@PostMapping("/broker/all/{brokerName}/send/sms")
	ResponseEntity<Long> sendEmail(@PathVariable("brokerName") String name, @RequestBody BrokerEmailDTO brokerEmailDTO);

}
