package th.co.heimdall.core.domain.port.incoming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICrudUseCase<T, ID extends Serializable> {
    List<T> find(Map<String, String> params);

    Page<T> find(Map<String, String> params, Pageable pageable);

    Optional<T> findById(ID id);

    Optional<T> save(T t);

    Optional<T> update(ID id, T t);

    Optional<T> patch(ID id, T t);

    void delete(ID id);

    void deleteAll();

    byte[] exportExcel(Map<String, String> params) throws Exception;

    void importExcel(byte[] fileData);
}
