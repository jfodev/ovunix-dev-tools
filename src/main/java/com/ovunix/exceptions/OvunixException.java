package com.ovunix.exceptions;


import lombok.Data;

import java.util.List;

@Data
public class OvunixException extends RuntimeException{

    private  List<String> errors;

    public OvunixException(List<String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

   public OvunixException(){
       super();
   }

    public OvunixException(String message){
        super(message);
    }

    public OvunixException(String message,Throwable cause){
        super(message,cause);
    }
}
