package gutsandgun.kite_sendEmail.dto.sendEmail;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
//msg랑 같음
public class SendManagerEmailDTO {
    private Long id;
    private Long sendingId;
    private String sender;
    private String receiver;
    private String name;
    private String regId;
    private String modId;
}