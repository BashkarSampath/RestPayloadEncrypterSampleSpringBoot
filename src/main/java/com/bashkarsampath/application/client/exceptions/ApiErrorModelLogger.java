package com.bashkarsampath.application.client.exceptions;

import org.springframework.boot.logging.LogLevel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiErrorModelLogger {
	@Getter
	private ApiErrorModel model;

	public ApiErrorModelLogger(ApiErrorModel model) {
		this.model = model;
	}

	private static final ObjectMapper mapper = new ObjectMapper();

	public void logToConsole(LogLevel level) {
		try {
			switch (level) {
			case ERROR:
				log.error(mapper.writeValueAsString(model));
				break;
			case WARN:
				log.warn(mapper.writeValueAsString(model));
				break;
			default: {
				log.info(mapper.writeValueAsString(model));
			}
			}
		} catch (JsonProcessingException jsonException) {
			log.error(model.toString());
		}
	}

	public void clearDebugMessage() {
		model.getError().setDebugMessage(null);
	}
}
