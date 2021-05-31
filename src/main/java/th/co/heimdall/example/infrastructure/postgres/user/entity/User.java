package th.co.heimdall.example.infrastructure.postgres.user.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import th.co.heimdall.core.infrastructure.postgres.entity.BaseModel;
import th.co.heimdall.core.infrastructure.postgres.extension.ModelConstant;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "users"
)
@JsonPropertyOrder(
        {
                ModelConstant.ID,
                "userName",
                ModelConstant.CREATED_BY,
                ModelConstant.CREATED_AT,
                ModelConstant.UPDATED_BY,
                ModelConstant.UPDATED_AT
        }
)
public class User extends BaseModel<Long> {
    @Column(nullable = false, length = 20)
    private String userName;
}
