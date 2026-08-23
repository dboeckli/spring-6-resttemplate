# AGENTS.md

Spring Boot 4 (parent 4.1.0) / Spring Framework 6 REST client on **Java 25** (enforced by the
maven-enforcer plugin). Single Maven module, package `guru.springframework.spring6resttemplate`. It is
an OAuth2 client + resource server that calls the `spring-6-rest-mvc` Beer REST API via
`RestTemplate` (with `OAuthClientInterceptor` for client-credentials tokens) and renders a small
Thymeleaf web UI.

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template/dry-run, OpenAPI generation. `./mvnw verify` also runs the unit tests.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=BeerClientImplMockedTest`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/spring-6-resttemplate-chart-*.tgz`. Skip the Docker build with
  `-Dskip.docker.build=true`, the publish with `-Dskip.docker.publish=true`.
- `-Dskip.start.stop.springboot=true` skips the in-build app boot (spring-boot:start/stop) and the
  OpenAPI generation that depends on it.
- Start locally: `./mvnw spring-boot:run` (app on `:8086`; `application-docker.yaml` profile `docker`
  auto-starts `compose.yaml` via spring-boot-docker-compose).

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Sandbox build quirk (background)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's
`npm install` (prettier) would fail with `EPERM` unless npm skips bin links. The sandbox kit sets
`npm_config_bin_links=false` globally (`spec.yaml` → `environment.variables`), so no manual export
is needed here. On a normal host (Windows/CI) this does not apply either.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md`, json, `src/main/resources/application*.yaml`, `**/*.sh`):
  Spotless → fix with `./mvnw spotless:apply`.
- Spotless flexmark also formats markdown, so `.md` edits must stay flexmark-clean; run
  `./mvnw spotless:apply` after editing markdown (AGENTS.md / CLAUDE.md are excluded).

## External dependency gotcha

- The DTOs (`BeerDTO`, `BeerDTOPageImpl`, `BeerStyle`, ...) and the OpenAPI spec come from the
  external modules `ch.dboeckli.springframeworkguru.spring-rest-mvc:spring-6-rest-mvc` /
  `spring-6-rest-mvc-api`, resolved from GitHub Packages (`maven.pkg.github.com`). Without a PAT in
  `~/.m2/settings.xml` (server id `github`) the build cannot resolve dependencies.

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe). A `*Test` class will
  not run during `verify`'s failsafe phase and vice versa.
- `BeerClientImplWithTestContainerIT` (`@Tag("testcontainer")`) boots MySQL, Kafka, auth-server and
  rest-mvc as Testcontainers and needs **Docker**; image tags must equal the `helm.chart.version`
  of the respective app (lowercase `-snapshot`).
- `BeerClientImplWithDockerComposeIT` (`@Tag("docker-compose")`, `@ActiveProfiles("testdocker")`)
  runs against the services from `compose.yaml`.
- A custom `TestClassOrderer` sorts test classes (unit → IT → docker-compose IT last) and
  `LocaleExtension` is auto-registered to force `Locale.US`. Do not add a global locale again.
- Controller/UI ITs assert rendered HTML; use Awaitility for async readiness.

## Architecture

- Flow: `web/BeerWebController` (Thymeleaf) → `client/BeerClientImpl` (RestTemplate) →
  `spring-6-rest-mvc` REST API.
- `OAuthClientInterceptor` adds a client-credentials bearer token; `ConfigurationValues` binds
  external URLs; `AuthServerHealthIndicator` / `RestMvcHealthIndicator` expose readiness of the
  upstream services. `RequestLoggingConfig` logs the HTTP traffic (`CommonsRequestLoggingFilter`).
- Jackson 3 (`tools.jackson`) is in use; the mocked and docker-compose tests register a custom
  `ObjectMapper` bean (`@Primary`).

## Running locally

- Default profile: app on `:8086`, expects auth-server on `:9000` and rest-mvc on `:8081`
  (`security.auth-server-health-url`, `rest.template.base.url`). Profile `docker` uses `compose.yaml`
  (MySQL + Kafka + auth-server + rest-mvc + gateway) via spring-boot-docker-compose.
- Manual API testing: IntelliJ HTTP files / browser on `http://localhost:8086/beers`.

## Deploy / CI

- Deployment is Helm-only: chart in `helm-charts/`, packaged to `target/helm/repo/`, release name =
  artifactId, namespace `spring-6-resttemplate`. Dependencies (auth-server, rest-mvc ± mysql/kafka)
  are remote OCI subcharts (Repsy / Cloudsmith). `.run/` scripts `deploy-k8s` / `test-k8s` /
  `uninstall-k8s` wrap the Helm flow.
- CI (`.github/workflows/`): `maven-build.yml` builds + deploys snapshots and triggers
  `deploy-and-test-cluster.yml`; `release.yml` runs `mvn release:prepare release:perform` on
  main/master only (version must be `-SNAPSHOT`); SonarCloud analysis runs in the `analyze` job.
  Helm jobs log into Cloudsmith (`helm registry login docker.cloudsmith.io`) to pull the rest-mvc
  subcharts.
- Dependency updates are managed via `.github/dependabot.yml` and `.github/renovate.json`; validate
  changes with `renovate-config-validator`.
