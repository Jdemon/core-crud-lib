package th.co.heimdall.example.infrastructure.postgres.user.adaptor;

import org.springframework.stereotype.Component;
import th.co.heimdall.core.infrastructure.postgres.adaptor.BaseModelAdaptor;
import th.co.heimdall.core.infrastructure.postgres.repository.ICoreRepository;
import th.co.heimdall.example.infrastructure.postgres.user.entity.User;

@Component
public class UserAdaptor extends BaseModelAdaptor<User, Long> {

    protected UserAdaptor(ICoreRepository<User, Long> repository) {
        super(repository);
    }
}
