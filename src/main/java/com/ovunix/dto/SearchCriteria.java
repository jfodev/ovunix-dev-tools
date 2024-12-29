package com.ovunix.dto;

import com.ovunix.enums.SearchOperation;
import lombok.Data;

@Data
public class SearchCriteria {
    private String key;
    private Object value;
    private SearchOperation operation;




}
