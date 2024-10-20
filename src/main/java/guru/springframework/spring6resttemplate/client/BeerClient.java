package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import org.springframework.data.domain.Page;

public interface BeerClient {
    
    Page<BeerDTO> listBeers();
}
