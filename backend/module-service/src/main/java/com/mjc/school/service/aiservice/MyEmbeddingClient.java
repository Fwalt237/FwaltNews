package com.mjc.school.service.aiservice;

import com.mjc.school.service.config.properties.GeminiProperties;
import com.mjc.school.service.aiservice.dto.EmbeddingRequest;
import com.mjc.school.service.aiservice.dto.EmbeddingResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MyEmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(MyEmbeddingClient.class);
    private static final int VECTOR_LENGTH = 768;

    private final RestTemplate restTemplate;
    private final GeminiProperties geminiProperties;

    @Autowired
    public MyEmbeddingClient(RestTemplate restTemplate, GeminiProperties geminiProperties){
        this.restTemplate=restTemplate;
        this.geminiProperties=geminiProperties;
    }

    public float[] getEmbedding(String text){
        String url = UriComponentsBuilder.fromHttpUrl(geminiProperties.getEmbeddingModel())
                        .queryParam("key",geminiProperties.getApiKey()).toUriString();

        EmbeddingRequest request = EmbeddingRequest.of("models/gemini-embedding-001", text,VECTOR_LENGTH);

        try{
            ResponseEntity<EmbeddingResponse> response = restTemplate.postForEntity(url,request,EmbeddingResponse.class);

            if(response.getStatusCode().is2xxSuccessful() && response.getBody()!=null){
                List<Double> values = response.getBody().embedding().values();

                if (values.size() != VECTOR_LENGTH) {
                    log.error("Dimension mismatch, expected {}, but returned {}", VECTOR_LENGTH, values.size());
                    throw new IllegalStateException("Returned incorrect vector dimensions: " + values.size());
                }

                float[] vector = new float[VECTOR_LENGTH];
                for(int i=0;i<VECTOR_LENGTH;i++){
                    vector[i]=values.get(i).floatValue();
                }
                log.info("Successfully retrieved embedding. Dimensions: {}", VECTOR_LENGTH);
                return vector;
            }else {
                throw new RuntimeException("Gemini API Error: " + response.getStatusCode());
            }
        }catch(Exception e){
            log.error("Failed to fetch embedding: {}", e.getMessage());
            throw new RuntimeException("Embedding generation failed", e);
        }
    }
}
