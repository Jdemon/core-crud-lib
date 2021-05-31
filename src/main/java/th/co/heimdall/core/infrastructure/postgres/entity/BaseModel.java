package th.co.heimdall.core.infrastructure.postgres.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import th.co.heimdall.core.infrastructure.postgres.extension.ModelConstant;

import javax.persistence.*;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@MappedSuperclass
@JsonPropertyOrder(
        {
                ModelConstant.ID
        }
)
public abstract class BaseModel<ID extends Serializable> extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    protected ID id;
}
