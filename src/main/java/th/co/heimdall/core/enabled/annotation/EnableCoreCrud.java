package th.co.heimdall.core.enabled.annotation;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Import;
import th.co.heimdall.core.enabled.configuration.CoreCrudConfiguration;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({CoreCrudConfiguration.class})
public @interface EnableCoreCrud {
}
