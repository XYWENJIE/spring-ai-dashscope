package org.springframework.ai.dashscope.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.ai.dashscope.qwen.api.QWenDashScopeService;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

public abstract class AbstractDashScopeService<I,O,SO> {

	private static final String apiVersion = "v1";
	private static final String httpUrl = "https://dashscope.aliyuncs.com/api/" + apiVersion;
	private static final String webSocket = String.format("wss://dashscope.aliyuncs.com/api-ws/%s/inference/", apiVersion);
	private static final Log logger = LogFactory.getLog(AbstractDashScopeService.class);
	protected final RestClient restClient;
	protected final WebClient webClient;
	protected final String accessToken;
	protected String requestUrl = "";

	public AbstractDashScopeService(String accessToken,String requestUrl) {
		this.restClient = RestClient.builder().baseUrl(httpUrl).defaultHeaders(httpHeaders -> {
			httpHeaders.setBearerAuth(accessToken);
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		}).build();
		this.webClient = WebClient.builder().baseUrl(httpUrl).defaultHeaders(httpHeaders -> {
			httpHeaders.setBearerAuth(accessToken);
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		}).build();
		this.accessToken = accessToken;
		this.requestUrl = requestUrl;
	}



	@JsonInclude(Include.NON_NULL)
	public record ResponseError(
			@JsonProperty("code")String code,
			@JsonProperty("message") String message,
			@JsonProperty("request_id")String requestId) {}
	
	protected O chatCompletion(I request) {
		return null;
	}

	protected Flux<O> chatCompletionStream(QWenDashScopeService.QWenChatRequest request){
		return null;
	}
	
	protected O internalInvocation(I request,Class<O> clazz) {
		return this.restClient.post().uri(requestUrl).body(request).retrieve().body(clazz);
	}

	protected Flux<O> internalInvocationStream(I request, Class<O> clazz){
		return this.webClient.post().uri(requestUrl).bodyValue(request).retrieve().bodyToFlux(clazz);
	}

}
