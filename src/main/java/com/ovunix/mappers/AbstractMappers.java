package com.ovunix.mappers;

import com.ovunix.domain.AbstractEntity;
import com.ovunix.dto.AbstractDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;


public interface AbstractMappers<DTO extends AbstractDto, ENTITY extends AbstractEntity> {
    ENTITY  toEntity(DTO dto);

    DTO toDto(ENTITY entity);

}
