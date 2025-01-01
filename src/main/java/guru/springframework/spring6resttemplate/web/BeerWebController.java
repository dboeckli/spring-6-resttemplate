package guru.springframework.spring6resttemplate.web;

import guru.springframework.spring6resttemplate.client.BeerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class BeerWebController {

    private final BeerClient beerclient;

    @RequestMapping("/beers")
    public String getBeers(Model model) {
        model.addAttribute("beers", beerclient.listBeers());
        return "beers";
    }

    @RequestMapping("/beer/{id}")
    public String getBeerById(@PathVariable("id") String beerId, Model model) {
        model.addAttribute("beer", beerclient.getBeerById(UUID.fromString(beerId)));
        return "beer";
    }
}
