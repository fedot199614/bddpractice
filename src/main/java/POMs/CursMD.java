package POMs;

import POMs.Pages.ExchangeRates.ExchangeRates;
import POMs.Pages.RatesEvolution;
import dto.ValCurs;
import helpers.Config;
import helpers.FileManager;
import helpers.SeleniumUtils;
import helpers.XMLParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CursMD extends AbstractPOM {

    private static final Logger log = LogManager.getLogger(CursMD.class);
    private static final String SITE_URL = Config.getString("homepage.url");


    @FindBy(xpath = "//a[@id='language-select']")
    public WebElement languageBtn;
    @FindBy(xpath = "//a[@id='language-select']/following::ul[1]/li/a")
    public List<WebElement> languagesSelect;
    @FindBy(xpath = "//button[@class='btn btn-dropdown dropdown-toggle']")
    public WebElement currencyViBtn;
    @FindBy(xpath = "//div[@class='col-sm-6 conversion-param']//div[@class='chosen-drop']/ul/li")
    public List<WebElement> currencyList;

    //other pages
    private List<Page> allPages;
    private ExchangeRates exchangeRatesPage;
    private RatesEvolution ratesEvolutionPage;


    public CursMD(WebDriver driver) {
        super(driver);
        exchangeRatesPage = new ExchangeRates(driver);
        ratesEvolutionPage = new RatesEvolution(driver);
    }

    public ExchangeRates getExchangeRatesPage() {
        return exchangeRatesPage;
    }

    public RatesEvolution getRatesEvolutionPage() {
        return ratesEvolutionPage;
    }

    public static CursMD init(WebDriver driver) {
        log.info("Redirecting to " + SITE_URL);
        driver.get(SITE_URL);
        SeleniumUtils.sleep(1);
        return new CursMD(driver);
    }

    public void changeLanguage(String language) {
        if (languageBtn.getText().trim().equals(language)) {
            log.warn("Language <" + language + "> already set!");
        } else {
            log.info("Setting language to <" + language + ">");
            languageBtn.click();

            SeleniumUtils.sleep(2);
            SeleniumUtils.clickJS(driver, languagesSelect.stream()
                    .filter(x -> x.getText().contains(language))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Language <" + language + "> not found!")));
            SeleniumUtils.sleep(3);
        }
    }

    public WebElement getMenuItem(String text) {
        log.info("Searching element with text <" + text + ">");
        return driver.findElement(By.xpath("//ul[contains(@class, 'navbar-nav')]//a[./span[text()='" + text + "']]"));
    }

    public void openCurrencyList() {
        log.info("Open currency list");
        SeleniumUtils.getWaiter(SeleniumUtils.getInstance().getDriver())
                .until(ExpectedConditions.elementToBeClickable(currencyViBtn)).click();

    }

    public void isCurrencyListCorrect(String lang) throws JAXBException, IOException {
        ValCurs valCurs = (ValCurs)  XMLParser.parseXML(
                FileManager.readFromFileBasedOnLang(lang),
                ValCurs.class);

        List<String> expectedResults = valCurs.getValute().stream()
                .collect(ArrayList::new, (list, item)->list.add(item.getCharCode()+" - "+item.getName()), ArrayList::addAll);

        //currencyList.forEach(v-> System.out.println(v.getText()+" ORIGINAL ELEMENT | "));

        log.info("Checking main page currency list language...");
        log.info("Actual -> "); SeleniumUtils.parseTextFromWebElementToCollection(currencyList,true).forEach(v-> System.out.print(v+" | "));
        log.info("\n");

        Collections.sort(expectedResults);
        log.info("Expected -> "); expectedResults.forEach(v-> System.out.print(v+" | "));
        log.info("\n");


        Assertions.assertThat(SeleniumUtils.parseTextFromWebElementToCollection(currencyList,true)).isEqualTo(expectedResults);
    }




}
