package com.mjc.school.service.aiservice;


import com.mjc.school.repository.airepo.impl.ChatMessagesRepository;
import com.mjc.school.repository.airepo.model.ChatMessages;
import com.mjc.school.service.aiservice.dto.ChatResponse;
import com.mjc.school.service.aiservice.dto.ChatTurn;
import com.mjc.school.service.config.properties.AiAssistantProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.messages.Message;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private static final String SYSTEM_PROMPT = """
        ## ROLE
        You are FwaltNews AI, an intelligent news assistant for a news aggregation platform.
        
        ## TOOL GUIDELINES
        - ALWAYS fetch real data before answering news questions unless info is already in the context.
        - IF user asks about a general topic or "what's the latest on X" -> USE `searchNewsByTopic`.
        - IF user asks for categories (tech, sports, business) -> USE `getLatestNewsByTag`.
        - IF user asks for a summary or details of a specific article -> USE `getFullArticle`.
        - IF user asks for a daily digest, briefing, or "what happened today" -> USE `getTopRecentNews`.
        - IF no relevant articles are found, say so honestly and DO NOT include the ARTICLES_FOUND tag.
        - IF the user refers to a previously listed article (e.g., "the first one", "tell me more about ID:123")
         -> USE `getFullArticle` with the known ID.
        - DO NOT re-run `searchNewsByTopic` or `getTopRecentNews` for articles already displayed.
        
        ## OUTPUT FORMAT
        - LIST articles found as: "[ID:123] Article Title (Author, Date)".
        - Use bullet points and keep responses concise.
        - TIME INTERPRETATION: Today=24h, Yesterday=48h, Week=168h, Month=720h.
        
        ## METADATA
        At the end of your response, IF AND ONLY IF you are providing new search results, add this line:
        ARTICLES_FOUND:[id1,id2,id3]
        
        EXCEPTIONS:
        - DO NOT include this line when summarizing an article already discussed.
        - DO NOT include this line for general greetings or "hello".
        """;

    private final ChatClient chatClient;
    private final ChatMessagesRepository chatMessagesRepository;
    private final AiAssistantProperties aiProperties;

    @Autowired
    public AiAssistantService(ChatClient.Builder chatClientBuilder, ChatMessagesRepository chatMessagesRepository,
                              NewsTools newsTools, AiAssistantProperties aiProperties){
        this.chatClient=chatClientBuilder.defaultTools(newsTools).build();
        this.chatMessagesRepository=chatMessagesRepository;
        this.aiProperties=aiProperties;
    }

    @Transactional
    public ChatResponse chat(String sessionId, String userMessage){

        saveMessage(sessionId,"user",userMessage);
        List<Message> history = buildHistory(sessionId);

        String aiReply;
        try{
            aiReply = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .messages(history)
                    .call()
                    .content();
        }catch(Exception e){
            log.error("AI service error for session {}: {}",sessionId,e.getMessage());
            return new ChatResponse("I'm sorry, I'm having internal issues. Try again", List.of());
        }

        saveMessage(sessionId,"assistant",aiReply);

        List<Long> articleIds = parseArticleIds(aiReply);
        String cleanReply = stripeArticleIdsLine(aiReply);

        return new ChatResponse(cleanReply, articleIds);
    }

    @Transactional(readOnly=true)
    public List<ChatTurn> getHistory(String sessionId){
        return chatMessagesRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(m->new ChatTurn(m.getRole(),m.getContent(),m.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void clearHistory(String sessionId){
        chatMessagesRepository.deleteBySessionId(sessionId);
    }

    private void saveMessage(String sessionId, String role, String content){
        ChatMessages message = new ChatMessages();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        chatMessagesRepository.save(message);
    }

    private List<Message> buildHistory(String sessionId){
        List<ChatMessages> stored = chatMessagesRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        int from = Math.max(0, stored.size() - aiProperties.getMaxHistoryTurns());
        List<ChatMessages> window = stored.subList(from,stored.size());
        List<Message> messages = new ArrayList<>();

        for(ChatMessages msg : window){
            if(msg.getRole().equals("user")){
                messages.add(new UserMessage(msg.getContent()));
            }else{
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        return messages;
    }

    private List<Long> parseArticleIds(String reply){
        if(reply == null) return List.of();

        Pattern pattern = Pattern.compile("ARTICLES_FOUND:\\[([\\d,\\s]+)]");
        Matcher matcher = pattern.matcher(reply);

        if (matcher.find()){
            return Arrays.stream(matcher.group(1).split(","))
                    .map(String::trim)
                    .filter(s->!s.isEmpty())
                    .map(s -> {
                        try { return Long.parseLong(s); }
                        catch (NumberFormatException e) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    private String stripeArticleIdsLine(String reply){
        if(reply==null) return "";
        return reply.replaceAll("(?m)^ARTICLES_FOUND:\\[.*]\\s*$","").trim();
    }
}
