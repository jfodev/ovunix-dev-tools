package com.ovunix.service;

import com.ovunix.domain.AbstractEntity;

public interface BusinessStrategy<T extends AbstractEntity> {

  public void treat (T entity) ;
}
