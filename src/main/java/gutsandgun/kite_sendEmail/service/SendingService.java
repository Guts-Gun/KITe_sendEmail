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
            //2.sending ?????? ??????
            sendEmailProceessingDTO.setSendingDto(objectMapper.readValue(sendingCache.getSendingDto(sendEmailProceessingDTO.getSendingId()), SendingDto.class));
            log.info("-----------------------------");
            if(!sendEmailProceessingDTO.getSendingType().equals(SendingType.EMAIL)){
                log.info("@@@@????????? ?????? ??????@@@@");
                sendEmailProceessingDTO.setSendingType(SendingType.EMAIL);
                log.info("-----------------------------");
            }
            //3.broker email?????????
            sendEmailProceessingDTO.setBrokerEmailDTO();
            log.info("-----------------------------");
            //4.??????
            BrokerResponseLogDTO brokerResponseLogDTO = sendBroker(sendEmailProceessingDTO);
            //5.???????????? (?????????)
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
                log.info("ERROR : sending ?????? DB ??? ??????");
                MissingSendingIdLogDTO missingSendingIdLogDTO = new MissingSendingIdLogDTO(sendEmailProceessingDTO);
                rabbitMQProducer.logSendQueue("log: " + missingSendingIdLogDTO.toString());
                log.info("log: " + missingSendingIdLogDTO.toString());
            }
            log.info("*******************************************");
        } catch (JsonProcessingException e) {
            log.info("*******************************************");
            if(e.getMessage().equals(ConsumerException.ERROR_DB)) {
                log.info("ERROR : sending ?????? DB ??? ??????2 (parsing error)");
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
                rabbitMQProducer.logSendQueue("broker[????????????] request log: "+ brokerRequestLogDTO.toString());
                log.info("broker[????????????] request log: "+ brokerRequestLogDTO.toString());
                ResponseEntity<Long> response = sendBrokerApi(sendEmailProceessingDTO.getBrokerId(), sendEmailProceessingDTO.getBrokerEmailDTO());
            }
            catch (CustomException e){
                log.info("*******************************************");
                log.info("ERROR : BROKER - " + e.getErrorCode());
                brokerResponseLogDTO = new BrokerResponseLogDTO(sendEmailProceessingDTO.getBrokerId(), SendingStatus.FAIL, sendEmailProceessingDTO);
                if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                    //1. ????????? ??????
                    brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                }
                else{
                    //other????????? ??????????????????????
                }
                rabbitMQProducer.logSendQueue("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                log.info("*******************************************");
            }
            finally {
                if(brokerResponseLogDTO==null){
                    brokerResponseLogDTO = new BrokerResponseLogDTO(sendEmailProceessingDTO.getBrokerId(), SendingStatus.COMPLETE, sendEmailProceessingDTO);
                    brokerResponseLogDTO.setLast(true);
                    rabbitMQProducer.logSendQueue("broker[????????????] response log: "+brokerResponseLogDTO.toString());
                    log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                }
            }
            log.info("-----------------------------");
            return brokerResponseLogDTO;
        }

        public void alternativeSendBroker(SendEmailProceessingDTO sendEmailProceessingDTO){
            log.info("5-2. ????????? ????????????");

            //broker ?????? ????????????
            List<BrokerDTO> brokerDTOList = getEmailBrokerList();
            ArrayList<Boolean> brokerResponseList = new ArrayList<Boolean>();
            log.info("brokerList:{}",brokerDTOList);
            log.info("-----------------------------");

            int brokerSendingCount = 1;

            //?????? ?????? ??????(sending queue)
            for (BrokerDTO b : brokerDTOList){
                //???????????? false??????
                if(sendEmailProceessingDTO.getBrokerId() == b.getId()){
                    brokerResponseList.add(false);
                }
                else{
                    Boolean alternativeBrokerSuccess = true;
                    try{
                        log.info("???????????? ?????????: {}???-{}", b.getId(),emailBroker.get(b.getId()));
                        BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(b.getId(), sendEmailProceessingDTO);
                        brokerSendingCount+=1;
                        rabbitMQProducer.logSendQueue("broker[????????????] request: "+ brokerRequestLogDTO.toString());
                        log.info("broker[????????????] request: "+ brokerRequestLogDTO.toString());
                        ResponseEntity<Long> response = sendBrokerApi(b.getId(), sendEmailProceessingDTO.getBrokerEmailDTO());
                    }
                    catch (CustomException e){
                        log.info("*******************************************");
                        log.info("ERROR : BROKER - " + e.getErrorCode());
                        alternativeBrokerSuccess = false;
                        BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(), SendingStatus.FAIL, sendEmailProceessingDTO);
                        if(brokerSendingCount==2){
                            //??? ?????? ?????? ?????????
                            brokerResponseLogDTO.setLast(true);
                        }
                        if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                            brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                        }
                        rabbitMQProducer.logSendQueue("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                        log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                        log.info("*******************************************");
                    }
                    finally {
                        if(alternativeBrokerSuccess){
                            brokerResponseList.add(true);
                            BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(),SendingStatus.COMPLETE, sendEmailProceessingDTO);
                            brokerResponseLogDTO.setLast(true);
                            rabbitMQProducer.logSendQueue("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
                            log.info("broker[????????????] response log: "+ brokerResponseLogDTO.toString());
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
