package org.springframework.ai.dashscope.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.dashscope.DashsCopeService;
import org.springframework.ai.dashscope.DashsCopeTestConfiguration;
import org.springframework.ai.parser.ListOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.DefaultConversionService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = {DashsCopeTestConfiguration.class})
@EnabledIfEnvironmentVariable(named = "DASHSCOPE_API_KEY",matches = ".+")
public class QwenChatClientIT {
	
	private final Logger logger = LoggerFactory.getLogger(QwenChatClientIT.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private ChatClient chatClient;
	
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

}
