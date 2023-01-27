package com.bashkarsampath.application.client.exceptions;

import java.util.Arrays;

import org.springframework.boot.logging.LogLevel;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private ResponseEntity<Object> buildResponseEntity(ApiErrorModel model, LogLevel logLevel) {
		ApiErrorModelLogger logger = new ApiErrorModelLogger(model);
		logger.logToConsole(logLevel);
		model.getError().setDebugMessage(null);
		return new ResponseEntity<>(logger.getModel(), logger.getModel().getError().getHttpStatusError());
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		return buildResponseEntity(new ApiErrorModel(HttpStatus.BAD_REQUEST, ex), LogLevel.INFO);
	}

	@ExceptionHandler(value = { java.lang.SecurityException.class })
	public ResponseEntity<Object> handleSecurityException(Exception ex) {
		ApiErrorModel err = new ApiErrorModel(HttpStatus.UNAUTHORIZED, ex);
		err.getError().setDebugMessage(null);
		return buildResponseEntity(err, LogLevel.INFO);
	}

	@ExceptionHandler(value = { OutOfMemoryError.class, Exception.class })
	public ResponseEntity<Object> handleException(Exception ex) {
		ApiErrorModel err = new ApiErrorModel(HttpStatus.INTERNAL_SERVER_ERROR, ex);
		return buildResponseEntity(err, LogLevel.ERROR);
	}

	@ExceptionHandler(value = { HttpServerErrorException.class, HttpClientErrorException.class,
			HttpStatusCodeException.class })
	public ResponseEntity<Object> handleHttpException(HttpStatusCodeException ex) throws JsonProcessingException {
		ApiErrorModel err = new ApiErrorModel(ex.getStatusCode(), ex);
		ObjectMapper mapper = new ObjectMapper();
		err.getError().setMessage("External Server Returned Error");
		try {
			JsonNode jn = mapper.readValue(ex.getResponseBodyAsString(), JsonNode.class);
			err.getError().getAdditionalProperties().add(jn);
			err.getError().setDebugMessage(ex.getLocalizedMessage());
		} catch (Exception jsonException) {
			err.getError().setDebugMessage(ex.getLocalizedMessage());
			JsonNode root = mapper.createObjectNode();
			ObjectNode node = (ObjectNode) root;
			node.put("reason", "" + ex.getLocalizedMessage());
			err.getError().setAdditionalProperties(Arrays.asList(root));
		}
		return buildResponseEntity(err, LogLevel.INFO);
	}
}