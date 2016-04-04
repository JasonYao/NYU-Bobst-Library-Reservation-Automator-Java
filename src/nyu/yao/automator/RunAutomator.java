package nyu.yao.automator;

/* Selenium imports */
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;

/* Time imports */
import java.util.concurrent.TimeUnit;
import com.google.common.base.Function;

/* Exception imports */
import nyu.yao.automator.Automator.CompletedException;
import nyu.yao.automator.Automator.TimeSlotTakenException;
import nyu.yao.automator.Automator.UserNumberException;
import nyu.yao.automator.Automator.ReservationException;
import nyu.yao.automator.Automator.InvalidLoginException;
import org.openqa.selenium.support.ui.Select;

/**
 * Class for the run automation script
 */
public class RunAutomator
{
    /**
     * Private constructor to make the class uninstantiable
     */
    private RunAutomator(){}

    /**
     * The wrapper for the actual run automator
     * @param browser The current browser for this run
     * @param user The current user for this run
     * @param offset The current amount of offsets (occurs when a time is already selected, but user is good)
     * @param setup The current setup containing all settings, loggers, and user pools
     * @throws CompletedException
     * @throws TimeSlotTakenException
     * @throws InterruptedException
     * @throws UserNumberException
     * @throws ReservationException
     * @throws InvalidLoginException
     */
    public static void run(WebDriver browser, User user, int offset, Setup setup)
            throws CompletedException, TimeSlotTakenException, InterruptedException, UserNumberException,
            ReservationException, InvalidLoginException
    {
        // Defines the wait
        FluentWait<WebDriver> wait = new FluentWait<>(browser)
                .withTimeout(20, TimeUnit.SECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class);

        // Steps through each run component
        initiate(browser, wait);
        login(browser, user);
        setDate(browser, wait, setup.getSettings());
        TimeTuple timeTuple = setTime(browser, offset, setup.getUserPool(), setup.getSettings());
        selectRoom(browser, user, wait, setup.getSettings());
        updateLog(user, setup.getUserPool(), timeTuple);
    } // End of the run method

    /**
     * Initialises the browser to the start of the run
     * @param browser The current browser for this run
     * @param wait The fluent wait that is to be used when waiting for an element to appear
     */
    private static void initiate(WebDriver browser, FluentWait<WebDriver> wait) throws TimeoutException
    {
        browser.manage().window().maximize();
        try
        {
            browser.get("https://login.library.nyu.edu/users/auth/nyu_shibboleth?auth_type=nyu&institution=NYU");
            wait.until((Function<WebDriver, WebElement>) driver -> driver.findElement(By.xpath("//form[@id='login']")));
        }
        catch (TimeoutException e)
        {throw new TimeoutException("Error: Unable to start reach start page, internet may not be connected");}
    } // End of the initiate method

    /**
     * Logs a user into NYU's services for the run
     * @param browser The current browser for this run
     * @param user The current user for this run
     * @throws InvalidLoginException
     */
    private static void login(WebDriver browser, User user) throws InvalidLoginException
    {
        // Now we're at the login page
        WebElement username = browser.findElement(By.xpath("//form[@id='login']/input[1]"));
        WebElement password = browser.findElement(By.xpath("//form[@id='login']/input[2]"));

        // Signs into the bobst reserve with the user's username and password
        username.sendKeys(user.getUsername());
        password.sendKeys(user.getPassword());
        browser.findElement(By.xpath("//form[@id='login']/input[3]")).click();

        // Check for invalid login credentials
        if (browser.getCurrentUrl().equals("https://shibboleth.nyu.edu:443/idp/Authn/UserPassword") ||
                (browser.getCurrentUrl().equals("https://shibboleth.nyu.edu/idp/Authn/UserPassword")) ||
                (browser.getCurrentUrl().equals("https://shibboleth.nyu.edu/idp/profile/SAML2/Redirect/SSO?execution=e1s2")))
            throw new InvalidLoginException("Error: User " + user.getUsername() + " had invalid login credentials");
    } // End of the login method

    /**
     * Sets the date on the date picker for this run
     * @param browser The current browser for this run
     * @param wait The fluent wait that is to be used when waiting for an element to appear
     * @param settings a Settings object containing the program settings
     * @throws InterruptedException
     */
    private static void setDate(WebDriver browser, FluentWait<WebDriver> wait, Settings settings) throws InterruptedException
    {
        // The selenium equivalent to a GOTO statement - bad programming, but oh well
        browser.get("https://rooms.library.nyu.edu/");

        // Error checking that rooms.library.nyu.edu pops up
        for (int count = 0; !browser.getCurrentUrl().equals("https://rooms.library.nyu.edu/") && count < 5; ++count)
        {
            browser.navigate().refresh();
            Thread.sleep(5000);
        }

        browser.findElement(By.xpath("//form[@class='form-horizontal']/div[@class='well well-sm']" +
                "/div[@class='form-group has-feedback']/div[@class='col-sm-6']/input[1]")).click();

        // Checks the month and year, utilizes a wait for the year for the form to pop up
        WebElement datePickerYear = wait.until((Function<WebDriver, WebElement>) driver -> driver.findElement(By.xpath(
                "//div[@id='ui-datepicker-div']/div[@class='ui-datepicker-group ui-datepicker-group-first']/" +
                        "div[@class='ui-datepicker-header ui-widget-header ui-helper-clearfix ui-corner-left']/" +
                        "div[@class='ui-datepicker-title']/span[@class='ui-datepicker-year']"
        )));
        String datePickerYearText = datePickerYear.getText();

        WebElement datePickerMonth = browser.findElement(By.xpath(
                "//div[@id='ui-datepicker-div']/div[@class='ui-datepicker-group ui-datepicker-group-first']/" +
                        "div[@class='ui-datepicker-header ui-widget-header ui-helper-clearfix ui-corner-left']/" +
                        "div[@class='ui-datepicker-title']/span[@class='ui-datepicker-month']"
        ));
        String datePickerMonthText = datePickerMonth.getText();

        // Alters year
        while (!datePickerYearText.equals(settings.getReservationYear()))
        {
            // Right clicks the month until it is the correct year
            browser.findElement(By.className("ui-icon-circle-triangle-e")).click();

            // Updates the datepicker year
            datePickerYear = browser.findElement(By.xpath(
                    "//div[@id='ui-datepicker-div']/div[@class='ui-datepicker-group ui-datepicker-group-first']/" +
                            "div[@class='ui-datepicker-header ui-widget-header ui-helper-clearfix ui-corner-left']/" +
                            "div[@class='ui-datepicker-title']/span[@class='ui-datepicker-year']"
            ));
            datePickerYearText = datePickerYear.getText();
        }

        // Alters month
        while (!datePickerMonthText.equals(settings.getReservationMonth()))
        {
            // Right clicks the month until it is the correct month
            browser.findElement(By.className("ui-icon-circle-triangle-e")).click();

            // Updates the datepicker month
            datePickerMonth = browser.findElement(By.xpath(
                    "//div[@id='ui-datepicker-div']/div[@class='ui-datepicker-group ui-datepicker-group-first']/" +
                            "div[@class='ui-datepicker-header ui-widget-header ui-helper-clearfix ui-corner-left']/" +
                            "div[@class='ui-datepicker-title']/span[@class='ui-datepicker-month']"
            ));
            datePickerMonthText = datePickerMonth.getText();
        }

        // At this point, we are on the correct year & month. Now we select the date
        browser.findElement(By.linkText(settings.getReservationDay())).click();
    } // End of the setDate method

    /**
     * Sets the time based upon any offsets and the current number of successful attempts
     * @param browser The current browser for this run
     * @param offset The current amount of offsets (occurs when a time is already selected, but user is good)
     * @param pool a UserPool object containing all users for the program
     * @param settings a Settings object containing the program settings
     * @throws UserNumberException
     */
    private static TimeTuple setTime(WebDriver browser, int offset, UserPool pool, Settings settings)
            throws UserNumberException
    {
        // Selects the start time
        TimeTuple timeTuple = getTime(settings, pool.getNumberOfTerminatedUsers() + offset);
        int timeStart = timeTuple.getTimeStart();
        boolean am_pm = timeTuple.isAm_pm();

        Select reservationHour = new Select(browser.findElement(By.cssSelector("select#reservation_hour")));
        reservationHour.selectByValue(Integer.toString(timeStart));

        Select reservationMinute = new Select(browser.findElement(By.cssSelector("select#reservation_minute")));
        reservationMinute.selectByValue("0");

        // Selects AM/PM
        Select reservationAMPM = new Select(browser.findElement(By.cssSelector("select#reservation_ampm")));
        if (am_pm)
            reservationAMPM.selectByValue("am");
        else
            reservationAMPM.selectByValue("pm");

        // Selects the time length
        Select timeLength = new Select(browser.findElement(By.cssSelector("select#reservation_how_long")));
        timeLength.selectByValue("120");

        // Generates the room/time picker
        browser.findElement(By.xpath("//button[@id='generate_grid']")).click();

        return timeTuple;
    } // End of the setTime method

    /**
     * Selects the room in the room picker
     * @param browser The current browser for this run
     * @param user The current user for this run
     * @param wait The fluent wait that is to be used when waiting for an element to appear
     */
    private static void selectRoom(WebDriver browser, User user, FluentWait<WebDriver> wait, Settings settings)
            throws ReservationException, TimeSlotTakenException, InterruptedException
    {
        WebElement alert = null;
        try
        {
            alert = wait.until((Function<WebDriver, WebElement>) driver -> driver.findElement(By.xpath(
                    "//div[@class='alert alert-danger']")));
        }
        catch (TimeoutException e)
        {
            // Does nothing, since it's a good thing (no alert popped up in 20 seconds)
        }

        if (alert != null)
            throw new ReservationException("Error: User " + user.getUsername() + " has already registered today");

        // Fills in the description
        WebElement descriptionElement = wait.until((Function<WebDriver, WebElement>) driver -> driver.findElement(
                By.id("reservation_title")));
        descriptionElement.sendKeys(settings.getDescription());

        // Fills in the duplicate email for the booking
        WebElement duplicateEmailElement = browser.findElement(By.id("reservation_cc"));
        duplicateEmailElement.sendKeys(user.getEmailDuplicate());

        // Selects the row on the room picker
        String roomText = "Bobst " + settings.getFloorNumber() + "-" + settings.getRoomNumber();

        // Locates the room
        WebElement divFind = browser.findElement(
                By.xpath("//form[@id='new_reservation']/table[@id='availability_grid_table']/" +
                        "tbody/tr[contains(., '" + roomText + "')]")
        );

        WebElement timeSlot;
        // Selects the time slot if possible
        try
        {timeSlot = divFind.findElement(By.xpath("td[@class='timeslot timeslot_available timeslot_preferred']"));}
        catch (NoSuchElementException e)
        {throw new TimeSlotTakenException("Error: The current time slot was already taken, moving to next user");}

        timeSlot.click();

        // Submits
        try
        {browser.findElement(By.xpath("//button[contains(text(),'Reserve selected timeslot')]")).click();}
        catch (NoSuchElementException e)
        {throw new ReservationException("Error: User " + user.getUsername() + " was unable to submit the request.");}

        // Waits a bit for confirmation to occur
        wait.until((Function<WebDriver, WebElement>) driver -> driver.findElement(By.xpath(
                "//div[@class='alert alert-success']")));
    } // End of the selectRoom method


    /**
     * Updates the log at the end, and marks the user as completed
     * @param user The current user for this run
     * @param pool a UserPool object containing all users for the program
     * @param timeTuple A TimeTuple object containing the current registration time
     * @throws InterruptedException
     */
    private static void updateLog(User user, UserPool pool, TimeTuple timeTuple) throws InterruptedException
    {
        String militaryTime = toMilitaryTime(timeTuple.getTimeStart());
        System.out.println(militaryTime + ": Reserved");
        Thread.sleep(5000);
        pool.markUserCompleted(user);
    } // End of the updateLog method

    /***** Helper Methods *****/
    /**
     * Gets the time that this current user will be signing up for
     * @param userIndex The current user index, from [0, 11]
     * @return An int between 0 and 24, and sets the global variable AM_PM to correct value
     */
    private static TimeTuple getTime(Settings settings, int userIndex) throws UserNumberException
    {
        if ((userIndex < 0) || (userIndex > 11))
            throw new UserNumberException("User number is invalid: " + userIndex);

        int timeStart = settings.getTimePreference()[userIndex];
        boolean am_pm = true;

        if ((timeStart >= 12) && (timeStart < 24))
        {
            am_pm = false;
            timeStart -= 12;
        }

        if (timeStart == 0)
            timeStart = 12;

        return new TimeTuple(timeStart, am_pm);
    } // End of the getTime method

    /**
     * Converts the user's time to military time (i.e. 2200) for logging purposes
     * @param time The time that is being converted
     * @return A string with the military version of time
     */
    private static String toMilitaryTime(int time)
    {
        String stringTime;

        if (time < 10)
            stringTime = "0" + Integer.toString(time) + "00";
        else
            stringTime = Integer.toString(time) + "00";
        return stringTime;
    } // End of the toMilitaryTime method
} // End of the run automator class
