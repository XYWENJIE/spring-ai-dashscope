package org.springframework.ai.dashscope.metadata.support;

import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.metadata.QwenRateLimit;
import org.springframework.http.ResponseEntity;

public class DashCopeResponseHeaderExtractor {
	
	public static RateLimit extractAiResponseHeaders(ResponseEntity<DashsCopeService.ChatCompletion> response) {
		//Long requestLimit = getHeaderAsLong(response,)
		return new QwenRateLimit(null,null,null,null,null,null);
	}

}
