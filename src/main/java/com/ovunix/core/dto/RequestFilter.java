package com.ovunix.core.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequestFilter {

    private List<Criteria> andCriterias = new ArrayList<>();
    private List<Criteria> orCriterias = new ArrayList<>();

    private int page;
    private int size;
    private boolean sortAsc;
    private String sortBy;

}
