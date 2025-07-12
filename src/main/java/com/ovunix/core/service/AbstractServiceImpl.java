package com.ovunix.core.service;

import com.ovunix.core.domain.Persistable;
import com.ovunix.core.dto.*;
import com.ovunix.core.dto.validation.ValidationRule;
import com.ovunix.core.annotations.Operation;
import com.ovunix.core.exceptions.OvunixException;
import com.ovunix.core.mappers.AbstractMappers;
import com.ovunix.core.repository.AbstractRepository;
import com.ovunix.core.strategy.BusinessStrategy;
import com.ovunix.core.strategy.IdGeneratorStrategy;
import com.ovunix.core.validators.Validator;
import jakarta.persistence.Entity;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;

@Transactional(rollbackFor = {OvunixException.class, Exception.class})
public abstract class AbstractServiceImpl<T extends AbstractDto, ID extends Serializable> implements IAbstractService<T, ID> {

    @Autowired
    protected IdGeneratorStrategy generatorStrategy;

    protected BusinessStrategy businessStrategy;

    public abstract AbstractRepository abstractRepository();

    public abstract AbstractMappers abstractMappers();

    private final Map<String, Validator<?>> validators;
    private final Map<String, BusinessStrategy<?, ?>> strategies;

    public AbstractServiceImpl(
            Map<String, Validator<?>> validators,
            Map<String, BusinessStrategy<?, ?>> strategies,
            IdGeneratorStrategy generatorStrategy
    ) {
        this.validators = validators;
        this.strategies = strategies;
        this.generatorStrategy = generatorStrategy;
    }

    protected Validator<T> getValidator(Class<T> dtoClass) {
        String key = dtoClass.getSimpleName().replace("Dto", "").toLowerCase() + "Validator";
        return (Validator<T>) validators.get(key);
    }

    protected BusinessStrategy<?, ?> getStrategy(Class<T> dtoClass) {
        String key = dtoClass.getSimpleName().replace("Dto", "").toLowerCase() + "Strategy";
        return strategies.get(key);
    }

    private void validate(T dto) {
        Validator<T> validator = getValidator((Class<T>) dto.getClass());
        if (validator == null) return;

        List<String> errors = new ArrayList<>();
        for (ValidationRule rule : validator.getValidationRules()) {
            if (rule.getCondition().test(dto)) {
                errors.add(rule.getErrorMessage());
            }
        }
        if (!errors.isEmpty()) throw new OvunixException(errors);
    }

    private T persist(T dto) {
        this.validate(dto);
        Persistable entity = abstractMappers().toEntity(dto);
        generatorStrategy.generate(entity);
        BusinessStrategy strategy = getStrategy((Class<T>) dto.getClass());
        if (strategy != null) {
            strategy.treat(entity, dto);
        }

        abstractRepository().save(entity);
        return (T) abstractMappers().toDto(entity);
    }

    @Override
    public T save(T dto) {
        return persist(dto, true);
    }

    @Override
    public T update(T dto) {
        return persist(dto, false);
    }

    private T persist(T dto, boolean isCreation) {
        Persistable entity = abstractMappers().toEntity(dto);
        generatorStrategy.generate(entity);

        if (businessStrategy != null) {
            businessStrategy.treat(entity, dto);
        }

        try {
            abstractRepository().save(entity);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new OvunixException("This resource has been modified by another user. Please reload and try again.");
        }

        return (T) abstractMappers().toDto(entity);
    }

    @Override
    public Optional<T> find(ID id) {
        return abstractRepository().findById(id).map(entity -> (T) abstractMappers().toDto((Persistable) entity));
    }

    @Override
    public List<T> findAll() {
        List<Persistable> entities = abstractRepository().findAll();
        List<T> results = new ArrayList<>();
        for (Persistable entity : entities) {
            results.add((T) abstractMappers().toDto(entity));
        }
        return results;
    }

    @Override
    public void deleteById(ID id) {
        abstractRepository().deleteById(id);
    }

    @Override
    public List<T> filter(RequestFilter filter) {
        Specification<Persistable> specification = buildSpecification(filter);

        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                filter.isSortAsc()
                        ? Sort.by(filter.getSortBy()).ascending()
                        : Sort.by(filter.getSortBy()).descending()
        );

        Page<Persistable> page = abstractRepository().findAll(specification, pageable);

        return page.getContent().stream()
                .map(p -> (T) determineMapping(p))
                .toList();
    }

    protected Specification<Persistable> buildSpecification(RequestFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> andPredicates = new ArrayList<>();
            List<Predicate> orPredicates = new ArrayList<>();

            BiFunction<Criteria, Path<?>, Predicate> toPredicate = (criteria, path) -> {
                Object value = criteria.value();
                Operation operation = criteria.operation();

                return switch (operation) {
                    case EQUAL -> criteriaBuilder.equal(path, value);
                    case NOT_EQUAL -> criteriaBuilder.notEqual(path, value);
                    case LIKE -> criteriaBuilder.like(path.as(String.class), "%" + value + "%");
                    case GREATER_THAN -> criteriaBuilder.greaterThan(path.as(Comparable.class), (Comparable) value);
                    case LESS_THAN -> criteriaBuilder.lessThan(path.as(Comparable.class), (Comparable) value);
                    case GREATER_THAN_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
                    case LESS_THAN_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
                    case IN -> path.in((List<?>) value);
                    case NOT_IN -> criteriaBuilder.not(path.in((List<?>) value));
                    case BLANK -> criteriaBuilder.or(criteriaBuilder.isNull(path), criteriaBuilder.equal(path, ""));
                    default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
                };
            };

            for (Criteria criteria : filter.getAndCriterias()) {
                Path<?> path = resolvePath(root, criteria.key());
                andPredicates.add(toPredicate.apply(criteria, path));
            }

            for (Criteria criteria : filter.getOrCriterias()) {
                Path<?> path = resolvePath(root, criteria.key());
                orPredicates.add(toPredicate.apply(criteria, path));
            }

            Predicate andPredicate = andPredicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));
            Predicate orPredicate = orPredicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));

            return criteriaBuilder.and(andPredicate, orPredicate);
        };
    }

    private Path<?> resolvePath(From<?, ?> root, String key) {
        String[] parts = key.split("\\.");
        Path<?> path = root;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean isLast = (i == parts.length - 1);

            if (path instanceof From<?, ?> fromPath) {
                Class<?> fieldType = getFieldType(fromPath.getJavaType(), part);
                if (!isLast && isEntity(fieldType)) {
                    path = getOrCreateJoin(fromPath, part);
                } else {
                    path = fromPath.get(part);
                }
            } else {
                path = path.get(part);
            }
        }

        return path;
    }

    private boolean isEntity(Class<?> type) {
        return type != null && type.isAnnotationPresent(Entity.class);
    }

    private Class<?> getFieldType(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field.getType();
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private Join<?, ?> getOrCreateJoin(From<?, ?> from, String attributeName) {
        return from.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals(attributeName))
                .findFirst()
                .orElseGet(() -> from.join(attributeName, JoinType.LEFT));
    }

    @Override
    public CountDto count(RequestFilter filter) {
        Specification<Persistable> specification = buildSpecification(filter);
        long total = abstractRepository().count(specification);
        return new CountDto((int) total);
    }

    @Override
    public CountDto count() {
        long total = abstractRepository().count();
        return new CountDto((int) total);
    }

    @Override
    public void setBusinessStrategy(BusinessStrategy strategy) {
        this.businessStrategy = strategy;
    }

    protected AbstractDto determineMapping(Persistable abstractEntity) {
        return this.abstractMappers().toDto(abstractEntity);
    }
}
