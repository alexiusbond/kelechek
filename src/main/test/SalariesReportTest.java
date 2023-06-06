import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

public class SalariesReportTest {
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
        // Select Menu
        Actions action = new Actions(driver);
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[1]/div/div[2]/div/div/div/div/div[1]/div/div[6]/div/span[10]")));
        action.moveToElement(element).build().perform();
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[2]/div[2]/div/div/span[9]")));
        element.click();

        //Select school
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[1]/div/div[2]/div/div/div/div/div[1]/div/div[2]/div/div/div[3]/div/div[2]/div/div")));
        element.click();
        //Click searched school
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[2]/div[2]/div/div[2]/table/tbody/tr[17]")));
        element.click();
        // Select report
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[1]/div/div[2]/div/div/div/div/div[3]/div/div/div[1]/div/div[2]/div[2]/input")));
        element.clear();
        element.sendKeys("Отчет по выплатам");
        //Click searched report
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[2]/div[2]/div/div[2]/table/tbody/tr/td")));
        element.click();
        //Select category
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[1]/div/div[2]/div/div/div/div/div[3]/div/div/div[1]/div/div[3]/div/div[5]/div/div[3]/div[1]/table/tbody/tr[9]")));
        element.click();
//Click generate button
        element = (new WebDriverWait(driver, 10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("/html/body/div[1]/div/div[2]/div/div/div/div/div[3]/div/div/div[1]/div/div[3]/div/div[6]")));
        element.click();*/
    }
}
