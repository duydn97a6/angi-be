package vn.angi.common.exception;

import org.springframework.http.HttpStatus;
import vn.angi.common.constant.ErrorCodes;

import java.util.Map;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(ErrorCodes.VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, Map<String, Object> details) {
        super(ErrorCodes.VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST, details);
    }
}
