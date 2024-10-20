package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// TODO: Currently this test only works when the project spring-6-rest-mvc is running listening on port 80. 
// TODO: Therefore the test will fail in github actions. We should mock the rest template
@SpringBootTest
@Slf4j
class BeerClientImplTest {
    
    @Autowired
    private BeerClientImpl beerClient;

    @Test
    void listBeers() {
        BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>)beerClient.listBeers();
        
        log.info("TotalElements: " + page.getTotalElements());
        log.info("NumberOfElements: " + page.getNumberOfElements());
        log.info("TotalPages: " + page.getTotalPages());
        log.info("Number: " + page.getNumber());
        log.info("Pageable: " + page.getPageable());
        log.info("First BeerDTO: " + page.getContent().getFirst().getBeerName());
    }
}
