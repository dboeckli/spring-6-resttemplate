package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.dto.BeerStyle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
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
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = getQueryForListBeer(beerName, beerStyle, showInventory, pageNumber, pageSize);
        ResponseEntity<BeerDTOPageImpl<BeerDTO>> pageResponseEntity = restTemplate.exchange(
            uriComponentsBuilder.toUriString(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<BeerDTOPageImpl<BeerDTO>>() {}
        );
        return pageResponseEntity.getBody();
    }

    @Override
    public BeerDTO getBeerById(UUID beerId) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate.getForObject(GET_BEER_BY_ID_PATH, BeerDTO.class, beerId);
    }

    @Override
    public BeerDTO createBeer(BeerDTO newBeer) {
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
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.put(PUT_UPDATE_BEER_PATH, beerDTO, beerDTO.getId());
        return this.getBeerById(beerDTO.getId());
    }

    @Override
    public void deleteBeer(UUID id) {
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
