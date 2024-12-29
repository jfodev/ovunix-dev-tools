package com.ovunix.service;

import com.ovunix.dto.AbstractDto;
import com.ovunix.dto.RequestFilter;
import com.ovunix.dto.SearchCriteria;
import com.ovunix.validators.Validator;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IAbstractService <T extends AbstractDto,ID extends Serializable>{

    T save (T t);


     Optional<T> find (ID id);

    List<T> findAll();

    void deleteById(ID id);


    List <T>  searchWithFilters(RequestFilter filter);


    void setValidator (Validator validator);

   void  setBusinessStrategy (BusinessStrategy businessStrategy);

}
