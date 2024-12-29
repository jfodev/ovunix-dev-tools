package com.ovunix.service;

import com.ovunix.domain.AbstractEntity;

public interface IdGeneratorStrategy<T extends AbstractEntity> {

  public void generate (T entity) ;
}
