package gutsandgun.kite_sendEmail.entity.read;

import gutsandgun.kite_sendEmail.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 발신 e-mail 저장 테이블
 */
@Entity
@Getter
@Setter
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE user_email SET is_deleted=true WHERE id = ?")
@Table(name = "user_email",
		indexes = {
				@Index(name = "idx_user_email_user_id", columnList = "fk_user_id")
		})
public class UserEmail extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "fk_user_id")
	@Comment("user id")
	private String userId;

	@Comment("발신 이름")
	private String name;

	@Comment("발신 email")
	private String email;

	@ColumnDefault("false")
	private Boolean isDeleted = false;

	@Comment("생성자")
	@Column(name = "reg_id", nullable = false, length = 20)
	private String regId;

	@Comment("수정자")
	@Column(name = "mod_id", length = 20)
	private String modId;
}
