package guru.springframework.spring6resttemplate.web.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testdocker")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BeerListPageIT {

    @LocalServerPort
    private int port;

    private WebDriver webDriver;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // Run in headless mode
        webDriver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }

    @Test
    void testBeerListPageLoads() {
        webDriver.get("http://localhost:" + port + "/beers");
        waitForPageLoad();
        Assertions.assertEquals("Beer List", webDriver.getTitle());
    }

    @Test
     void testBeerListContainsItems() {
        webDriver.get("http://localhost:" + port + "/beers");
        waitForPageLoad();
        List<WebElement> tableRows = webDriver.findElements(By.cssSelector("table tbody tr"));
        
        assertFalse(tableRows.isEmpty(), "Beer list should contain items");
        assertEquals(25, tableRows.size());
    }

    @Test
    void testPaginationExists() {
        webDriver.get("http://localhost:" + port + "/beers");
        waitForPageLoad();
        WebElement pagination = webDriver.findElement(By.cssSelector("nav[aria-label='Page navigation']"));

        assertNotNull(pagination, "Pagination should exist");

        // Check if 'Previous' and 'First' are disabled
        WebElement previousButton = webDriver.findElement(By.id("previousButton"));
        WebElement firstButton = webDriver.findElement(By.id("firstButton"));
        assertTrue(Objects.requireNonNull(previousButton.findElement(By.xpath("./..")).getAttribute("class")).contains("disabled"), "Previous button should be disabled");
        assertTrue(firstButton.findElement(By.xpath("./..")).getAttribute("class").contains("disabled"), "First button should be disabled");

        // Check if page 1 is selected
        WebElement page1Button = webDriver.findElement(By.id("pageButton1"));
        assertTrue(Objects.requireNonNull(page1Button.findElement(By.xpath("./..")).getAttribute("class")).contains("active"), "Page 1 should be selected");

        // Check number of page number buttons
        List<WebElement> pageNumberButtons = webDriver.findElements(By.cssSelector("[id^='pageButton']"));
        assertEquals(5, pageNumberButtons.size());

        // Check the last button
        WebElement lastButton = webDriver.findElement(By.id("lastButton"));
        assertEquals("97", lastButton.getText(), "The last button should display 97");

        // Check if 'Next' is clickable
        WebElement nextButton = webDriver.findElement(By.id("nextButton"));
        assertFalse(Objects.requireNonNull(nextButton.findElement(By.xpath("./..")).getAttribute("class")).contains("disabled"), "Next button should be clickable");
        assertTrue(nextButton.isEnabled(), "Next button should be enabled");

        // Check the last visible page number
        WebElement lastVisiblePageButton = pageNumberButtons.getLast();
        int lastVisiblePageNumber = Integer.parseInt(lastVisiblePageButton.getText());
        assertTrue(lastVisiblePageNumber > 1 && lastVisiblePageNumber <= 97, "Last visible page number should be between 2 and 97");
    }

    @Test
    void testViewButtonWorks() {
        webDriver.get("http://localhost:" + port + "/beers");
        waitForPageLoad();
        WebElement firstViewButton = webDriver.findElement(By.cssSelector("table tbody tr:first-child .btn-primary"));
        String href = firstViewButton.getAttribute("href");
        
        assertNotNull(href, "View button should have a href attribute");
        assertTrue(href.contains("/beer/"), "View button should link to a specific beer");

        // Extract the beer ID from the href
        String expectedBeerId = href.substring(href.lastIndexOf("/") + 1);

        // Click the "View" button
        firstViewButton.click();

        // Wait for the new page to load and for the beerId element to be present
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        WebElement beerIdElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("beerId")));

        assertAll("Beer Details Page Assertions",
            () -> assertEquals("http://localhost:" + port + "/beer/" + expectedBeerId, webDriver.getCurrentUrl(),
                "Should navigate to the specific beer page"),
            () -> assertEquals("Beer Details", webDriver.getTitle(),
                "Page title should be 'Beer Details'"),
            () -> assertEquals(expectedBeerId, beerIdElement.getText(),
                "Displayed beer ID should match the expected ID")
        );
   }

    private void waitForPageLoad() {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
        wait.until((ExpectedCondition<Boolean>) wd ->
            Objects.equals(((JavascriptExecutor) wd).executeScript("return document.readyState"), "complete"));
    }
}
