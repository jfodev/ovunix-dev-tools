package com.ovunix.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequestFilter {

    private  static final  int MAX_ITEM_PER_PAGE=10;
    private  static final  int START_PAGE=0;
    private List<SearchCriteria> criterias;
    private int page=START_PAGE;
    private  int size=MAX_ITEM_PER_PAGE;
    private String sortBy;
    private boolean sortAsc;

}
