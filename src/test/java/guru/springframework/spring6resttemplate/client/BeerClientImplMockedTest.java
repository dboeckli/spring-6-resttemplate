package guru.springframework.spring6resttemplate.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.dto.BeerStyle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
@Slf4j
class BeerClientImplMockedTest {

    @Value("${rest.template.base.url}")
    private String baseUrl;
    
    private BeerClient beerClient;

    @Autowired
    MockRestServiceServer mockServer;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());
    
    @Autowired
    ObjectMapper objectMapper;
    
    private BeerDTO givenBeerDTO;
    private String givenBeerDTOJson; 

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);

        givenBeerDTO = getBeerDto();
        givenBeerDTOJson = objectMapper.writeValueAsString(givenBeerDTO);
    }

    @Test
    void testListBeers() throws JsonProcessingException {
        String givenPayload = objectMapper.writeValueAsString(getPage());

        mockServer.expect(method(HttpMethod.GET))
            .andExpect(requestTo(baseUrl + BeerClientImpl.GET_LIST_BEER_PATH))
            .andRespond(withSuccess(givenPayload, MediaType.APPLICATION_JSON));
        
        BeerDTOPageImpl<BeerDTO> beerPage = (BeerDTOPageImpl<BeerDTO>)beerClient.listBeers(null, 
            null, 
            null, 
            null,
            null);

        assertAll(() -> {
            assertNotNull(beerPage);
            assertEquals(26, beerPage.getTotalElements());
            assertEquals(1, beerPage.getNumberOfElements());
            assertEquals(2, beerPage.getTotalPages());
            assertEquals(1, beerPage.getNumber());
            assertEquals("Mango Bobs", beerPage.getContent().getFirst().getBeerName());
        });
    }

    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + BeerClientImpl.GET_LIST_BEER_PATH)
            .queryParam("beerName", "ALE")
            .build().toUri();

        mockServer.expect(method(HttpMethod.GET))
            .andExpect(requestTo(uri))
            .andExpect(queryParam("beerName", "ALE"))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> beerPage = beerClient.listBeers("ALE", null, null, null, null);

        assertThat(beerPage.getContent().size()).isEqualTo(1);
    }

    @Test
    void testGetById() {
        mockServer.expect(method(HttpMethod.GET))
            .andExpect(requestToUriTemplate(baseUrl + BeerClientImpl.GET_BEER_BY_ID_PATH, givenBeerDTO.getId()))
            .andRespond(withSuccess(givenBeerDTOJson, MediaType.APPLICATION_JSON));

        BeerDTO responseDto = beerClient.getBeerById(givenBeerDTO.getId());
        assertThat(responseDto.getId()).isEqualTo(givenBeerDTO.getId());
    }

    @Test
    void testCreateBeer() {
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH).build(givenBeerDTO.getId());

        mockServer.expect(method(HttpMethod.POST))
            .andExpect(requestTo(baseUrl + BeerClientImpl.POST_CREATE_BEER_PATH))
            .andRespond(withAccepted().location(uri));

        mockServer.expect(method(HttpMethod.GET))
            .andExpect(requestToUriTemplate(baseUrl + BeerClientImpl.GET_BEER_BY_ID_PATH, givenBeerDTO.getId()))
            .andRespond(withSuccess(givenBeerDTOJson, MediaType.APPLICATION_JSON));

        BeerDTO responseDto = beerClient.createBeer(givenBeerDTO);
        assertThat(responseDto.getId()).isEqualTo(givenBeerDTO.getId());
    }

    @Test
    void testUpdateBeer() {
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH).build(givenBeerDTO.getId());
        
        mockServer.expect(method(HttpMethod.PUT))
            .andExpect(requestToUriTemplate(baseUrl + BeerClientImpl.PUT_UPDATE_BEER_PATH, givenBeerDTO.getId()))
            .andRespond(withAccepted().location(uri));

        mockServer.expect(method(HttpMethod.GET))
            .andExpect(requestToUriTemplate(baseUrl + BeerClientImpl.GET_BEER_BY_ID_PATH, givenBeerDTO.getId()))
            .andRespond(withSuccess(givenBeerDTOJson, MediaType.APPLICATION_JSON));

        BeerDTO responseDto = beerClient.updateBeer(givenBeerDTO);
        assertThat(responseDto.getId()).isEqualTo(givenBeerDTO.getId());
    }

    @Test
    void testDeleteBeer() {
        mockServer.expect(method(HttpMethod.DELETE))
            .andExpect(requestToUriTemplate(baseUrl + BeerClientImpl.DELETE_BEER_BY_ID_PATH, givenBeerDTO.getId()))
            .andRespond(withNoContent());

        beerClient.deleteBeer(givenBeerDTO.getId());
        mockServer.verify();
    }

    @Test
    void testDeleteBeerNoFound() {
        mockServer.expect(method(HttpMethod.DELETE))
            .andExpect(requestToUriTemplate(baseUrl + BeerClientImpl.DELETE_BEER_BY_ID_PATH, givenBeerDTO.getId()))
            .andRespond(withResourceNotFound());

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () ->
            beerClient.deleteBeer(givenBeerDTO.getId())
        );
        assertEquals(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), thrown.getStatusCode());
        mockServer.verify();
    }

    private BeerDTO getBeerDto(){
        return BeerDTO.builder()
            .id(UUID.randomUUID())
            .price(new BigDecimal("10.99"))
            .beerName("Mango Bobs")
            .beerStyle(BeerStyle.IPA)
            .quantityOnHand(500)
            .upc("123245")
            .build();
    }
    
    private BeerDTOPageImpl getPage() {
        return new BeerDTOPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
    }

}
