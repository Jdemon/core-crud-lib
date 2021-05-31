package th.co.heimdall.core.domain.port.outgoing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface ICrudPort<T, ID extends Serializable> {
    List<T> findAll();

    List<T> findAll(Specification<T> specification);

    Page<T> findAll(Specification<T> specification, Pageable pageable);

    Optional<T> findById(ID id);

    Optional<T> save(T t);

    void saveList(List<T> t);

    Optional<T> update(ID id, T t);

    void delete(ID id);

    void deleteAll();
}
