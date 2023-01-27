package com.bashkarsampath.application.client.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorModel {
	@JsonProperty("error")
	private Error error;

	@Data
	public class Error {
		private String apiDomain;
		private String errorType;
		@JsonIgnore
		private HttpStatus httpStatusError;
		private int errorCode;
		private String message;
		private String referenceId;
		private String debugMessage;
		@JsonIgnore
		@ToStringExclude
		private Throwable exception;
		private List<JsonNode> additionalProperties;
	}

	private ApiErrorModel(Throwable ex, String referenceId) {
		this.error = new Error();
		this.error.apiDomain = "personal";
		this.error.httpStatusError = HttpStatus.INTERNAL_SERVER_ERROR;
		this.error.errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		this.error.referenceId = referenceId;
		this.error.exception = ex;
		this.error.errorType = this.error.exception.getClass().getName();
		int mid = this.error.errorType.lastIndexOf('.') + 1;
		this.error.errorType = this.error.errorType.substring(mid);
		this.error.message = ex.getMessage();
		if (this.error.message == null && ex.getLocalizedMessage() != null)
			this.error.message = ex.getLocalizedMessage();
		this.error.debugMessage = ExceptionUtils.getStackTrace(ex).replace("\r\n\tat", " => ");
		this.error.additionalProperties = new ArrayList<>();
	}

	public ApiErrorModel(HttpStatus httpStatus, Throwable ex) {
		this(ex, UUID.randomUUID().toString());
		this.error.httpStatusError = httpStatus;
		this.error.errorCode = this.error.httpStatusError.value();
	}
}