package th.co.heimdall.core.infrastructure.postgres.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import th.co.heimdall.core.infrastructure.postgres.extension.ModelConstant;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonPropertyOrder(
        {
                ModelConstant.CREATED_BY,
                ModelConstant.CREATED_AT,
                ModelConstant.UPDATED_BY,
                ModelConstant.UPDATED_AT
        }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Auditable implements Serializable {

    @CreatedBy
    @Column(updatable = false, nullable = false , length = 100)
    protected String createdBy;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    protected LocalDateTime createdAt;

    @LastModifiedBy
    @Column(nullable = false, length = 100)
    protected String updatedBy;

    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime updatedAt;
}
