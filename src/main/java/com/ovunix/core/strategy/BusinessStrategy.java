package com.ovunix.core.strategy;

import com.ovunix.core.domain.Persistable;
import com.ovunix.core.dto.AbstractDto;

@FunctionalInterface
public interface BusinessStrategy<T extends Persistable, D extends AbstractDto> {

  void treat(T entity, D dto);
}