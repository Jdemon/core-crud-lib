package th.co.heimdall.core.domain.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.application")
@Data
public class ApplicationProperty {
    private String name;
    private String version;
    private String buildTime;
}
