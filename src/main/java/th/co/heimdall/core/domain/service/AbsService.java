package th.co.heimdall.core.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import th.co.heimdall.core.domain.exception.FilterException;
import th.co.heimdall.core.domain.excel.ExcelExporter;
import th.co.heimdall.core.domain.excel.ExcelImporter;
import th.co.heimdall.core.domain.port.outgoing.ICrudPort;
import th.co.heimdall.core.domain.port.incoming.ICrudUseCase;
import th.co.heimdall.core.infrastructure.postgres.extension.ClazzUtil;
import th.co.heimdall.core.infrastructure.postgres.extension.ModelConstant;

import javax.persistence.Id;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Transactional(rollbackFor = Throwable.class, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
public abstract class AbsService<T, ID extends Serializable> implements ICrudUseCase<T, ID> {

    protected final ICrudPort<T, ID> port;
    protected Class<T> clazz;
    protected static final String TH = "Th";
    protected static final String EN = "En";
    protected static final String BLANK = " ";
    protected static final String QUERY = "q";
    protected static final String SORT = "sort";
    protected static final String GREATER_THAN_EQUAL = "_gte";
    protected static final String LESS_THAN_EQUAL = "_lte";
    protected static final String NOT_EQUAL = "_ne";
    protected static final String LIKE = "_like";
    private static final Set<Class<?>> primitiveNumbers = Stream
            .of(int.class, long.class, float.class,
                    double.class, byte.class, short.class)
            .collect(Collectors.toSet());

    public AbsService(ICrudPort<T, ID> port) {
        this.port = port;
        this.clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Override
    public List<T> find(Map<String, String> params) {
        return port.findAll(coreSpecification(params));
    }

    @Override
    public Page<T> find(Map<String, String> params, Pageable pageable) {
        return port.findAll(coreSpecification(params), pageable);
    }

    @Override
    public Optional<T> findById(ID id) {
        return port.findById(id);
    }

    @Override
    public Optional<T> save(T t) {
        return port.save(t);
    }

    @Override
    public Optional<T> update(ID id, T t) {
        return port.update(id, t);
    }

    @Override
    public Optional<T> patch(ID id, T t) {
        Optional<T> opt = port.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        T original = opt.get();
        copyPropertiesIgnoreNull(t, original);
        return port.update(id, original);
    }

    @Override
    public void delete(ID id) {
        port.delete(id);
    }

    protected Specification<T> coreSpecification(Map<String, String> params) {
        try {
            return (root, query, cb) -> {
                boolean isFullText = params.containsKey(QUERY);
                Field[] fields = ClazzUtil.getAllFields(this.clazz);
                if (isFullText) {
                    if (!params.containsKey(SORT)) {
                        query.orderBy(cb.asc(root.get(ModelConstant.ID)));
                    }
                    String q = params.get(QUERY).toLowerCase();
                    List<Predicate> predicates = fullTextSearch(fields, q, root, cb);
                    predicates = customFullTextSearch(predicates, q, root, cb);
                    return cb.or(predicates.toArray(new Predicate[0]));
                }
                return searchByFields(fields, params, root, query, cb);
            };
        } catch (Exception e) {
            throw new FilterException(e);
        }
    }

    protected List<Predicate> customFullTextSearch(List<Predicate> predicates, String query, Root<T> root, CriteriaBuilder cb) {
        return predicates;
    }

    public List<Predicate> fullTextSearch(Field[] fields, String query, Root<T> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        for (Field field : fields) {
            predicates.add(cb.like(cb.lower(root.get(field.getName()).as(String.class)),
                    "%" + query + "%"));
        }
        return predicates;
    }

    protected Predicate searchByFields(Field[] fields, Map<String, String> params, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        for (Field field : fields) {
            if (!params.containsKey(SORT) && field.isAnnotationPresent(Id.class)) {
                query.orderBy(cb.asc(root.get(field.getName())));
            }

            if (params.containsKey(field.getName())) {
                List<Predicate> fieldPredicate = new ArrayList<>();
                String[] values = StringUtils.split(params.get(field.getName()), ',');
                for (String val : values) {
                    fieldPredicate.add(cb.equal(root.get(field.getName()).as(String.class),
                            StringUtils.trim(val)));
                }
                predicates.add(cb.or(fieldPredicate.toArray(new Predicate[0])));
            }
            String gteVal = params.get(field.getName() + GREATER_THAN_EQUAL);
            String lteVal = params.get(field.getName() + LESS_THAN_EQUAL);
            String neVal = params.get(field.getName() + NOT_EQUAL);
            String likeVal = params.get(field.getName() + LIKE);

            if (StringUtils.isNotBlank(neVal)) {
                String[] values = StringUtils.split(neVal, ',');
                for (String val : values) {
                    predicates.add(cb.notEqual(root.get(field.getName()).as(String.class),
                            val));
                }
            }


            if (StringUtils.isNotBlank(likeVal)) {
                predicates.add(cb.like(cb.lower(root.get(field.getName()).as(String.class)),
                        "%" + likeVal + "%"));
            }

            if (isNumericType(field.getType()) && NumberUtils.isCreatable(lteVal)) {
                try {
                    if (StringUtils.isNotBlank(lteVal) && NumberUtils.isCreatable(lteVal)) {
                        predicates.add(cb.le(root.get(field.getName()), NumberFormat.getInstance().parse(lteVal)));
                    }
                    if (StringUtils.isNotBlank(gteVal) && NumberUtils.isCreatable(gteVal)) {
                        predicates.add(cb.ge(root.get(field.getName()), NumberFormat.getInstance().parse(gteVal)));
                    }
                } catch (ParseException e) {
                    log.warn(ExceptionUtils.getStackTrace(e), e);
                }
            }
            if (field.getType().isAssignableFrom(LocalDateTime.class)) {
                if (StringUtils.isNotBlank(lteVal)) {
                    LocalDateTime lteDateTime = LocalDateTime.parse(lteVal, DateTimeFormatter.ofPattern(ClazzUtil.DATETIME_FORMAT));
                    predicates.add(cb.lessThanOrEqualTo(root.get(field.getName()),
                            lteDateTime));
                }
                if (StringUtils.isNotBlank(gteVal)) {
                    LocalDateTime gteDateTime = LocalDateTime.parse(gteVal, DateTimeFormatter.ofPattern(ClazzUtil.DATETIME_FORMAT));
                    predicates.add(cb.greaterThanOrEqualTo(root.get(field.getName()),
                            gteDateTime));
                }
            }

            if (field.getType().isAssignableFrom(LocalDate.class)) {
                if (StringUtils.isNotBlank(lteVal)) {
                    LocalDate lteDateTime = LocalDate.parse(lteVal, DateTimeFormatter.ofPattern(ClazzUtil.DATE_FORMAT));
                    predicates.add(cb.lessThanOrEqualTo(root.get(field.getName()),
                            lteDateTime));
                }
                if (StringUtils.isNotBlank(gteVal)) {
                    LocalDate gteDateTime = LocalDate.parse(gteVal, DateTimeFormatter.ofPattern(ClazzUtil.DATE_FORMAT));
                    predicates.add(cb.greaterThanOrEqualTo(root.get(field.getName()),
                            gteDateTime));
                }
            }
        }
        if (!predicates.isEmpty()) {
            return cb.and(predicates.toArray(new Predicate[0]));
        } else {
            return null;
        }
    }

    protected Expression<String> searchFullTextAppendFields(Root<T> root, CriteriaBuilder cb, @NotNull String separator, @NotNull String... fieldNames) {
        Expression<String> base = cb.concat(root.get(fieldNames[0]), separator);
        int length = fieldNames.length;
        if (length > 1) {
            for (int index = 1; index < length; index++) {
                base = cb.concat(base, root.get(fieldNames[index]));
                if (index != (length - 1)) {
                    base = cb.concat(base, separator);
                }
            }
        }
        return base;
    }

    private static boolean isNumericType(Class<?> cls) {
        if (cls.isPrimitive()) {
            return primitiveNumbers.contains(cls);
        } else {
            return Number.class.isAssignableFrom(cls);
        }
    }

    public String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    // then use Spring BeanUtils to copy and ignore null using our function
    public void copyPropertiesIgnoreNull(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    @Override
    public byte[] exportExcel(Map<String, String> params) {
        List<T> allData = port.findAll(coreSpecification(params));
        return new ExcelExporter<T>(allData, clazz).exportExcel();
    }

    @Override
    public void importExcel(byte[] fileData) {
        port.saveList(new ExcelImporter<T>(clazz).importExcel(fileData));
    }

    @Override
    public void deleteAll() {
        port.deleteAll();
    }
}
