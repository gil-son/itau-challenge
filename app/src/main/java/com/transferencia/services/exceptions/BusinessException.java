package com.transferencia.services.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String msg){
          super(msg);
    }
}
