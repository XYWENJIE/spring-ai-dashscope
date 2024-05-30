package org.springframework.ai.dashscope.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Abstract class, providing basic services for the abstract API of DashScope.
 * @param <I>
 * @param <O>
 * @param <TO>
 */
public abstract class AbstractDashScopeService<I,O,TO> {

	private static final String apiVersion = "v1";
	private static final String httpUrl = "https://dashscope.aliyuncs.com/api/" + apiVersion;
	private static final String webSocket = String.format("wss://dashscope.aliyuncs.com/api-ws/%s/inference/", apiVersion);
	private static final Logger logger = LoggerFactory.getLogger(AbstractDashScopeService.class);
	protected final RestClient restClient;
	protected final WebClient webClient;
	protected final String accessToken;
	protected String requestUrl = "";

	public AbstractDashScopeService(String accessToken,String requestUrl) {
		this.restClient = RestClient.builder().baseUrl(httpUrl).defaultHeaders(httpHeaders -> {
			httpHeaders.setBearerAuth(accessToken);
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			addHttpHeaders(httpHeaders);
		}).build();
		this.webClient = WebClient.builder().baseUrl(httpUrl).defaultHeaders(httpHeaders -> {
			httpHeaders.setBearerAuth(accessToken);
			//httpHeaders.set("X-DashScope-SSE","enable");
			httpHeaders.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			addHttpHeaders(httpHeaders);
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

	protected Flux<O> chatCompletionStream(I request){
		return null;
	}

	protected O queryTask(String taskId){
		return null;
	}

	protected O submitTask(I request){
		return null;
	}
	
	protected O internalInvocation(I request,Class<O> clazz) {
        try {
			logger.info("Request URI:{}",requestUrl);
            logger.info(new ObjectMapper().writeValueAsString(request));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return this.restClient.post().uri(requestUrl).body(request).retrieve().body(clazz);
	}

	protected Flux<O> internalInvocationStream(I request, Class<O> clazz){
		try {
			logger.info(new ObjectMapper().writeValueAsString(request));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return this.webClient.post().uri(requestUrl).bodyValue(request).retrieve().bodyToFlux(clazz);
	}

	protected void addHttpHeaders(HttpHeaders httpHeaders){

	}

}
