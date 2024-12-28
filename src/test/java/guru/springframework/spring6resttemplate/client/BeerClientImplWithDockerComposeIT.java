package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.dto.BeerStyle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ActiveProfiles("testdocker")
@Tag("docker-compose")
class BeerClientImplWithDockerComposeIT {
    
    @Autowired
    private BeerClientImpl beerClient;

    @Test
    void listBeers() {
        BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>)beerClient.listBeers(null, 
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
    void listBeersWithBeerName() {
        BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>)beerClient.listBeers("ALE", 
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

        assertEquals(636, page.getTotalElements());
    }
    
    @Test
    void getBeerById() {
        BeerDTO beer = beerClient.listBeers().getContent().getFirst();
        BeerDTO beerByIdDTO = beerClient.getBeerById(beer.getId());

        assertEquals(beer.getId(), beerByIdDTO.getId());
    }

    @Test
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
    void testUpdateBeer() {
        BeerDTO beerToUpdate = beerClient.listBeers().getContent().getFirst();
        beerToUpdate.setBeerName("updated beer name");
        
        BeerDTO updatedBeerDTO = beerClient.updateBeer(beerToUpdate);

        assertEquals(beerToUpdate.getBeerName(), updatedBeerDTO.getBeerName());
    }

    @Test
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
