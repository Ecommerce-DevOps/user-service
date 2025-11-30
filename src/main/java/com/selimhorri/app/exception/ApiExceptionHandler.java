package com.selimhorri.app.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.selimhorri.app.exception.payload.ExceptionMsg;
import com.selimhorri.app.exception.wrapper.AddressNotFoundException;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.exception.wrapper.VerificationTokenNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiExceptionHandler {

	@ExceptionHandler(value = {
			MethodArgumentNotValidException.class,
			HttpMessageNotReadableException.class
	})
	public <T extends BindException> ResponseEntity<ExceptionMsg> handleValidationException(final T e) {

		log.info("**ApiExceptionHandler controller, handle validation exception*\n");
		final var badRequest = HttpStatus.BAD_REQUEST;

		return new ResponseEntity<>(
				ExceptionMsg.builder()
						.msg("*" + e.getBindingResult().getFieldError().getDefaultMessage() + "!**")
						.httpStatus(badRequest)
						.timestamp(ZonedDateTime
								.now(ZoneId.systemDefault()))
						.build(),
				badRequest);
	}

	@ExceptionHandler(value = {
			UserObjectNotFoundException.class,
			CredentialNotFoundException.class,
			VerificationTokenNotFoundException.class,
			AddressNotFoundException.class
	})
	public <T extends RuntimeException> ResponseEntity<ExceptionMsg> handleApiRequestException(final T e) {

		log.info("**ApiExceptionHandler controller, handle API request*\n");
		final var notFound = HttpStatus.NOT_FOUND;

		return new ResponseEntity<>(
				ExceptionMsg.builder()
						.msg("#### " + e.getMessage() + "! ####")
						.httpStatus(notFound)
						.timestamp(ZonedDateTime
								.now(ZoneId.systemDefault()))
						.build(),
				notFound);
	}

	@ExceptionHandler(value = org.springframework.dao.DataIntegrityViolationException.class)
	public ResponseEntity<ExceptionMsg> handleDataIntegrityViolation(
			final org.springframework.dao.DataIntegrityViolationException e) {

		log.info("**ApiExceptionHandler controller, handle data integrity violation*\n");
		final var conflict = HttpStatus.CONFLICT;

		String message = "Data integrity violation";
		if (e.getMessage().contains("username")) {
			message = "Username already exists";
		} else if (e.getMessage().contains("email")) {
			message = "Email already exists";
		}

		return new ResponseEntity<>(
				ExceptionMsg.builder()
						.msg("*" + message + "!*")
						.httpStatus(conflict)
						.timestamp(ZonedDateTime
								.now(ZoneId.systemDefault()))
						.build(),
				conflict);
	}

	@ExceptionHandler(value = org.hibernate.LazyInitializationException.class)
	public ResponseEntity<ExceptionMsg> handleLazyInitializationException(
			final org.hibernate.LazyInitializationException e) {

		log.error("**LazyInitializationException - this should not happen with proper mapping*\n", e);
		final var internalError = HttpStatus.INTERNAL_SERVER_ERROR;

		return new ResponseEntity<>(
				ExceptionMsg.builder()
						.msg("*Internal server error - lazy loading issue*")
						.httpStatus(internalError)
						.timestamp(ZonedDateTime
								.now(ZoneId.systemDefault()))
						.build(),
				internalError);
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ExceptionMsg> handleGenericException(final Exception e) {

		log.error("**Unhandled exception*\n", e);
		final var internalError = HttpStatus.INTERNAL_SERVER_ERROR;

		return new ResponseEntity<>(
				ExceptionMsg.builder()
						.msg("*Internal server error*")
						.httpStatus(internalError)
						.timestamp(ZonedDateTime
								.now(ZoneId.systemDefault()))
						.build(),
				internalError);
	}

}
