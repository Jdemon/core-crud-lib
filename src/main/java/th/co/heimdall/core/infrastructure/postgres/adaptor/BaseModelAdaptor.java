package th.co.heimdall.core.infrastructure.postgres.adaptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import th.co.heimdall.core.infrastructure.postgres.entity.BaseModel;
import th.co.heimdall.core.domain.port.outgoing.ICrudPort;
import th.co.heimdall.core.infrastructure.postgres.repository.ICoreRepository;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class BaseModelAdaptor<T extends BaseModel<ID>, ID extends Serializable> implements ICrudPort<T, ID> {

    private final ICoreRepository<T, ID> repository;

    protected BaseModelAdaptor(ICoreRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public List<T> findAll(Specification<T> specification) {
        return repository.findAll(specification);
    }

    @Override
    public Page<T> findAll(Specification<T> specification, Pageable pageable) {
        return repository.findAll(specification, pageable);
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<T> save(T t) {
        t.setId(null);
        return Optional.of(repository.save(t));
    }

    @Override
    public void saveList(List<T> listOfT) {
        for (T t : listOfT) {
            ID id = t.getId();
            if (id != null) {
                update(id, t);
            } else {
                save(t);
            }
        }
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public Optional<T> update(ID id, T t) {
        Optional<T> original = repository.findById(id);
        t.setId(id);
        original.ifPresent(value ->
                {
                    t.setCreatedAt(value.getCreatedAt());
                    t.setCreatedBy(value.getCreatedBy());
                }
        );
        return Optional.of(repository.save(t));
    }

    @Override
    public void delete(ID id) {
        repository.deleteById(id);
    }
}
