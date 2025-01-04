package guru.springframework.spring6resttemplate.web;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("testdocker")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeerWebControllerIT {

    @Autowired
    BeerWebController controller;

    @Test
    @Order(0)
    void testListBeers() {
        Model model = new ExtendedModelMap();
        
        String viewName = controller.getBeers(null, null, model);
        
        assertEquals("beers", viewName);

        List<BeerDTO> beerList = (List<BeerDTO>)model.getAttribute("beers");
        assertEquals(25, beerList.size());
        assertEquals(97, model.getAttribute("totalPages"));
        assertEquals(0, model.getAttribute("currentPage"));
        assertEquals(2413L, model.getAttribute("totalElements"));
        assertEquals(0, model.getAttribute("startPage"));
        assertEquals(4, model.getAttribute("endPage"));
    }

    @Test
    @Order(0)
    void testListBeersPaged() {
        Model model = new ExtendedModelMap();

        String viewName = controller.getBeers(5, 25, model);

        assertEquals("beers", viewName);

        List<BeerDTO> beerList = (List<BeerDTO>)model.getAttribute("beers");
        assertEquals(25, beerList.size());
        assertEquals(97, model.getAttribute("totalPages"));
        assertEquals(4, model.getAttribute("currentPage"));
        assertEquals(2413L, model.getAttribute("totalElements"));
        assertEquals(3, model.getAttribute("startPage"));
        assertEquals(7, model.getAttribute("endPage"));
    }

    @Test
    @Order(10)
    void testGetBeerById() {
        // Given
        Model model = new ExtendedModelMap();

        controller.getBeers(null, null, model);

        List<BeerDTO> beerList = (List<BeerDTO>)model.getAttribute("beers");
        BeerDTO beerDTO = beerList.getFirst();

        String viewName = controller.getBeerById(beerDTO.getId().toString(), model);

        // Then
        assertEquals("beer", viewName);
        BeerDTO beer = (BeerDTO) model.getAttribute("beer");
        assertEquals(beerDTO.getId(), beer.getId());
    }
}
