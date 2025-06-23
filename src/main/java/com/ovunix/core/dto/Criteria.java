package com.ovunix.core.dto;

import com.ovunix.core.annotations.Operation;


public record Criteria(String key, Object value, Operation operation){

}
