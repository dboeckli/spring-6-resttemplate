package guru.springframework.spring6resttemplate.test.util.docker;

import guru.springframework.spring6resttemplate.client.BeerClientImpl;
import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@UtilityClass
public class MvcServerTestUtil {

    public static void checkMvcDatabaseInitDone(BeerClientImpl beerClient) {
        // Wait for the database to be fully initialized
        log.info("Check Mvc Database init done");

        AtomicInteger attempts = new AtomicInteger(0);
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> {
                int attempt = attempts.incrementAndGet();
                try {
                    BeerDTOPageImpl<BeerDTO> page =
                        (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers(null, null, null, null, null);

                    long found = page.getTotalElements();
                    log.info("### Poll #{}: aktuell gefundene Biere: {}", attempt, found);

                    return found >= 503;
                } catch (Exception ex) {
                    log.error("### Poll #{}: listBeers noch nicht erfolgreich", attempt, ex);
                    return false;
                }
            });

        log.info("Database is fully initialized.");
    }
}
