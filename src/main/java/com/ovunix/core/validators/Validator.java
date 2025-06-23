package com.ovunix.core.validators;

import com.ovunix.core.dto.AbstractDto;
import com.ovunix.core.dto.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;

public interface Validator<T extends AbstractDto>{
    List<ValidationRule> getValidationRules();
}
