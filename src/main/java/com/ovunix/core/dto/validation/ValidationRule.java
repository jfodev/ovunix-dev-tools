package com.ovunix.core.dto.validation;

import com.ovunix.core.dto.AbstractDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Predicate;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ValidationRule <T extends AbstractDto>{

    private Predicate<T> condition;
    private String errorMessage;
    private boolean enCreation=false;


}
