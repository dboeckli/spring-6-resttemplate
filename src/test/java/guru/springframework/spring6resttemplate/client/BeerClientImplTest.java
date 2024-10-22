package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.dto.BeerStyle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO: Currently this test only works when the project spring-6-rest-mvc is running listening on port 80. 
// TODO: Therefore the test will fail in github actions. We should mock the rest template
@SpringBootTest
@Slf4j
class BeerClientImplTest {
    
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
    }
    
    @Test
    void getBeerById() {
        BeerDTO beer = beerClient.listBeers().getContent().getFirst();
        BeerDTO beerByIdDTO = beerClient.getBeerById(beer.getId());

        assertEquals(beer.getId(), beerByIdDTO.getId());
    }

    @Test
    void createBeer() {
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
}
