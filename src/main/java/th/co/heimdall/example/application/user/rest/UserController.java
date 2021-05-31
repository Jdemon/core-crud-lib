package th.co.heimdall.example.application.user.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import th.co.heimdall.core.application.rest.AbsController;
import th.co.heimdall.example.domain.user.service.UserService;
import th.co.heimdall.example.infrastructure.postgres.user.entity.User;

@RestController
@RequestMapping("users")
public class UserController extends AbsController<User, Long> {

    public UserController(UserService port) {
        super(port);
    }
}
