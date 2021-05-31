package th.co.heimdall.example.domain.user.service;

import org.springframework.stereotype.Service;
import th.co.heimdall.core.domain.service.AbsService;
import th.co.heimdall.example.infrastructure.postgres.user.adaptor.UserAdaptor;
import th.co.heimdall.example.infrastructure.postgres.user.entity.User;

@Service
public class UserService extends AbsService<User, Long> {
    public UserService(UserAdaptor port) {
        super(port);
    }
}
