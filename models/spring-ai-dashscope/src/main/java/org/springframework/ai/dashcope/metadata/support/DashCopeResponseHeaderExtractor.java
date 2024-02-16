package org.springframework.ai.dashcope.metadata.support;

import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.dashcope.DashCopeService.ChatCompletion;
import org.springframework.ai.dashcope.metadata.QwenRateLimit;
import org.springframework.http.ResponseEntity;

public class DashCopeResponseHeaderExtractor {
	
	public static RateLimit extractAiResponseHeaders(ResponseEntity<ChatCompletion> response) {
		//Long requestLimit = getHeaderAsLong(response,)
		return new QwenRateLimit(null,null,null,null,null,null);
	}

}
