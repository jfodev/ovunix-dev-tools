package com.ovunix.core.mappers;

import com.ovunix.core.domain.Persistable;
import com.ovunix.core.dto.AbstractDto;


public interface AbstractMappers<DTO extends AbstractDto, ENTITY extends Persistable> {

    ENTITY  toEntity(DTO dto);

    DTO toDto(ENTITY entity);

}
