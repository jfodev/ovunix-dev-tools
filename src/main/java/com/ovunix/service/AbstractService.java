package com.ovunix.service;

import com.ovunix.domain.AbstractEntity;
import com.ovunix.dto.AbstractDto;
import com.ovunix.dto.RequestFilter;
import com.ovunix.dto.SearchCriteria;
import com.ovunix.dto.validation.ValidationRule;
import com.ovunix.enums.SearchOperation;
import com.ovunix.exceptions.OvunixException;
import com.ovunix.mappers.AbstractMappers;
import com.ovunix.repository.AbstractRepository;
import com.ovunix.utils.UniqueNumberGenerator;
import com.ovunix.validators.Validator;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public abstract class AbstractService<T extends AbstractDto, ID extends Serializable> implements IAbstractService<T, ID> {

    private static final int MAX_TOTAL_ITEM=0;
    public abstract AbstractRepository abstractRepository();

    public abstract AbstractMappers abstractMappers();

    @Autowired
    public IdGeneratorStrategy generatorStrategy;

    protected BusinessStrategy businessStrategy;
    protected Validator<T> validator;


    @Override
    public T save(T t) {
        List<String> errors = new ArrayList<>();
        if (validator != null) {
            for (ValidationRule rule : this.validator.getValidationRules()) {
                if (rule.getCondition().test(t)) {
                    errors.add(rule.getErrorMessage());
                }
            }
            if (!errors.isEmpty()) {
                throw new OvunixException(errors);
            }
        }

        AbstractEntity abstractEntity = this.abstractMappers().toEntity(t);
        generatorStrategy.generate(abstractEntity);
        if (businessStrategy != null) businessStrategy.treat(abstractEntity);
        this.abstractRepository().save(abstractEntity);
        return (T) this.abstractMappers().toDto(abstractEntity);
    }

    @Override
    public Optional<T> find(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        Optional<AbstractEntity> optional = abstractRepository()
                .findById(id);
        if (optional.isEmpty()) return null;
        else return (Optional<T>) Optional.of(this.abstractMappers().toDto(optional.get()));
    }

    @Override
    public List<T> findAll() {
        List<T> results = new ArrayList<>();
        List<AbstractEntity> abstractEntities = this.abstractRepository().findAll();
        if (abstractEntities != null && !abstractEntities.isEmpty()) {
            for (AbstractEntity abstractEntity : abstractEntities) {
                results.add((T) abstractMappers().toDto(abstractEntity));
            }
            return results;
        }
        return new ArrayList<>();
    }

    @Override
    public void deleteById(ID id) {
        this.abstractRepository().deleteById(id);
    }

    @Override
    public  List <T> searchWithFilters(RequestFilter filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), filter.isSortAsc() ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending());
        List <T> dtos=new ArrayList<>();
        Specification<T> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (SearchCriteria criteria : filter.getCriterias()) {
                String key = criteria.getKey();
                Object value = criteria.getValue();
                SearchOperation operation = criteria.getOperation();

                Class<?> fieldType = root.get(key).getJavaType();

                switch (operation) {
                    case EQUAL:
                    case NOT_EQUAL:
                        if (isComparable(fieldType)) {
                            predicates.add(operation == SearchOperation.EQUAL
                                    ? criteriaBuilder.equal(root.get(key), value)
                                    : criteriaBuilder.notEqual(root.get(key), value));
                        } else {
                            throw new IllegalArgumentException("EQUAL/NOT_EQUAL incompatible avec le type : " + fieldType);
                        }
                        break;

                    case LIKE:
                        if (fieldType.equals(String.class)) {
                            predicates.add(criteriaBuilder.like(root.get(key), "%" + value + "%"));
                        } else {
                            throw new IllegalArgumentException("LIKE n'est applicable qu'aux chaînes de caractères.");
                        }
                        break;

                    case GREATER_THAN:
                    case LESS_THAN:
                    case GREATER_THAN_OR_EQUAL:
                    case LESS_THAN_OR_EQUAL:
                        if (Comparable.class.isAssignableFrom(fieldType)) {
                            predicates.add(switch (operation) {
                                case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(key), (Comparable) value);
                                case LESS_THAN -> criteriaBuilder.lessThan(root.get(key), (Comparable) value);
                                case GREATER_THAN_OR_EQUAL ->
                                        criteriaBuilder.greaterThanOrEqualTo(root.get(key), (Comparable) value);
                                case LESS_THAN_OR_EQUAL ->
                                        criteriaBuilder.lessThanOrEqualTo(root.get(key), (Comparable) value);
                                default -> throw new IllegalStateException("Opération non gérée : " + operation);
                            });
                        } else {
                            throw new IllegalArgumentException("Opération incompatible avec le type : " + fieldType);
                        }
                        break;

                    case IN:
                    case NOT_IN:
                        if (value instanceof List<?> listValue) {
                            predicates.add(operation == SearchOperation.IN
                                    ? root.get(key).in(listValue)
                                    : criteriaBuilder.not(root.get(key).in(listValue)));
                        } else {
                            throw new IllegalArgumentException("IN/NOT_IN nécessite une liste de valeurs.");
                        }
                        break;
                    case BLANK:
                        break;
                    default:
                        throw new IllegalArgumentException("Opération inconnue : " + operation);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AbstractEntity> items= abstractRepository().findAll(specification, pageable);
        if (items.getTotalPages()>MAX_TOTAL_ITEM){
          for (AbstractEntity item:items.getContent()){
              dtos.add((T) this.abstractMappers().toDto(item));
          }
        }
        return dtos;
    }

    private boolean isComparable(Class<?> type) {
        return Comparable.class.isAssignableFrom(type);
    }


    public Validator getValidator() {
        return validator;
    }

    @Override
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public BusinessStrategy getBusinessStrategy() {
        return businessStrategy;
    }

    @Override
    public void setBusinessStrategy(BusinessStrategy businessStrategy) {
        this.businessStrategy = businessStrategy;
    }

}
