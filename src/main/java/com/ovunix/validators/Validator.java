package com.ovunix.validators;

import com.ovunix.dto.AbstractDto;
import com.ovunix.dto.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;

public interface Validator<T extends AbstractDto>{
   default List<ValidationRule<T>> getValidationRules (){
       return new ArrayList<>();
   }
}
