package org.springframework.ai.dashscope.api.tool;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class MockWeatherService implements Function<MockWeatherService.Request, MockWeatherService.Response> {
	
	@JsonInclude(Include.NON_NULL)
	@JsonClassDescription("Weather API request")
	public record Request(
			@JsonProperty(required = true,value = "location") @JsonPropertyDescription("The city and state e.g. San Francisco, CA") String location,
			@JsonProperty(required = true,value = "lat") @JsonPropertyDescription("The city latitude") double lat,
			@JsonProperty(required = true,value = "lon") @JsonPropertyDescription("The city longitude") double lon,
			@JsonProperty(required = true,value = "unit") @JsonPropertyDescription("Temperature unit") Unit unit) {}
	
	public record Response(String location,double temp,double feels_link,double temp_min,
			double temp_max,int pressure,int humidity,Unit unit) {}
	
	public enum Unit{
		C("metric"),F("imperial");

		public final String unitName;

		private Unit(String unitName) {
			this.unitName = unitName;
		}
	}

	@Override
	public Response apply(Request request) {
		double temperature = 0;
		if(request.location().contains("巴黎")) {
			temperature = 15;
		}else if(request.location().contains("东京")) {
			temperature = 10;
		}else if(request.location().contains("旧金山")) {
			temperature = 30;
		}
		return new Response(request.location,temperature,15,20,2,53,45,Unit.C);
	}

}
