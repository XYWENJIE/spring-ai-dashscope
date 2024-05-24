package org.springframework.ai.dashscope.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.DashsCopeTestConfiguration;
import org.springframework.ai.dashscope.api.tool.MockWeatherService;
import org.springframework.ai.dashscope.metadata.support.Model;
import org.springframework.ai.dashscope.qwen.QWenChatOptions;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.parser.ListOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.DefaultConversionService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

@SpringBootTest(classes = {DashsCopeTestConfiguration.class})
@EnabledIfEnvironmentVariable(named = "DASHSCOPE_API_KEY",matches = ".+")
public class QwenChatModelIT {
	
	private final Logger logger = LoggerFactory.getLogger(QwenChatModelIT.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private ChatModel chatClient;

	@Autowired
	private StreamingChatModel streamingChatClient;
	
	/**
	 * 测试用例-将阿里云返回JSON字符串反序列化成ChatCompletion
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	@Test
	public void testDeserializeChatCompletionFromJson() throws JsonMappingException, JsonProcessingException {
		String jsonBody = "{\"output\":{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\",\"content\":\"1. Blackbeard (Edward Teach): He was one of the most notorious pirates of the Golden Age of Piracy, known for his long black beard and terrifying appearance. Blackbeard blockaded the entrance to the Chesapeake Bay in 1718 and captured several ships. It is believed that he turned to piracy after being a privateer during Queen Anne's War.\\n  2. William Kidd: He was a Scottish sailor who was commissioned as a privateer to hunt for French ships during the Nine Years' War. However, he soon turned to piracy and was eventually caught and executed for his crimes. Some historians argue that Kidd was unjustly accused and that he may have been framed by corrupt officials.\\n  3. Calico Jack (John Rackham): He was an English pirate who operated in the Caribbean during the early 18th century. He was known for his colorful clothing and for having two female pirates in his crew, Anne Bonny and Mary Read. Calico Jack was eventually captured and hanged for his crimes.\\n\\nThese pirates turned to piracy for various reasons, including financial gain, adventure, and revenge against the Spanish and British empires. The Golden Age of Piracy lasted from the late 1600s to the early 1700s, and it was a time when piracy was widespread and lucrative due to the large amount of trade that took place in the Caribbean and along the eastern coast of North America.\"}}]},\"usage\":{\"total_tokens\":315,\"output_tokens\":297,\"input_tokens\":18},\"request_id\":\"ca24cfc3-2ba0-944c-8529-3363e5ed45ab\"}";
		DashsCopeService.ChatCompletion chatCompletion = objectMapper.readValue(jsonBody, DashsCopeService.ChatCompletion.class);
		logger.info(chatCompletion.toString());
	}
	
	@Test
	public void roleTest() {
		logger.info("chatClient对象"+chatClient);
		UserMessage userMessage = new  UserMessage("Tell me about 3 famous pirates from the Golden Age of Piracy and why they did");
		Prompt prompt = new Prompt(List.of(userMessage));
		ChatResponse chatResponse = this.chatClient.call(prompt);
		logger.info("AI回答：{}",chatResponse.getResult().toString());
		assertThat(chatResponse.getResults()).hasSize(1);
		assertThat(chatResponse.getResults().get(0).getOutput().getContent()).contains("Blackbeard");
	}
	
	@Test
	public void outputParser() {
		DefaultConversionService conversionService = new DefaultConversionService();
		ListOutputParser listOutputParser = new ListOutputParser(conversionService);
		
		String format = listOutputParser.getFormat();
		String template = """
				List five {subject}
				{format}
				""";
		PromptTemplate promptTemplate = new PromptTemplate(template,
				Map.of("subject","ice cream flavors","format",format));
		Prompt prompt = new Prompt(promptTemplate.createMessage());
		Generation generation = this.chatClient.call(prompt).getResult();
		logger.info(generation.toString());
		
		List<String> list = listOutputParser.parse(generation.getOutput().getContent());
		assertThat(list).hasSize(5);
	}
	
	@Test
	public void functionCallTest() {
		UserMessage userMessage = new UserMessage("我想查询这几个城市天气 旧金山,东京,和巴黎?");
		List<Message> messages = new ArrayList<>(List.of(userMessage));
		
		var promptOption = QWenChatOptions.builder().withModel(Model.QWen_TURBO)
				.withFunctionCallbacks(List.of(FunctionCallbackWrapper.builder(new MockWeatherService())
						.withName("getCurrentWeather")
						.withDescription("Get the weather in location")
						.withResponseConverter((response) -> {
							return  "location："+response.location() +";Temperature:"+response.temp() + ";Temperature unit:" + response.unit().unitName;
						})
						.build()))
				.build();
		ChatResponse chatResponse = chatClient.call(new Prompt(messages,promptOption));
		
		//logger.info("Response:{}",chatResponse);
		logger.info("AI回答:{}",chatResponse.getResult().getOutput().getContent());
	}

	@Test
	public void streamFunctionCallTest(){

		UserMessage userMessage = new UserMessage("我想查询这几个城市天气 旧金山,东京,和巴黎?");

		List<Message> messages = new ArrayList<>(List.of(userMessage));
		var promptOption = QWenChatOptions.builder().withModel(Model.QWen_TURBO)
				.withFunctionCallbacks(List.of(FunctionCallbackWrapper.builder(new MockWeatherService())
						.withName("getCurrentWeather")
						.withDescription("Get the weather in location")
						.withResponseConverter((response) -> {
							logger.info("withResponseConverter");
							return  "location："+response.location() +";Temperature:"+response.temp() + ";Temperature unit:" + response.unit().unitName;
						}).build()))
				.build();
		Flux<ChatResponse> chatResponse = streamingChatClient.stream(new Prompt(messages,promptOption));

		String content = chatResponse.collectList().block().stream().map(ChatResponse::getResults).flatMap(List::stream).map(Generation::getOutput).map(AssistantMessage::getContent).collect(Collectors.joining());
		logger.info("AI:{}",content);

	}


	@Test
	void multiModalityEmbeddedImage() {


		var userMessage = new UserMessage("Explain what do you see on this picture?",
				List.of(new Media(MimeTypeUtils.IMAGE_PNG, "https://bkimg.cdn.bcebos.com/pic/a686c9177f3e67096d187aa634c79f3df8dc554a?x-bce-process=image/format,f_auto/watermark,image_d2F0ZXIvYmFpa2UyNzI,g_7,xp_5,yp_5,P_20/resize,m_lfit,limit_1,h_1080")));

		ChatResponse response = chatClient.call(new Prompt(List.of(userMessage),
				QWenChatOptions.builder().withModel(Model.QWen_VL_PLUS).build()));

		logger.info(response.getResult().getOutput().getContent());
		assertThat(response.getResult().getOutput().getContent()).contains("bananas", "apple");
	}

}
