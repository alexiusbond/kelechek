import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

public class AuthenticatedScreenTest {
    public static void main(String[] args) {
/*
        ChromeOptions opt = new ChromeOptions();
        opt.setAcceptInsecureCerts(true);
        System.setProperty("webdriver.chrome.driver", "/home/chromedriver_win32/chromedriver.exe");
        ChromeDriver driver = new ChromeDriver(opt);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.get("https://localhost:8443/spt_war_exploded/");
        driver.manage().window().maximize();
        WebElement element = driver.findElement(By.xpath("//input[@id='username']"));
        element.sendKeys("100001");
        element = driver.findElement(By.xpath("//input[@id='password']"));
        element.sendKeys("1");
        element.submit();
        // Select Kassa
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[1]/div/div[2]/div/div" +
                                "/div/div/div[1]/div/div[6]/div/span[4]")));
        element.click();
        // Set start date
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[1]/div/div[2]/div/div/div/" +
                                "div/div[3]/div/div[2]/div/div/div[1]/div/input")));
        element.clear();
        element.sendKeys("21-05-2021");
        //Click search
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[1]/div/div[2]/div/div/div/div" +
                                "/div[3]/div/div[2]/div/div/div[5]/div")));
        element.click();
        */
    }
}
