package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.databind.JsonNode;
import guru.springframework.spring6resttemplate.dto.BeerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerClientImpl implements BeerClient {
    
    private final RestTemplateBuilder restTemplateBuilder;
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final String GET_BEER_URL = "/api/v1/beer/listBears";
    
    @Override
    public Page<BeerDTO> listBeers() {
        RestTemplate restTemplate =restTemplateBuilder.build();

        ResponseEntity<String> stringResponseEntity = restTemplate.getForEntity(BASE_URL + GET_BEER_URL, String.class);
        log.info("String Response was: " + stringResponseEntity.getBody());

        ResponseEntity<Map> mapResponseEntity = restTemplate.getForEntity(BASE_URL + GET_BEER_URL, Map.class);
        log.info("Map Response was: " + mapResponseEntity.getBody());

        ResponseEntity<JsonNode> jsonResponseEntity = restTemplate.getForEntity(BASE_URL + GET_BEER_URL, JsonNode.class);
        log.info("Json Response was: " + jsonResponseEntity.getBody());
        log.info("Json Response content was: " + jsonResponseEntity.getBody().findPath("content"));
        jsonResponseEntity.getBody().findPath("content")
            .elements().forEachRemaining(jsonNode ->  {
                log.info("Get Beername: " + jsonNode.get("beerName").asText());
            });
        
        return null;
    }
}
