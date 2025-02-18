package com.ovunix.dto;

import com.ovunix.annotations.SearchOperation;
import lombok.Data;

@Data
public class SearchCriteria {
    private String key;
    private Object value;
    private SearchOperation operation;




}
