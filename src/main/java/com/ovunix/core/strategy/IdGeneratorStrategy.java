package com.ovunix.core.strategy;

import com.ovunix.core.domain.Persistable;

public interface IdGeneratorStrategy<T extends Persistable> {

  public void generate (T entity) ;
}
