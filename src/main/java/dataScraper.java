import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class dataScraper{
    record Tpa(String year, double attempts){}
    public static void main(String[] args) throws IOException {
        //getting the name of the player
        String name = args[0] + " " + args[1];
        //creating a new web scraper
        WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
        //turning off/ ignoring css error and some javaScripts
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);

        //connecting to the page we are scraping
        HtmlPage searchPage = client.getPage("https://www.nba.com/stats");

        //finding the search bar wehre we search for the player
        HtmlInput input = (HtmlInput) searchPage.getByXPath("//input").get(2);

        //typing the name into the search bar
        input.type(name);

        //waiting for the JavaScript to find our player
        client.waitForBackgroundJavaScriptStartingBefore(200);
        client.waitForBackgroundJavaScript(2000);

        //getting the first result and clicking on it
        HtmlElement link = (HtmlElement) searchPage.getByXPath("//a").get(321);
        HtmlPage resultsPage = link.click();

        //finding the selector for different options for presenting data of the player
        HtmlSelect select = (HtmlSelect) resultsPage.getByXPath("//select").get(1);

        //choosing per 40 minutes of playing time
        HtmlOption option = select.getOptionByValue("Per40");
        select.setSelectedAttribute(option, true);

        //waiting for JavaScript to get new data for our filter
        client.waitForBackgroundJavaScriptStartingBefore(200);
        client.waitForBackgroundJavaScript(2000);

        //calling the function where we find and get data from table
        List<Tpa> tpas = parseResults(resultsPage);

        //writing out our data
        for (Tpa tpa : tpas) {
            System.out.println(tpa.year + " " + tpa.attempts);
        }
    }

    private static List<Tpa> parseResults(HtmlPage resultsPage) {
        //finding table with the data we want
        HtmlTable table = (HtmlTable) resultsPage.getByXPath("//table").get(0);
        //making a list of all the data that we want from the table
        List<Tpa> tpas = table.getBodies().get(0).getRows().stream()
                .map(r -> {
                    String attempts = r.getCell(9).getTextContent();
                    return new Tpa(
                            r.getCell(0).getTextContent(),
                            Double.parseDouble(attempts)
                    );
                }).collect(Collectors.toList());
        return tpas;
    }
}