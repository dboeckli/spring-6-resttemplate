package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.dto.BeerStyle;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static guru.springframework.spring6resttemplate.test.util.docker.MvcServerTestUtil.checkMvcDatabaseInitDone;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ActiveProfiles("testdocker")
@Tag("docker-compose")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeerClientImplWithDockerComposeIT {

    @Autowired
    private BeerClientImpl beerClient;

    @TestConfiguration
    static class JacksonTestConfig {

        @Bean
        @Primary
        ObjectMapper testObjectMapper(ObjectMapper springConfiguredObjectMapper) {

            // Jackson 3: Mapper ist immutable -> rebuild() + enable(...) + build()
            return springConfiguredObjectMapper.rebuild()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
        }
    }

    @BeforeAll
    static void setUp(@Autowired BeerClientImpl beerClient) {
        checkMvcDatabaseInitDone(beerClient);
    }

    @Test
    @Order(1)
    void testListBeers() {
        BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers(null,
            null,
            null,
            1,
            25);

        log.info("TotalElements: " + page.getTotalElements());
        log.info("NumberOfElements: " + page.getNumberOfElements());
        log.info("TotalPages: " + page.getTotalPages());
        log.info("Number: " + page.getNumber());
        log.info("Pageable: " + page.getPageable());
        log.info("First BeerDTO: " + page.getContent().getFirst().getBeerName());

        assertEquals(503, page.getTotalElements());
    }

    @Test
    @Order(8)
    void testListBeersWithBeerName() {
        Awaitility.await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> {
                BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers(null, null, null, null, null);
                return page.getTotalElements() >= 503;
            });

        String givenBeerName = "IPA";
        BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers(givenBeerName,
            null,
            null,
            null,
            null);

        assertEquals(60, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(beer -> beer.getBeerName().toLowerCase().contains(givenBeerName.toLowerCase())),
            "Alle gefundenen Biere sollten '" + givenBeerName + "' im Namen haben");
        log.info("Gefundene Biere mit Namen '{}': {}", givenBeerName, page.getTotalElements());
    }

    @Test
    @Order(3)
    void testGetBeerById() {
        BeerDTO beer = beerClient.listBeers().getContent().getFirst();
        BeerDTO beerByIdDTO = beerClient.getBeerById(beer.getId());

        assertEquals(beer.getId(), beerByIdDTO.getId());
    }

    @Test
    @Order(11)
    void testCreateBeer() {
        BeerDTO newBeer = BeerDTO.builder()
            .beerName("Guguseli")
            .upc("abc")
            .quantityOnHand(6)
            .price(BigDecimal.valueOf(20))
            .beerStyle(BeerStyle.LAGER)
            .build();
        BeerDTO createdBeerDTO = beerClient.createBeer(newBeer);
        BeerDTO beerByIdDTO = beerClient.getBeerById(createdBeerDTO.getId());

        assertEquals(newBeer.getBeerName(), beerByIdDTO.getBeerName());
    }

    @Test
    @Order(21)
    void testUpdateBeer() {
        BeerDTO beerToUpdate = beerClient.listBeers().getContent().getFirst();
        beerToUpdate.setBeerName("updated beer name");

        BeerDTO updatedBeerDTO = beerClient.updateBeer(beerToUpdate);

        assertEquals(beerToUpdate.getBeerName(), updatedBeerDTO.getBeerName());
    }

    @Test
    @Order(91)
    void testDeleteBeer() {
        BeerDTO beerToDelete = beerClient.listBeers().getContent().getFirst();

        beerClient.deleteBeer(beerToDelete.getId());

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class,
            () -> beerClient.getBeerById(beerToDelete.getId()
            ));
        assertEquals(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), thrown.getStatusCode());

        log.error("Exception Status: {} -  {}", thrown.getStatusCode(), thrown.getStatusText());
        log.error("Exception - header {} ", thrown.getResponseHeaders());
        log.error("\n Exception - message  {} ", thrown.getResponseBodyAsString());
    }
}
