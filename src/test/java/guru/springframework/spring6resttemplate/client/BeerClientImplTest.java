package guru.springframework.spring6resttemplate.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// TODO: Currently this test only works when the project spring-6-rest-mvc is running listening on port 80. 
// TODO: Therefore the test will fail in github actions. We should mock the rest template
@SpringBootTest
class BeerClientImplTest {
    
    @Autowired
    private BeerClientImpl beerClient;

    @Test
    void listBeers() {
        beerClient.listBeers();
    }
}
