package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.dto.BeerStyle;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ActiveProfiles("testdocker")
@Tag("docker-compose")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeerClientImplWithDockerComposeIT {

    @Autowired
    private BeerClientImpl beerClient;

    @BeforeEach
    void setUp() {
        Locale.setDefault(Locale.US);
    }

    @BeforeAll
    static void setUp(@Autowired BeerClientImpl beerClient) {
        // Wait for the database to be fully initialized
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> {
                try {
                    BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers(null, null, null, null, null);
                    log.info("### Waiting for database to be fully initialized. Inserted: Beers: {}", page.getTotalElements());
                    return page.getTotalElements() >= 2413;
                } catch (Exception e) {
                    return false;
                }
            });
        log.info("Database is fully initialized.");
    }


    @Test
    @Order(1)
    void testListBeers() {
        BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers(null,
            null,
            null,
            null,
            null);

        log.info("TotalElements: " + page.getTotalElements());
        log.info("NumberOfElements: " + page.getNumberOfElements());
        log.info("TotalPages: " + page.getTotalPages());
        log.info("Number: " + page.getNumber());
        log.info("Pageable: " + page.getPageable());
        log.info("First BeerDTO: " + page.getContent().getFirst().getBeerName());

        assertEquals(2413, page.getTotalElements());
    }

    @Test
    @Order(8)
    void testListBeersWithBeerName() {
        Awaitility.await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> {
                BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers(null, null, null, null, null);
                return page.getTotalElements() >= 2413; // Erwartete Mindestanzahl
            });

        String beerName = "IPA";
        BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers(beerName,
            null,
            null,
            null,
            null);

        log.info("### testListBeersWithBeerName: TotalElements: " + page.getTotalElements());
        log.info("### testListBeersWithBeerName: NumberOfElements: " + page.getNumberOfElements());
        log.info("### testListBeersWithBeerName: TotalPages: " + page.getTotalPages());
        log.info("### testListBeersWithBeerName: Number: " + page.getNumber());
        log.info("### testListBeersWithBeerName: Pageable: " + page.getPageable());
        log.info("### testListBeersWithBeerName: First BeerDTO: " + page.getContent().getFirst().getBeerName());

        assertEquals(336, page.getTotalElements());  

        assertTrue(page.getContent().stream().allMatch(beer -> beer.getBeerName().toLowerCase().contains(beerName.toLowerCase())),
            "Alle gefundenen Biere sollten '" + beerName + "' im Namen haben");

        log.info("Gefundene Biere mit Namen '{}': {}", beerName, page.getTotalElements());
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
