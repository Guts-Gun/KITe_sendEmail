package gutsandgun.kite_sendEmail.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gutsandgun.kite_sendEmail.dto.*;
import gutsandgun.kite_sendEmail.dto.log.BrokerRequestLogDTO;
import gutsandgun.kite_sendEmail.dto.log.BrokerResponseLogDTO;
import gutsandgun.kite_sendEmail.dto.log.MissingSendingIdLogDTO;
import gutsandgun.kite_sendEmail.dto.sendEmail.BrokerEmailDTO;
import gutsandgun.kite_sendEmail.dto.sendEmail.SendEmailProceessingDTO;
import gutsandgun.kite_sendEmail.entity.read.Broker;
import gutsandgun.kite_sendEmail.entity.read.Sending;
import gutsandgun.kite_sendEmail.exception.ConsumerException;
import gutsandgun.kite_sendEmail.exception.CustomException;
import gutsandgun.kite_sendEmail.exception.ErrorCode;
import gutsandgun.kite_sendEmail.feignClients.EmailBroker1FeignClient;
import gutsandgun.kite_sendEmail.feignClients.EmailBroker2FeignClient;
import gutsandgun.kite_sendEmail.publisher.RabbitMQProducer;
import gutsandgun.kite_sendEmail.repository.read.ReadBrokerRepository;
import gutsandgun.kite_sendEmail.repository.read.ReadSendingRepository;
import gutsandgun.kite_sendEmail.type.FailReason;
import gutsandgun.kite_sendEmail.type.SendingStatus;
import gutsandgun.kite_sendEmail.type.SendingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SendingService {
    //borker mapping
    Map<Long, String> emailBroker = new HashMap<>() {{
        put(4L, "emailbroker1");
        put(5L, "emailbroker2");
    }};
    @Autowired
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(SendingService.class);
    //read db

    //repo
    @Autowired
    ReadSendingRepository readSendingRepository;

    @Autowired
    ReadBrokerRepository readBrokerRepository;

    //api
    @Autowired
    private EmailBroker1FeignClient emailBroker1FeignClient;
    @Autowired
    private EmailBroker2FeignClient emailBroker2FeignClient;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    SendingCache sendingCache;
    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    public void sendEmailProcessing(SendEmailProceessingDTO sendEmailProceessingDTO){
        try{
            //2.sending 정보 얻기
            sendEmailProceessingDTO.setSendingDto(objectMapper.readValue(sendingCache.getSendingDto(sendEmailProceessingDTO.getSendingId()), SendingDto.class));
            log.info("-----------------------------");
            if(!sendEmailProceessingDTO.getSendingType().equals(SendingType.EMAIL)){
                log.info("@@@@플랫폼 대체 발송@@@@");
                sendEmailProceessingDTO.setSendingType(SendingType.EMAIL);
                log.info("-----------------------------");
            }
            //3.broker email만들기
            sendEmailProceessingDTO.setBrokerEmailDTO();
            log.info("-----------------------------");
            //4.발송
            BrokerResponseLogDTO brokerResponseLogDTO = sendBroker(sendEmailProceessingDTO);
            //5.대체발송 (브로커)
            if(brokerResponseLogDTO.getSuccess().equals(SendingStatus.FAIL)){
                switch (brokerResponseLogDTO.getFailReason()){
                    case BAD_REQUEST :
                        alternativeSendBroker(sendEmailProceessingDTO);
                        break;
                }
            }
        }
        catch(ConsumerException e){
            log.info("*******************************************");
            if(e.getMessage().equals(ConsumerException.ERROR_DB)) {
                log.info("ERROR : sending 정보 DB 에 없음");
                MissingSendingIdLogDTO missingSendingIdLogDTO = new MissingSendingIdLogDTO(sendEmailProceessingDTO);
                rabbitMQProducer.logSendQueue("log: " + missingSendingIdLogDTO.toString());
                log.info("log: " + missingSendingIdLogDTO.toString());
            }
            log.info("*******************************************");
        } catch (JsonProcessingException e) {
            log.info("*******************************************");
            if(e.getMessage().equals(ConsumerException.ERROR_DB)) {
                log.info("ERROR : sending 정보 DB 에 없음2 (parsing error)");
                MissingSendingIdLogDTO missingSendingIdLogDTO = new MissingSendingIdLogDTO(sendEmailProceessingDTO);
                rabbitMQProducer.logSendQueue("log: " + missingSendingIdLogDTO.toString());
                log.info("log: " + missingSendingIdLogDTO.toString());
            }
            log.info("*******************************************");

        }
    }


    public BrokerResponseLogDTO sendBroker(SendEmailProceessingDTO sendEmailProceessingDTO){
            BrokerResponseLogDTO brokerResponseLogDTO = null;
            try {
                log.info("4. Send broker: {}", sendEmailProceessingDTO.getBrokerEmailDTO());
                BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(sendEmailProceessingDTO.getBrokerId(), sendEmailProceessingDTO);
                rabbitMQProducer.logSendQueue("broker[초기발송] request log: "+ brokerRequestLogDTO.toString());
                log.info("broker[초기발송] request log: "+ brokerRequestLogDTO.toString());
                ResponseEntity<Long> response = sendBrokerApi(sendEmailProceessingDTO.getBrokerId(), sendEmailProceessingDTO.getBrokerEmailDTO());
            }
            catch (CustomException e){
                log.info("*******************************************");
                log.info("ERROR : BROKER - " + e.getErrorCode());
                brokerResponseLogDTO = new BrokerResponseLogDTO(sendEmailProceessingDTO.getBrokerId(), SendingStatus.FAIL, sendEmailProceessingDTO);
                if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                    //1. 브로커 오류
                    brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                }
                else{
                    //other오류도 처리해야하는지?
                }
                rabbitMQProducer.logSendQueue("broker[초기발송] response log: "+ brokerResponseLogDTO.toString());
                log.info("broker[초기발송] response log: "+ brokerResponseLogDTO.toString());
                log.info("*******************************************");
            }
            finally {
                if(brokerResponseLogDTO==null){
                    brokerResponseLogDTO = new BrokerResponseLogDTO(sendEmailProceessingDTO.getBrokerId(), SendingStatus.COMPLETE, sendEmailProceessingDTO);
                    brokerResponseLogDTO.setLast(true);
                    rabbitMQProducer.logSendQueue("broker[초기발송] response log: "+brokerResponseLogDTO.toString());
                    log.info("broker[초기발송] response log: "+ brokerResponseLogDTO.toString());
                }
            }
            log.info("-----------------------------");
            return brokerResponseLogDTO;
        }

        public void alternativeSendBroker(SendEmailProceessingDTO sendEmailProceessingDTO){
            log.info("5-2. 중계사 대체발송");

            //broker 정보 가져오기
            List<BrokerDTO> brokerDTOList = getEmailBrokerList();
            ArrayList<Boolean> brokerResponseList = new ArrayList<Boolean>();
            log.info("brokerList:{}",brokerDTOList);
            log.info("-----------------------------");

            int brokerSendingCount = 0;

            //대체 발송 처리(sending queue)
            for (BrokerDTO b : brokerDTOList){
                //최초발송 false처리
                if(sendEmailProceessingDTO.getBrokerId() == b.getId()){
                    brokerResponseList.add(false);
                    brokerSendingCount+=1;
                }
                else{
                    Boolean alternativeBrokerSuccess = true;
                    try{
                        log.info("대체발송 중계사: {}번-{}", b.getId(),emailBroker.get(b.getId()));
                        BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(b.getId(), sendEmailProceessingDTO);
                        brokerSendingCount+=1;
                        rabbitMQProducer.logSendQueue("broker[대체발송] request: "+ brokerRequestLogDTO.toString());
                        log.info("broker[대체발송] request: "+ brokerRequestLogDTO.toString());
                        ResponseEntity<Long> response = sendBrokerApi(sendEmailProceessingDTO.getBrokerId(), sendEmailProceessingDTO.getBrokerEmailDTO());
                    }
                    catch (CustomException e){
                        log.info("*******************************************");
                        log.info("ERROR : BROKER - " + e.getErrorCode());
                        alternativeBrokerSuccess = false;
                        BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(), SendingStatus.FAIL, sendEmailProceessingDTO);
                        if(brokerSendingCount==2){
                            //끝 대체 발송 브로커
                            brokerResponseLogDTO.setLast(true);
                        }
                        if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                            brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                        }
                        rabbitMQProducer.logSendQueue("broker[대체발송] response log: "+ brokerResponseLogDTO.toString());
                        log.info("broker[대체발송] response log: "+ brokerResponseLogDTO.toString());
                        log.info("*******************************************");
                    }
                    finally {
                        if(alternativeBrokerSuccess){
                            brokerResponseList.add(true);
                            BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(),SendingStatus.COMPLETE, sendEmailProceessingDTO);
                            brokerResponseLogDTO.setLast(true);
                            rabbitMQProducer.logSendQueue("broker[대체발송] response log: "+ brokerResponseLogDTO.toString());
                            log.info("broker[대체발송] response log: "+ brokerResponseLogDTO.toString());
                            break;
                        }
                        else{
                            brokerResponseList.add(false);
                        }
                    }
                }
            }
        }
            private ResponseEntity<Long> sendBrokerApi(Long brokerId, BrokerEmailDTO brokerEmailDTO){
                ResponseEntity<Long> responseEntity = null;
                if (brokerId == 4L) {
                    responseEntity=emailBroker1FeignClient.sendEmail(emailBroker.get(brokerId),brokerEmailDTO);
                } else if (brokerId == 5L) {
                    responseEntity=emailBroker2FeignClient.sendEmail(emailBroker.get(brokerId),brokerEmailDTO);
                }
                return responseEntity;
            }

            private List<BrokerDTO> getEmailBrokerList(){
                List<Broker> BrokerList = readBrokerRepository.findBySendingType(SendingType.EMAIL);
                List<BrokerDTO> brokerDTOList = new ArrayList<>();
                BrokerList.forEach(broker -> {
                    brokerDTOList.add(mapper.convertValue(broker,BrokerDTO.class));
                });

                return brokerDTOList;
            }

}
