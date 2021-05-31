package th.co.heimdall.core.domain.property;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.jpa")
@Data
public class JpaProperty {
    private String entityPackage;
}
