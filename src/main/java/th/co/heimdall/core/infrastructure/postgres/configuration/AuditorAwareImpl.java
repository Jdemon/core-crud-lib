package th.co.heimdall.core.infrastructure.postgres.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import th.co.heimdall.core.domain.exception.NotAuthorizedException;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        HttpServletRequest request =
                ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                        .getRequest();

        String userId = request.getHeader("x-user-id");

        if(StringUtils.isBlank(userId)) {
            throw new NotAuthorizedException();
        }
        return Optional.of(userId);
    }

}
