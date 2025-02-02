package com.ovunix.service;

import com.ovunix.domain.AbstractEntity;
import com.ovunix.dto.AbstractDto;

public interface BusinessStrategy<T extends AbstractEntity> {

  public void treat (T entity, AbstractDto dto) ;
}
