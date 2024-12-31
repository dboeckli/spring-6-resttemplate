package guru.springframework.spring6resttemplate.web;

import guru.springframework.spring6resttemplate.client.BeerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class BeerWebController {

    private final BeerClient beerclient;

    @RequestMapping("/beers")
    public String getAuthors(Model model) {
        model.addAttribute("beers", beerclient.listBeers());
        return "beers";
    }
}
