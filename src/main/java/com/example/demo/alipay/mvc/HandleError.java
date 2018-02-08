package com.example.demo.alipay.mvc;

import com.example.demo.alipay.exception.ServiceException;
import com.example.demo.alipay.model.ErrorBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Created by youzhiyong on 2018/1/26.
 */
@ControllerAdvice
public class HandleError {

    @ExceptionHandler(value = ServiceException.class)
    public ResponseEntity<ErrorBody> handleServiceException(ServiceException exception) {
        ErrorBody errorBody = new ErrorBody();
        errorBody.setMessage(exception.getMessage());
        errorBody.setCode(exception.getCode());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(errorBody);
    }

}
