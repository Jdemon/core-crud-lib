package th.co.heimdall.core.enabled.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ComponentScan("th.co.heimdall.core")
public class CoreCrudConfiguration {
}
