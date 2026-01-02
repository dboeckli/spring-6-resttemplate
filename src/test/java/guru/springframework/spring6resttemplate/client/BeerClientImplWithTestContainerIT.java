package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.dto.BeerDTO;
import guru.springframework.spring6resttemplate.dto.BeerDTOPageImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("testcontainer")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class BeerClientImplWithTestContainerIT {

    private static final String MYSQL_VERSION = "8.4.7";
    private static final String AUTH_SERVER_VERSION = "0.0.5-SNAPSHOT";
    private static final String REST_MVC_VERSION = "0.0.4-SNAPSHOT";
    private static final String KAFKA_VERSION = "4.1.1";

    private static final String IMAGE_REPOSITORY = "domboeckli";

    static final int REST_MVC_PORT = TestSocketUtils.findAvailableTcpPort();
    static final int AUTH_SERVER_PORT = TestSocketUtils.findAvailableTcpPort();

    static final Network sharedNetwork = Network.newNetwork();

    @Container
    static GenericContainer<?> kafkaContainer= new GenericContainer<>("apache/kafka:" + KAFKA_VERSION)
            .withNetworkAliases("kafka")
            .withNetwork(sharedNetwork)
            .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_NODE_ID", "1")
            .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@kafka:19093")
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:19093")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
            .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
            .withEnv("KAFKA_LOG_DIRS", "/var/lib/kafka/data")
            .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("kafka")))
            .waitingFor(Wait.forSuccessfulCommand("/opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092 > /dev/null 2>&1"));

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:" + MYSQL_VERSION)
            .withNetworkAliases("mysql")
            .withNetwork(sharedNetwork)
            .withEnv("MYSQL_DATABASE", "restdb")
            .withEnv("MYSQL_USER", "restadmin")
            .withEnv("MYSQL_PASSWORD", "password")
            .withEnv("MYSQL_ROOT_PASSWORD", "password")

            .withDatabaseName("restdb")
            .withUsername("restadmin")
            .withPassword("password")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("mysql")))
            .waitingFor(Wait.forSuccessfulCommand("mysqladmin ping -h localhost -uroot -ppassword"));

    @Container
    static GenericContainer<?> authServer = new GenericContainer<>(IMAGE_REPOSITORY + "/spring-6-auth-server:" + AUTH_SERVER_VERSION)
            .withNetworkAliases("auth-server")
            .withNetwork(sharedNetwork)
            .withEnv("SERVER_PORT", String.valueOf(AUTH_SERVER_PORT))
            .withEnv("SPRING_SECURITY_OAUTH2_AUTHORIZATION_SERVER_ISSUER", "http://auth-server:" + AUTH_SERVER_PORT)
            .withExposedPorts(AUTH_SERVER_PORT)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("auth-server")))
            .waitingFor(Wait.forHttp("/actuator/health/readiness")
                    .forStatusCode(200)
                    .forResponsePredicate(response ->
                            response.contains("\"status\":\"UP\"")
                    )
            );

    @Container
    static GenericContainer<?> restMvc = new GenericContainer<>(IMAGE_REPOSITORY + "/spring-6-rest-mvc:" + REST_MVC_VERSION)
            .withExposedPorts(REST_MVC_PORT)
            .withNetwork(sharedNetwork)
            .withEnv("SPRING_PROFILES_ACTIVE", "mysql")
            .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI", "http://auth-server:" + AUTH_SERVER_PORT)
            .withEnv("SERVER_PORT", String.valueOf(REST_MVC_PORT))
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:mysql://mysql:3306/restdb")
            .withEnv("LOGGING_LEVEL_ORG_APACHE_KAFKA_CLIENTS_NETWORKCLIENT", "ERROR")

            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("SPRING_KAFKA_CONSUMER_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("SPRING_KAFKA_ADMIN_PROPERTIES_BOOTSTRAP_SERVERS", "kafka:9092")

            .dependsOn(mysql, kafkaContainer, authServer)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("rest-mvc")))
            .waitingFor(Wait.forHttp("/actuator/health/readiness")
                    .forStatusCode(200)
                    .forResponsePredicate(response ->
                            response.contains("\"status\":\"UP\"")
                    )
            );

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

    @Test
    void testListBeers() {
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

        assertEquals(503, page.getTotalElements());
    }

    @Test
    void testListBeersWithBeerName() {
        BeerDTOPageImpl<BeerDTO> page = (BeerDTOPageImpl<BeerDTO>) beerClient.listBeers("IPA",
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

        assertEquals(60, page.getTotalElements());
    }

}
