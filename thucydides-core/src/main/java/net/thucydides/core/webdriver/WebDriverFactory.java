package net.thucydides.core.webdriver;

import net.thucydides.core.ThucydidesSystemProperty;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

import java.io.File;

/**
 * Provides an instance of a supported WebDriver.
 * When you instanciate a Webdriver instance for Firefox or Chrome, it opens a new browser.
 * We
 * 
 * @author johnsmart
 *
 */
public class WebDriverFactory {

    private final WebdriverInstanceFactory webdriverInstanceFactory;

    public WebDriverFactory() {
        this.webdriverInstanceFactory = new WebdriverInstanceFactory();
    }

    public WebDriverFactory(WebdriverInstanceFactory webdriverInstanceFactory) {
        this.webdriverInstanceFactory = webdriverInstanceFactory;
    }

    /***
     * Create a new WebDriver instance of a given type.
     */
    public WebDriver newInstanceOf(final SupportedWebDriver driverType)  {
        if (driverType == null) {
            throw new IllegalArgumentException("Driver type cannot be null");
        }

        return newWebdriverInstance(driverType.getWebdriverClass());
    }

    public static Class<? extends WebDriver> getClassFor(final SupportedWebDriver driverType)  {
        return driverType.getWebdriverClass();
    }

    protected WebDriver newWebdriverInstance(final Class<? extends WebDriver> driverClass) {
        try {
            WebDriver driver;
            if (isAFirefoxDriver(driverClass)) {
               driver = webdriverInstanceFactory.newInstanceOf(driverClass, buildFirefoxProfile());
            } else {
                driver =  webdriverInstanceFactory.newInstanceOf(driverClass);
            }
            redimensionBrowser(driver);
            return driver;
        } catch (Exception cause) {
            throw new UnsupportedDriverException("Could not instantiate " + driverClass, cause);
        }
    }

    private void redimensionBrowser(final WebDriver driver) {
        int height = ThucydidesSystemProperty.getIntegerValue(ThucydidesSystemProperty.SNAPSHOT_HEIGHT, 0);
        int width = ThucydidesSystemProperty.getIntegerValue(ThucydidesSystemProperty.SNAPSHOT_WIDTH, 0);

        if ((height > 0) && (width > 0)) {
            String resizeWindow = "window.resizeTo(" + width + "," + height + ")";
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(resizeWindow);
        }
    }

    private boolean isAFirefoxDriver(Class<? extends WebDriver> driverClass) {
        return (FirefoxDriver.class.isAssignableFrom(driverClass));
    }

    protected FirefoxProfile createNewFirefoxProfile() {
        return new FirefoxProfile();
    }

    protected FirefoxProfile useExistingFirefoxProfile(final File profileDirectory) {
        return new FirefoxProfile(profileDirectory);
    }

    private FirefoxProfile buildFirefoxProfile() {

        String profileDirectory = System.getProperty("webdriver.firefox.profile");
        FirefoxProfile profile;
        if (profileDirectory == null) {
            profile = createNewFirefoxProfile();
        } else {
            profile = useExistingFirefoxProfile(new File(profileDirectory));
        }
        if (dontAssumeUntrustedCertificateIssuer()) {
            profile.setAssumeUntrustedCertificateIssuer(false);
        }
        return profile;
    }

    private boolean dontAssumeUntrustedCertificateIssuer() {
        return !(ThucydidesSystemProperty.getBooleanValue(ThucydidesSystemProperty.ASSUME_UNTRUSTED_CERTIFICATE_ISSUER,
                                                          true));
    }

    /**
     * Initialize a page object's fields using the specified WebDriver instance.
     */
    public static void initElementsWithAjaxSupport(final Object pageObject, final WebDriver driver) {
        ElementLocatorFactory finder = new DisplayedElementLocatorFactory(driver, Configuration.getElementTimeout());
        PageFactory.initElements(finder, pageObject);
    }

    public static void initElementsWithAjaxSupport(final Object pageObject, final WebDriver driver, int timeout) {
        ElementLocatorFactory finder = new DisplayedElementLocatorFactory(driver, timeout);
        PageFactory.initElements(finder, pageObject);
    }

}
