package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Slf4j
//@ActiveProfiles("testdocker")
class BeerClientImplWithTestContainerIT {

    final static int REST_MVC_PORT = TestSocketUtils.findAvailableTcpPort();
    final static int AUTH_SERVER_PORT = TestSocketUtils.findAvailableTcpPort();

    static final Network network = Network.newNetwork();
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9")
        .withNetworkAliases("mysql")
        .withNetwork(network)
        .withEnv("MYSQL_DATABASE", "restdb")
        .withEnv("MYSQL_USER", "restadmin")
        .withEnv("MYSQL_PASSWORD", "password")
        .withEnv("MYSQL_ROOT_PASSWORD", "password")

        .withDatabaseName("restdb")
        .withUsername("restadmin")
        .withPassword("password");
    
    @Container
    static GenericContainer<?> authServer = new GenericContainer<>("domboeckli/spring-6-auth-server:0.0.1-SNAPSHOT")
        .withNetworkAliases("auth-server")
        .withNetwork(network)
        .withEnv("SERVER_PORT", String.valueOf(AUTH_SERVER_PORT))
        .withEnv("SPRING_SECURITY_OAUTH2_AUTHORIZATION_SERVER_ISSUER", "http://auth-server:" + AUTH_SERVER_PORT)
        .withExposedPorts(AUTH_SERVER_PORT)
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("auth-server")));

    @Container
    static GenericContainer<?> restMvc = new GenericContainer<>("domboeckli/spring-6-rest-mvc:0.0.1-SNAPSHOT")
        .withExposedPorts(REST_MVC_PORT)
        .withNetwork(network)
        .withEnv("SPRING_PROFILES_ACTIVE", "localmysql")
        .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI", "http://auth-server:" + AUTH_SERVER_PORT)
        .withEnv("SERVER_PORT", String.valueOf(REST_MVC_PORT))
        .withEnv("SPRING_DATASOURCE_URL", "jdbc:mysql://mysql:3306/restdb")
        .withEnv("LOGGING_LEVEL_ORG_APACHE_KAFKA_CLIENTS_NETWORKCLIENT", "ERROR")
        .dependsOn(mysql, authServer)
        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("rest-mvc")));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String mvcServerUrl = "http://" + restMvc.getHost() + ":" + restMvc.getFirstMappedPort();
        log.info("### Rest MVC Server URL: " + mvcServerUrl);
        registry.add("rest.template.base.url", () -> mvcServerUrl);

        String authServerAuthorizationUrl = "http://" + authServer.getHost() + ":" + authServer.getFirstMappedPort() + "/auth2/authorize";
        log.info("### AuthServer Authorization Url: " + authServerAuthorizationUrl);
        registry.add("spring.security.oauth2.client.provider.springauth.authorization-uri", () -> authServerAuthorizationUrl);

        String authServerTokenUrl = "http://" + authServer.getHost() + ":" + authServer.getFirstMappedPort() + "/oauth2/token";
        log.info("### Auth Server Token Url: " + authServerTokenUrl);
        registry.add("spring.security.oauth2.client.provider.springauth.token-uri", () -> authServerTokenUrl);

        String issuerUrl = "http://auth-server:" + AUTH_SERVER_PORT;
        log.info("### Issuer Url: " + issuerUrl);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUrl);
    }

    @Autowired
    private BeerClientImpl beerClient;

    @BeforeAll
    static void setUp() {
        log.info("#### mysql jdbc url: {}", mysql.getJdbcUrl());
        log.info("#### mysql Container name: {}", mysql.getContainerName());
        log.info("#### restMvc Container name: {}", restMvc.getContainerName());
        log.info("#### authServer Container name: {}", authServer.getContainerName());

        authServer.start();
        restMvc.start();

        waitForServerReadiness(authServer, AUTH_SERVER_PORT, "auth server");
        waitForServerReadiness(restMvc, REST_MVC_PORT, "REST MVC server");

        log.info("All servers are ready.");
    }

    @AfterAll
    static void printLogs() {
        System.out.println("----------- Auth Server Logs ------------------------");
        System.out.println(authServer.getLogs());

        System.out.println("----------- REST MVC Server Logs ------------------------");
        System.out.println(restMvc.getLogs());
    }

    private static void waitForServerReadiness(GenericContainer<?> container, int port, String serverName) {
        log.info("Waiting for {} to be ready on port {}", serverName, container.getMappedPort(port));

        Awaitility.await()
            .atMost(60, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<String> response = restTemplate.getForEntity(
                        String.format("http://%s:%d/actuator/health/readiness", container.getHost(), container.getMappedPort(port)),
                        String.class
                    );
                    log.info("{} readiness check response: {}", serverName, response.getBody());
                    return response.getStatusCode().is2xxSuccessful() && response.getBody().contains("\"status\":\"UP\"");
                } catch (Exception e) {
                    log.warn("{} not ready yet: {}", serverName, e.getMessage());
                    return false;
                }
            });
        log.info("{} is ready", serverName);
    }

    @Test
    void listBeers() {
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

}
