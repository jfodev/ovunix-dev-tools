package com.ovunix.core.service;

import com.ovunix.core.dto.AbstractDto;
import com.ovunix.core.dto.CountDto;
import com.ovunix.core.dto.RequestFilter;
import com.ovunix.core.strategy.BusinessStrategy;
import com.ovunix.core.validators.Validator;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface IAbstractService <T extends AbstractDto,ID extends Serializable>{

    T save (T t);

    T update (T t);

    Optional<T> find (ID id);

    List<T> findAll();

    void deleteById(ID id);

    List <T> filter(RequestFilter filter);

    void setValidator (Validator validator);

   void  setBusinessStrategy (BusinessStrategy businessStrategy);

   CountDto count(RequestFilter filter);

   CountDto count();
}
