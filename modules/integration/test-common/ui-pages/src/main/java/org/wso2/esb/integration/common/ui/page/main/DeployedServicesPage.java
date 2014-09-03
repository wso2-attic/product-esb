package org.wso2.esb.integration.common.ui.page.main;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.List;

public class DeployedServicesPage {
    private final WebDriver driver;

    public DeployedServicesPage(WebDriver driver) throws IOException {
        this.driver = driver;
        if (!"Deployed Services".equals(driver.findElement(By.id("middle")).findElement(By.tagName("h2")).getText())) {
            throw new IllegalStateException("This is not the Deployed Service Page");
        }
    }

    public ProxySourcePage gotoSourceView(String serviceName) throws IOException {
        List<WebElement> services = driver.findElements(By.cssSelector("#sgTable tr td:nth-child(2) a"));
        for (int i = 0; i < services.size(); i++) {
            WebElement serviceLink = services.get(i);
            if (serviceName.equals(serviceLink.getText())) {
                By sourceTdSelector = By.cssSelector("#sgTable tr:nth-child(" + (i + 1) + ") td:nth-child(10)");
                WebElement showSource = driver.findElement(sourceTdSelector).findElement(By.tagName("a"));
                showSource.click();
                return new ProxySourcePage(driver);
            }
        }
        throw new IllegalStateException("service named '" + serviceName + "' is not visible in the UI");
    }
}