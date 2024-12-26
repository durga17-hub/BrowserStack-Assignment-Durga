package test;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowserStackTest {

	private WebDriver driver;
	public static final String USERNAME = "durgabidasir_fopnHh";
	public static final String AUTOMATE_KEY = "mcPyKbz7RYCsy4kXsZ2W";
	public static final String URL = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub";

	@Parameters({ "os", "osVersion", "browser", "browserVersion", "device", "realMobile" })
	@Test
	public void runTest(String os, String osVersion, String browser, String browserVersion, String device,
			boolean realMobile) throws Exception {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		if (realMobile) {
			// Mobile capabilities
			capabilities.setCapability("bstack:options",
					Map.of("deviceName", device, "realMobile", true, "osVersion", osVersion));
		} else {
			// Desktop capabilities
			capabilities.setCapability("bstack:options", Map.of("os", os, "osVersion", osVersion));
			capabilities.setCapability("browserName", browser);
			capabilities.setCapability("browserVersion", browserVersion);
		}

		driver = new RemoteWebDriver(new URL(URL), capabilities);

		PerformNavigation(driver);
	}

	public void PerformNavigation(WebDriver driver) {

		/*
		 * 1. Visit the website El PaÌs, a Spanish news outlet. Ensure that the
		 * website's text is displayed in Spanish.
		 */

		try {
			driver.get("https://elpais.com/");

			// used explicit wait to show the accept cookie button.
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("didomi-notice-agree-button")));
			driver.findElement(By.id("didomi-notice-agree-button")).click();

			// used assertion here verifying whether the page loaded in spanish or not.
			String langAttribute = driver.findElement(By.tagName("html")).getDomAttribute("lang");
			String spanishtext = driver.getTitle();
			System.out.println(spanishtext);
			Assert.assertEquals(langAttribute, "es-ES", "The website is not in Spanish.");
			System.out.println(langAttribute);

			// get text from the page.
			WebElement RandomText = driver.findElement(By.xpath("//span[text()='Compruebe su n˙mero']"));
			String Text = RandomText.getText();

			// Check the text that contains Spanish-specific regular expression words or
			// patterns
			Assert.assertTrue(Text.matches(".*[a-zA-ZÒ·ÈÌÛ˙¸—¡…Õ”⁄‹]"), "The text does not appear to be in Spanish.");

			System.out.println("The website is displayed in Spanish.");

			// 2. Scrape Articles from the Opinion Section:

			// Navigate to the Opinion section of the website
			WebElement opinionSection = driver.findElement(By.xpath("//a[@href='https://elpais.com/opinion/']"));
			opinionSection.click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cs_t_l")));

			

			List<WebElement> articles1 = driver.findElements(By.tagName("article"));

			ArrayList<String> articleTitles = new ArrayList<String>();

			int count = 0;
			// Fetch the first five articles in this section.
			for (WebElement child : articles1) {

				if (count >= 5) {
					break;
				}

				// Printing the title and content of each article in Spanish.
				System.out.println("Title of the Article: " + child.findElement(By.tagName("h2")).getText());

				String title = child.findElement(By.tagName("h2")).getText();
				articleTitles.add(title);

				System.out.println("Content of the Article: " + child.findElement(By.tagName("p")).getText());
				
				//capturing the screenshot if the cover image of the article is available
				try {
                    WebElement articleImageElement = child.findElement(By.cssSelector(".c_m_e"));
                    if (articleImageElement != null) {
                        String imageUrl = articleImageElement.getDomAttribute("src");
                        File destinationFile = new File("cover_image_" + count + ".jpg");
                        FileUtils.copyURLToFile(new URL(imageUrl), destinationFile);
                        System.out.println("Image saved as: " + destinationFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.out.println("No cover image found for article" + count);
                }
				
				count++;

			}
			String[] titlesToArray = articleTitles.toArray(new String[0]);

			// calling translateApi() method
			translateApi(titlesToArray);

		} catch (Exception e) {
			System.out.println("Test failed: " + e.getMessage());
		}

	}

	// 3. This method is to translate Article Headers 
	public void translateApi(String[] titlesOfpage) throws IOException {

		MediaType mediaType = MediaType.get("application/json; charset=utf-8");

		// Input parameters
		String from = "es";
		String to = "en";
		String e = "";


		// Creating the JSON object dynamically
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("from", from);
		jsonObject.put("to", to);
		jsonObject.put("e", e);

		// Converting array to JSONArray and add it to the JSON object
		JSONArray jsonArray = new JSONArray(titlesOfpage);
		jsonObject.put("q", jsonArray);

		// Converting JSON object to string
		String requestBodyString = jsonObject.toString();

		// Creating RequestBody
		RequestBody body = RequestBody.create(mediaType, requestBodyString);

		// Printing dynamically created RequestBody
		System.out.println("Request Body: " + requestBodyString);

		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder().url("https://rapid-translate-multi-traduction.p.rapidapi.com/t")
				.post(body).addHeader("x-rapidapi-key", "2f22fdfa56mshe6125ee3465fa19p1ec7cejsnbcc4310c5aeb")
				.addHeader("x-rapidapi-host", "rapid-translate-multi-traduction.p.rapidapi.com")
				.addHeader("Content-Type", "application/json").build();

		Response response = client.newCall(request).execute();

		String responseBody = response.body().string();
		System.out.println("Translated Headers: " + responseBody);

		// 4. Analyze Translated Headers:
		// Map to store word counts
		Map<String, Integer> wordCounts = new HashMap<String, Integer>();

		String[] words1 = responseBody.split(" ");

		// Process each header
		for (String header : words1) {
			// Split the header into words and normalize to lowercase
			String[] words = header.toLowerCase().split("\\W+");

			for (String word : words) {
				// Update word count
				wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
			}
		}

		// Print words repeated more than twice
		System.out.println("Words repeated more than twice:");
		for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
			if (entry.getValue() > 2) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}
		}

	}

	@AfterMethod
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

}
