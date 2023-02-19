package gutsandgun.kite_sendEmail.consumer;


import gutsandgun.kite_sendEmail.dto.sendEmail.SendManagerEmailDTO;
import gutsandgun.kite_sendEmail.dto.sendEmail.SendEmailProceessingDTO;
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
    @RabbitListener(queues = "${rabbitmq.email.queue1.name}")
    public void consumeEmail1(SendManagerEmailDTO sendManagerEmailDTO){
        Long brokerId = 4L;
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("1. Email1 message: {}", sendManagerEmailDTO);
        log.info("-----------------------------");

        SendEmailProceessingDTO sendEmailProceessingDTO = new SendEmailProceessingDTO(brokerId, sendManagerEmailDTO);
        new Thread(()->sendingService.sendEmailProcessing(sendEmailProceessingDTO)).start();

        log.info("============================");

    }

    // KT
    @RabbitListener(queues = "${rabbitmq.email.queue2.name}")
    public void consumeEmail2(SendManagerEmailDTO sendManagerEmailDTO){
        Long brokerId = 5L;
        log.info("============================");
        //1.rabbitmq consumer - sendManager msg
        log.info("1. Email2: {}", sendManagerEmailDTO);
        log.info("-----------------------------");

        SendEmailProceessingDTO sendEmailProceessingDTO = new SendEmailProceessingDTO(brokerId, sendManagerEmailDTO);
        new Thread(()->sendingService.sendEmailProcessing(sendEmailProceessingDTO)).start();

        log.info("============================");

    }



}
