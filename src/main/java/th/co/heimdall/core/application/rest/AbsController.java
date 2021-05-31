package th.co.heimdall.core.application.rest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import th.co.heimdall.core.domain.exception.CantDeleteException;
import th.co.heimdall.core.domain.exception.NotFoundException;
import th.co.heimdall.core.domain.service.AbsService;
import th.co.heimdall.core.infrastructure.postgres.extension.ClazzUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;

public class AbsController<T, ID extends Serializable> {

    private final AbsService<T, ID> port;
    private final Class<T> clazz;

    public AbsController(AbsService<T, ID> port) {
        this.port = port;
        this.clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @GetMapping
    public Page<T> find(@RequestParam Map<String, String> params, Pageable pageable) {
        return port.find(params, pageable);
    }

    @GetMapping("{id}")
    public ResponseEntity<T> findById(@PathVariable ID id) {
        Optional<T> optT = port.findById(id);
        return optT.map(ResponseEntity::ok).orElseThrow(NotFoundException::new);
    }

    @PostMapping
    public ResponseEntity<T> save(@RequestBody T t) {
        Optional<T> optT = port.save(t);
        return optT.map(ResponseEntity::ok).orElseThrow(NotFoundException::new);
    }

    @PutMapping("{id}")
    public ResponseEntity<T> update(@PathVariable ID id, @RequestBody T t) {
        if (port.findById(id).isEmpty()) {
            throw new NotFoundException();
        }
        Optional<T> optT = port.update(id, t);
        return optT.map(ResponseEntity::ok).orElseThrow(NotFoundException::new);
    }

    @PatchMapping("{id}")
    public ResponseEntity<T> patch(@PathVariable ID id, @RequestBody T t) {
        if (port.findById(id).isEmpty()) {
            throw new NotFoundException();
        }
        Optional<T> optT = port.patch(id, t);
        return optT.map(ResponseEntity::ok).orElseThrow(NotFoundException::new);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<T> delete(@PathVariable ID id) {
        if (port.findById(id).isEmpty()) {
            throw new CantDeleteException();
        }
        port.delete(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("clear/all")
    public ResponseEntity<?> deleteAll() {
        port.deleteAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<?> exportExcel(HttpServletResponse response, @RequestParam Map<String, String> params) {
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "filename=" + clazz.getSimpleName() + "_" + System.currentTimeMillis() + ".xlsx");
            byte[] byteArray = port.exportExcel(params);
            response.setContentLength(byteArray.length);
            outputStream.write(byteArray);
        } catch (Throwable e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import/excel")
    public ResponseEntity<?> importExcel(MultipartFile file) throws IOException {
        port.importExcel(file.getBytes());
        return ResponseEntity.ok().build();
    }

    @GetMapping("schema")
    public ResponseEntity<?> getSchema() {
        return ResponseEntity.ok(ClazzUtil.entitySchema(clazz));
    }
}
