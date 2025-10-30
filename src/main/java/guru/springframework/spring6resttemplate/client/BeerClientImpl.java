package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.dto.BeerStyle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerClientImpl implements BeerClient {

    private final RestTemplateBuilder restTemplateBuilder;

    public static final String GET_LIST_BEER_PATH = "/api/v1/beer/listBeers";
    public static final String GET_BEER_BY_ID_PATH = "/api/v1/beer/getBeerById/{beerId}";
    public static final String POST_CREATE_BEER_PATH = "/api/v1/beer/createBeer";
    public static final String PUT_UPDATE_BEER_PATH = "/api/v1/beer/editBeer/{beerId}";
    public static final String DELETE_BEER_BY_ID_PATH = "/api/v1/beer/deleteBeer/{beerId}";

    public Page<BeerDTO> listBeers() {
        return this.listBeers(null, null, null, null, null);
    }

    @Override
    public Page<BeerDTO> listBeers(String beerName,
                                   BeerStyle beerStyle,
                                   Boolean showInventory,
                                   Integer pageNumber,
                                   Integer pageSize) {
        log.info("### ListBeer: beerName: {}, beerStyle: {}, showInventory: {}, pageNumber: {}, pageSize: {}", beerName, beerStyle, showInventory, pageNumber, pageSize);
        RestTemplate restTemplate = restTemplateBuilder.build();
        UriComponentsBuilder uriComponentsBuilder = getQueryForListBeer(beerName, beerStyle, showInventory, pageNumber, pageSize);

        //ResponseEntity<String> stringResponseEntity = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), String.class);
        //log.debug("String Response was: " + stringResponseEntity.getBody());

        //ResponseEntity<Map> mapResponseEntity = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), Map.class);
        //log.debug("Map Response was: " + mapResponseEntity.getBody());

        //ResponseEntity<JsonNode> jsonResponseEntity = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), JsonNode.class);
        //log.debug("Json Response was: " + jsonResponseEntity.getBody());
        //log.debug("Json Response content was: " + jsonResponseEntity.getBody().findPath("content"));
        //jsonResponseEntity.getBody().findPath("content")
        //    .elements().forEachRemaining(jsonNode -> log.debug("Get Beername: " + jsonNode.get("beerName").asText()));

        ResponseEntity<BeerDTOPageImpl> pageResponseEntity = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), BeerDTOPageImpl.class);
        return pageResponseEntity.getBody();
    }

    @Override
    public BeerDTO getBeerById(UUID beerId) {
        log.info("### GetBeerById: beerId: {}", beerId);
        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate.getForObject(GET_BEER_BY_ID_PATH, BeerDTO.class, beerId);
    }

    @Override
    public BeerDTO createBeer(BeerDTO newBeer) {
        log.info("### CreateBeer: newBeer: {}", newBeer);
        RestTemplate restTemplate = restTemplateBuilder.build();
        //ResponseEntity<BeerDTO> response = restTemplate.postForEntity(POST_CREATE_BEER_PATH, newBeer, BeerDTO.class);
        //return response.getBody();

        URI location = restTemplate.postForLocation(POST_CREATE_BEER_PATH, newBeer);
        if (location != null) {
            return restTemplate.getForObject(location.getPath(), BeerDTO.class);
        } else {
            throw new RuntimeException("Failed to create beer. Location header is missing.");
        }
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beerDTO) {
        log.info("### UpdateBeer: beerDTO: {}", beerDTO);
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.put(PUT_UPDATE_BEER_PATH, beerDTO, beerDTO.getId());
        return this.getBeerById(beerDTO.getId());
    }

    @Override
    public void deleteBeer(UUID id) {
        log.info("### DeleteBeer: id: {}", id);
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.delete(DELETE_BEER_BY_ID_PATH, id);
    }

    private UriComponentsBuilder getQueryForListBeer(String beerName,
                                                     BeerStyle beerStyle,
                                                     Boolean showInventory,
                                                     Integer pageNumber,
                                                     Integer pageSize) {
        
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(GET_LIST_BEER_PATH);
        if (beerName != null && !beerName.isEmpty()) {
            uriComponentsBuilder.queryParam("beerName", beerName);
        }
        if (beerStyle != null) {
            uriComponentsBuilder.queryParam("beerStyle", beerStyle);
        }
        if (showInventory != null && showInventory) {
            uriComponentsBuilder.queryParam("showInventory", showInventory);
        }
        if (pageNumber != null) {
            uriComponentsBuilder.queryParam("pageNumber", pageNumber);
        }
        if (pageSize != null) {
            uriComponentsBuilder.queryParam("pageSize", pageSize);
        }
        return uriComponentsBuilder;
    }
}
