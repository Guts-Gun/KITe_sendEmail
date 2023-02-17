package gutsandgun.kite_sendEmail.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gutsandgun.kite_sendEmail.dto.SendingDto;
import gutsandgun.kite_sendEmail.entity.read.Sending;
import gutsandgun.kite_sendEmail.exception.ConsumerException;
import gutsandgun.kite_sendEmail.repository.read.ReadSendingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendingCache {

    private static final Logger log = LoggerFactory.getLogger(SendingCache.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ReadSendingRepository readSendingRepository;


    @Cacheable(value="sending" , key = "#sendingId" ,cacheManager = "CacheManager")
    public String getSendingDto(Long sendingId) throws JsonProcessingException {
        Sending sending = getSending(sendingId);
        log.info("2. getSending :{} :",sending.toString());
        SendingDto sendingDto = new SendingDto(sending);
        String sendingDtoStr = objectMapper.writeValueAsString(sendingDto);
        return sendingDtoStr;
    }
        public Sending getSending(Long sendingId){
            //with log
            Sending sending = readSendingRepository.findById(sendingId).orElseThrow(()-> new ConsumerException(ConsumerException.ERROR_DB));
            log.info("!GetSending in db :{} :",sending.toString());
            return sending;
        }

}
