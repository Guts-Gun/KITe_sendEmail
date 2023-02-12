package gutsandgun.kite_sendEmail.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gutsandgun.kite_sendEmail.dto.*;
import gutsandgun.kite_sendEmail.dto.log.BrokerRequestLogDTO;
import gutsandgun.kite_sendEmail.dto.log.BrokerResponseLogDTO;
import gutsandgun.kite_sendEmail.dto.log.MissingSendingIdLogDTO;
import gutsandgun.kite_sendEmail.dto.sendMsg.ReplaceSendingBodyDTO;
import gutsandgun.kite_sendEmail.dto.sendMsg.SendMsgProceessingDTO;
import gutsandgun.kite_sendEmail.entity.read.Broker;
import gutsandgun.kite_sendEmail.entity.read.Sending;
import gutsandgun.kite_sendEmail.exception.ConsumerException;
import gutsandgun.kite_sendEmail.exception.CustomException;
import gutsandgun.kite_sendEmail.exception.ErrorCode;
import gutsandgun.kite_sendEmail.feignClients.SendingFeignClient;
import gutsandgun.kite_sendEmail.feignClients.SmsFeignClient;
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
    Map<Long, String> msgBroker = new HashMap<>() {{
        put(1L, "SKT");
        put(2L, "KT");
        put(3L, "LG");
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
    private SmsFeignClient smsFeignClient;
    @Autowired
    private SendingFeignClient sendingFeignClient;

    public void sendMsgProcessing(Long brokerId,SendMsgProceessingDTO sendMsgProceessingDTO){
        try{
            //2.sending 정보 얻기
            sendMsgProceessingDTO.setSendingDto(getSendingToDto(sendMsgProceessingDTO.getSendingId()));
            log.info("-----------------------------");
            //3.broker msg만들기 (with msg 처리)
            sendMsgProceessingDTO.setBrokerMsgDTO();
            log.info("-----------------------------");
            //4.발송
            BrokerResponseLogDTO brokerResponseLogDTO = sendBroker(sendMsgProceessingDTO);
            //5.대체발송 (브로커/이메일)
            if(brokerResponseLogDTO.getSuccess().equals(SendingStatus.FAIL) && sendMsgProceessingDTO.getSendingDto().getReplaceYn().equals("Y")){
                switch (brokerResponseLogDTO.getFailReason()){
                    case BAD_REQUEST :
                        alternativeSendBroker(sendMsgProceessingDTO);
                        break;
                    case INVALID_PHONE:
                        alternativeSendEmail(sendMsgProceessingDTO);
                        break;
                }
            }
        }
        catch(ConsumerException e){
            log.info("*******************************************");
            if(e.getMessage().equals(ConsumerException.ERROR_DB)) {
                log.info("ERROR : sending 정보 DB 에 없음");
                MissingSendingIdLogDTO missingSendingIdLogDTO = new MissingSendingIdLogDTO(sendMsgProceessingDTO);
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
            log.info("2. getSending :{} :",sending.toString());
            log.info("처리 속도(using cache) : "+secDiffTime);

            return sendingDto;
        }



        public BrokerResponseLogDTO sendBroker(SendMsgProceessingDTO sendMsgProceessingDTO){
            BrokerResponseLogDTO brokerResponseLogDTO = null;
            try {
                log.info("4. Send broker: {}",sendMsgProceessingDTO.getBrokerMsgDTO());
                BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(sendMsgProceessingDTO.getBrokerId(),sendMsgProceessingDTO);
                log.info("request log: "+ brokerRequestLogDTO.toString());
                ResponseEntity<Long> response = smsFeignClient.sendSms(msgBroker.get(sendMsgProceessingDTO.getBrokerId()),sendMsgProceessingDTO.getBrokerMsgDTO());
            }
            catch (CustomException e){
                log.info("*******************************************");
                System.out.println("ERROR : BROKER - " + e.getErrorCode());
                brokerResponseLogDTO = new BrokerResponseLogDTO(sendMsgProceessingDTO.getBrokerId(), SendingStatus.FAIL,sendMsgProceessingDTO);
                if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                    //1. 브로커 오류
                    brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                }
                else if(e.getErrorCode() == ErrorCode.INVALID_PHONE){
                    //2.수신거부/전화번호 없음 로그 기록
                    brokerResponseLogDTO.setFailReason(FailReason.INVALID_PHONE);
                }
                else{
                    //other오류도 처리해야하는지?
                }
                log.info("response log: "+ brokerResponseLogDTO.toString());
                log.info("*******************************************");
            }
            finally {
                if(brokerResponseLogDTO==null){
                    brokerResponseLogDTO = new BrokerResponseLogDTO(sendMsgProceessingDTO.getBrokerId(), SendingStatus.COMPLETE,sendMsgProceessingDTO);
                    log.info("response log: "+brokerResponseLogDTO.toString());
                }
            }
            log.info("-----------------------------");
            return brokerResponseLogDTO;
        }

        public void alternativeSendEmail(SendMsgProceessingDTO sendMsgProceessingDTO){
            ReplaceSendingBodyDTO replaceSendingBodyDTO = new ReplaceSendingBodyDTO(sendMsgProceessingDTO.getSendingId(),sendMsgProceessingDTO.getTxId());
            log.info("5-1. 이메일 대체발송");
            log.info("request: {}",replaceSendingBodyDTO);
            ResponseEntity<String> response = sendingFeignClient.sendSms(replaceSendingBodyDTO);
            log.info("response: {}",response.getStatusCode());
        }
        public void alternativeSendBroker(SendMsgProceessingDTO sendMsgProceessingDTO){
            log.info("5-2. 중계사 대체발송");

            //broker 정보 가져오기
            List<BrokerDTO> brokerDTOList = getMsgBrokerList();
            ArrayList<Boolean> brokerResponseList = new ArrayList<Boolean>();
            //log.info("brokerList:{}",brokerDTOList);
            //log.info("-----------------------------");

            //대체 발송 처리(sending queue)
            for (BrokerDTO b : brokerDTOList){
                //최초발송 false처리
                if(sendMsgProceessingDTO.getBrokerId() == b.getId()){
                    brokerResponseList.add(false);
                }
                else{
                    Boolean alternativeBrokerSuccess = true;
                    try{
                        log.info("대체발송 중계사: {}번-{}", b.getId(),msgBroker.get(b.getId()));
                        BrokerRequestLogDTO brokerRequestLogDTO = new BrokerRequestLogDTO(b.getId(),sendMsgProceessingDTO);
                        log.info("request: "+ brokerRequestLogDTO.toString());
                        ResponseEntity<Long> response = smsFeignClient.sendSms(msgBroker.get(b.getId()),sendMsgProceessingDTO.getBrokerMsgDTO());
                    }
                    catch (CustomException e){
                        System.out.println(e);
                        log.info("*******************************************");
                        System.out.println("ERROR : BROKER - " + e.getErrorCode());
                        alternativeBrokerSuccess = false;
                        BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(), SendingStatus.FAIL,sendMsgProceessingDTO);
                        if(e.getErrorCode() == ErrorCode.BAD_REQUEST){
                            brokerResponseLogDTO.setFailReason(FailReason.BAD_REQUEST);
                        }
                        else if(e.getErrorCode() == ErrorCode.INVALID_PHONE){
                            brokerResponseLogDTO.setFailReason(FailReason.INVALID_PHONE);
                        }
                        log.info("response log: "+ brokerResponseLogDTO.toString());
                        log.info("*******************************************");
                    }
                    finally {
                        if(alternativeBrokerSuccess){
                            brokerResponseList.add(true);
                            BrokerResponseLogDTO brokerResponseLogDTO = new BrokerResponseLogDTO(b.getId(),SendingStatus.COMPLETE,sendMsgProceessingDTO);
                            log.info("response log: "+ brokerResponseLogDTO.toString());
                            break;
                        }
                        else{
                            brokerResponseList.add(false);
                        }
                    }
                }
            }
        }

            public List<BrokerDTO> getMsgBrokerList(){
                List<Broker> BrokerList = readBrokerRepository.findBySendingType(SendingType.SMS);
                List<BrokerDTO> brokerDTOList = new ArrayList<>();
                BrokerList.forEach(broker -> {
                    brokerDTOList.add(mapper.convertValue(broker,BrokerDTO.class));
                });

                return brokerDTOList;
            }

}
