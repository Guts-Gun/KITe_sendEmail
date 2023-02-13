package gutsandgun.kite_sendEmail.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gutsandgun.kite_sendEmail.dto.*;
import gutsandgun.kite_sendEmail.dto.log.BrokerRequestLogDTO;
import gutsandgun.kite_sendEmail.dto.log.BrokerResponseLogDTO;
import gutsandgun.kite_sendEmail.dto.log.MissingSendingIdLogDTO;
import gutsandgun.kite_sendEmail.dto.sendEmail.SendEmailProceessingDTO;
import gutsandgun.kite_sendEmail.entity.read.Broker;
import gutsandgun.kite_sendEmail.entity.read.Sending;
import gutsandgun.kite_sendEmail.exception.ConsumerException;
import gutsandgun.kite_sendEmail.exception.CustomException;
import gutsandgun.kite_sendEmail.exception.ErrorCode;
import gutsandgun.kite_sendEmail.feignClients.EmailFeignClient;
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
    private EmailFeignClient emailFeignClient;

    public void sendMsgProcessing(SendEmailProceessingDTO sendEmailProceessingDTO){
        try{
            //2.sending 정보 얻기
            sendEmailProceessingDTO.setSendingDto(getSendingToDto(sendEmailProceessingDTO.getSendingId()));
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
                log.info("log: " + missingSendingIdLogDTO.toString());
            }
            log.info("*******************************************");
        }
    }

        @Cacheable(value="test" , key = "#id" ,cacheManager = "redisCacheManager")
        public SendingDto getSendingToDto(Long id){
            //with log
            long beforeTime = System.currentTimeMillis();

            Sending sending = readSendingRepository.findById(id).orElseThrow(()-> new ConsumerException(ConsumerException.ERROR_DB));
            SendingDto sendingDto = new SendingDto(sending);

            long afterTime = System.currentTimeMillis();
            long secDiffTime = (afterTime - beforeTime);
            log.info("2. getSending :{} :",sendingDto.toString());
            log.info("처리 속도(using cache) : "+secDiffTime);

            return sendingDto;
        }



        public BrokerResponseLogDTO sendBroker(SendEmailProceessingDTO sendEmailProceessingDTO){
            BrokerResponseLogDTO brokerResponseLogDTO = null;
            try {
                log.info("4. Send broker: {}", sendEmailProceessingDTO.getBrokerEmailDTO());
                BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(sendEmailProceessingDTO.getBrokerId(), sendEmailProceessingDTO);
                log.info("broker[초기발송] request log: "+ brokerRequestLogDTO.toString());
                ResponseEntity<Long> response = emailFeignClient.sendEmail(emailBroker.get(sendEmailProceessingDTO.getBrokerId()), sendEmailProceessingDTO.getBrokerEmailDTO());
            }
            catch (CustomException e){
                log.info("*******************************************");
                System.out.println("ERROR : BROKER - " + e.getErrorCode());
                brokerResponseLogDTO = new BrokerResponseLogDTO(sendEmailProceessingDTO.getBrokerId(), SendingStatus.FAIL, sendEmailProceessingDTO);
                if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                    //1. 브로커 오류
                    brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                }
                else{
                    //other오류도 처리해야하는지?
                }
                log.info("broker[초기발송] response log: "+ brokerResponseLogDTO.toString());
                log.info("*******************************************");
            }
            finally {
                if(brokerResponseLogDTO==null){
                    brokerResponseLogDTO = new BrokerResponseLogDTO(sendEmailProceessingDTO.getBrokerId(), SendingStatus.COMPLETE, sendEmailProceessingDTO);
                    log.info("broker[초기발송] response log: "+brokerResponseLogDTO.toString());
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
            //log.info("brokerList:{}",brokerDTOList);
            //log.info("-----------------------------");

            //대체 발송 처리(sending queue)
            for (BrokerDTO b : brokerDTOList){
                //최초발송 false처리
                if(sendEmailProceessingDTO.getBrokerId() == b.getId()){
                    brokerResponseList.add(false);
                }
                else{
                    Boolean alternativeBrokerSuccess = true;
                    try{
                        log.info("대체발송 중계사: {}번-{}", b.getId(),emailBroker.get(b.getId()));
                        BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(b.getId(), sendEmailProceessingDTO);
                        log.info("broker[대체발송] request: "+ brokerRequestLogDTO.toString());
                        ResponseEntity<Long> response = emailFeignClient.sendEmail(emailBroker.get(b.getId()), sendEmailProceessingDTO.getBrokerEmailDTO());
                    }
                    catch (CustomException e){
                        System.out.println(e);
                        log.info("*******************************************");
                        System.out.println("ERROR : BROKER - " + e.getErrorCode());
                        alternativeBrokerSuccess = false;
                        BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(), SendingStatus.FAIL, sendEmailProceessingDTO);
                        if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                            brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                        }
                        log.info("broker[대체발송] response log: "+ brokerResponseLogDTO.toString());
                        log.info("*******************************************");
                    }
                    finally {
                        if(alternativeBrokerSuccess){
                            brokerResponseList.add(true);
                            BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(),SendingStatus.COMPLETE, sendEmailProceessingDTO);
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

            public List<BrokerDTO> getEmailBrokerList(){
                List<Broker> BrokerList = readBrokerRepository.findBySendingType(SendingType.EMAIL);
                List<BrokerDTO> brokerDTOList = new ArrayList<>();
                BrokerList.forEach(broker -> {
                    brokerDTOList.add(mapper.convertValue(broker,BrokerDTO.class));
                });

                return brokerDTOList;
            }

}
