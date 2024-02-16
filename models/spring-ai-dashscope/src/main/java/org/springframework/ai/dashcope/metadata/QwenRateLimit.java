package org.springframework.ai.dashcope.metadata;

import java.time.Duration;

import org.springframework.ai.chat.metadata.RateLimit;

public class QwenRateLimit implements RateLimit {
	
	private final Long requestsLimit; 
	
	private final Long requestsRemaining;
	
	private final Long tokensLimit;
	
	private final Long tokensRemaining;
	
	private final Duration requestReset;
	
	private final Duration tokensReset;
	
	public QwenRateLimit(Long requestsLimit,Long requestsRemaining,Duration requestsReset,Long tokensLimit,
			Long tokensRemaining,Duration tokensReset) {
		this.requestsLimit = requestsLimit;
		this.requestsRemaining = requestsRemaining;
		this.tokensLimit = tokensLimit;
		this.tokensRemaining = tokensRemaining;
		this.requestReset = requestsReset;
		this.tokensReset = tokensReset;
	}

	@Override
	public Long getRequestsLimit() {
		return this.requestsLimit;
	}

	@Override
	public Long getRequestsRemaining() {
		return this.requestsRemaining;
	}

	@Override
	public Duration getRequestsReset() {
		return this.requestReset;
	}

	@Override
	public Long getTokensLimit() {
		return this.tokensLimit;
	}

	@Override
	public Long getTokensRemaining() {
		return this.tokensRemaining;
	}

	@Override
	public Duration getTokensReset() {
		return this.tokensReset;
	}

}
