package org.openlmis.requisition.errorhandling;

import org.openlmis.errorhandling.AbstractErrorHandling;
import org.openlmis.requisition.exception.ContentNotFoundMessageException;
import org.openlmis.utils.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error handling for requisition related exceptions.
 */
@ControllerAdvice
public class RequisitionErrorHandling extends AbstractErrorHandling {

  @ExceptionHandler(ContentNotFoundMessageException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public Message.LocalizedMessage handleContentNotFoundMessageException(
      ContentNotFoundMessageException ex) {
    return getLocalizedMessage(ex);
  }
}
