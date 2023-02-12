package gutsandgun.kite_sendEmail.consumer;


import gutsandgun.kite_sendEmail.dto.sendMsg.SendManagerMsgDTO;
import gutsandgun.kite_sendEmail.dto.sendMsg.SendMsgProceessingDTO;
import gutsandgun.kite_sendEmail.service.SendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    //service
    @Autowired
    private SendingService sendingService;

    // SKT
    @RabbitListener(queues = "${rabbitmq.routing.key.queue1}")
    public void consumeSKT(SendManagerMsgDTO sendManagerMsgDTO){
        Long brokerId = 1L;
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("1. SKT message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        SendMsgProceessingDTO sendMsgProceessingDTO = new SendMsgProceessingDTO(brokerId,sendManagerMsgDTO);
        sendingService.sendMsgProcessing(brokerId,sendMsgProceessingDTO);

        log.info("============================");

    }

    // KT
    @RabbitListener(queues = "${rabbitmq.routing.key.queue2}")
    public void consumeKT(SendManagerMsgDTO sendManagerMsgDTO){
        Long brokerId = 2L;
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("1/ KT message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        SendMsgProceessingDTO sendMsgProceessingDTO = new SendMsgProceessingDTO(brokerId,sendManagerMsgDTO);
        sendingService.sendMsgProcessing(brokerId,sendMsgProceessingDTO);

        log.info("============================");

    }

    // LG
    @RabbitListener(queues = "${rabbitmq.routing.key.queue3}")
    public void consumeLG(SendManagerMsgDTO sendManagerMsgDTO){
        Long brokerId = 3L;
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("1. LG message: {}",sendManagerMsgDTO);
        log.info("-----------------------------");

        SendMsgProceessingDTO sendMsgProceessingDTO = new SendMsgProceessingDTO(brokerId,sendManagerMsgDTO);
        sendingService.sendMsgProcessing(brokerId,sendMsgProceessingDTO);

        log.info("============================");
    }

}
