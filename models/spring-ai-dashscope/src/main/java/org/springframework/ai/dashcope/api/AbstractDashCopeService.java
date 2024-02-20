package org.springframework.ai.dashcope.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class AbstractDashCopeService<I,O,SO> {
	
	private static final Log logger = LogFactory.getLog(AbstractDashCopeService.class);
	
	@JsonInclude(Include.NON_NULL)
	public record ResponseError(
			@JsonProperty("code")String code,
			@JsonProperty("message") String message,
			@JsonProperty("request_id")String requestId) {}
	
	protected O chatCompletion(I request) {
		return null;
		
	}
	
	protected O internalInvocation(I request,Class<O> clazz) {
		return null;
		
	}

}
