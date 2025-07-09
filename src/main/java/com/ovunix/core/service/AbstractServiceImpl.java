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
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;

@Transactional(rollbackFor = {OvunixException.class, Exception.class})
public abstract class AbstractServiceImpl<T extends AbstractDto, ID extends Serializable> implements IAbstractService<T, ID> {

    private static final int MAX_TOTAL_ITEM = 0;

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
            if ( rule.getCondition().test(dto)) {
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
        this.validate(dto);
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

    /**
     * Construit dynamiquement une spécification JPA (de type {@link Specification}) à partir des critères
     * de filtrage fournis dans un objet {@link RequestFilter}. Cette spécification peut ensuite être utilisée
     * avec Spring Data JPA pour appliquer des requêtes dynamiques complexes, combinant des conditions
     * « ET » (AND) et « OU » (OR).
     * <p>
     * Les conditions AND sont évaluées en priorité, et les conditions OR sont regroupées entre elles.
     * Cela permet des requêtes du type :
     * <pre>{@code
     *     WHERE statut = 'ACTIF' AND (nom LIKE '%Jean%' OR prenom LIKE '%Jean%')
     * }</pre>
     *
     * @param filter un objet {@link RequestFilter} contenant les critères à appliquer :
     *               - {@code andCriterias} : liste des critères à combiner avec AND
     *               - {@code orCriterias} : liste des critères à combiner avec OR
     * @return une spécification JPA à passer à une méthode Spring Data JPA telle que {@code findAll(specification)} ou {@code count(specification)}
     * @throws IllegalArgumentException si une clé de critère ne peut pas être résolue ou si une opération est invalide
     */
    protected Specification<Persistable> buildSpecification(RequestFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> andPredicates = new ArrayList<>();
            List<Predicate> orPredicates = new ArrayList<>();

            // Fonction utilitaire pour convertir un SearchCriteria en Predicate
            BiFunction<Criteria, Path<?>, Predicate> toPredicate = (criteria, path) -> {
                Object value = criteria.value();
                Operation operation = criteria.operation();
                Class<?> fieldType = path.getJavaType();

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

            // Traitement des AND
            for (Criteria criteria : filter.getAndCriterias()) {
                Path<?> path = resolvePath(root, criteria.key());
                andPredicates.add(toPredicate.apply(criteria, path));
            }

            // Traitement des OR
            for (Criteria criteria : filter.getOrCriterias()) {
                Path<?> path = resolvePath(root, criteria.key());
                orPredicates.add(toPredicate.apply(criteria, path));
            }

            Predicate andPredicate = andPredicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));
            Predicate orPredicate = orPredicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));

            return criteriaBuilder.and(andPredicate, orPredicate);
        };
    }

    @Override
    public CountDto count(RequestFilter filter) {
        Specification<Persistable> specification = buildSpecification(filter);
        long total = abstractRepository().count(specification);
        return new CountDto((int) total);
    }

    private Path<?> resolvePath(From<?, ?> root, String key) {
        String[] parts = key.split("\\.");
        Path<?> path = root;
        for (int i = 0; i < parts.length - 1; i++) {
            path = ((From<?, ?>) path).join(parts[i], JoinType.LEFT);
        }
        return path.get(parts[parts.length - 1]);
    }

    @Override
    public void setBusinessStrategy(BusinessStrategy strategy) {
        this.businessStrategy = strategy;
    }

    private boolean isComparable(Class<?> type) {
        return Comparable.class.isAssignableFrom(type);
    }

    protected AbstractDto determineMapping (Persistable abstractEntity){
        return  this.abstractMappers().toDto(abstractEntity);
    }

    @Override
    public CountDto count() {
        long total = abstractRepository().count();
        return new CountDto((int) total);
    }
}
