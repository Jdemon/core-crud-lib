package th.co.heimdall.example.infrastructure.postgres.user.repository;

import th.co.heimdall.core.infrastructure.postgres.repository.ICoreRepository;
import th.co.heimdall.example.infrastructure.postgres.user.entity.User;

interface UserRepository extends ICoreRepository<User, Long> {
}
