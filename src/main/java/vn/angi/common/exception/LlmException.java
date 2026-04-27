package vn.angi.common.exception;

import org.springframework.http.HttpStatus;
import vn.angi.common.constant.ErrorCodes;

public class LlmException extends AppException {
    public LlmException(String message) {
        super(ErrorCodes.LLM_ERROR, message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public LlmException(String message, Throwable cause) {
        super(ErrorCodes.LLM_ERROR, message, HttpStatus.SERVICE_UNAVAILABLE);
        initCause(cause);
    }
}
