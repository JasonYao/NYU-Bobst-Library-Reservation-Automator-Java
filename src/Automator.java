/**
 * Selenium imports
 */
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Support imports
 */
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

/**
 * Exception imports
 */
import org.openqa.selenium.NoSuchElementException;
import java.io.IOException;
import org.openqa.selenium.TimeoutException;
import java.io.FileNotFoundException;

/**
 * Time imports
 */
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * Reader imports
 */
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Settings file imports
 */
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Logging file imports
 */
import java.io.PrintStream;
import java.io.FileOutputStream;

/**
 * @author Jason Yao
 * This Java class is an executable that is able to automatically reserve a room at NYU's Bobst Library
 */
public class Automator
{
	// Global Variables
	private static boolean AM_PM = true; // Current time set is AM if true, PM if false
	private static int[] timePreference = {
			12, 14, 16, 18, 20, 22, 10, 8, 6, 4, 2, 0
	}; // The array of reservation times ordered in 2 hour blocks

	// Settings
	private static int TIME_DELTA;
	private static String DESCRIPTION;
	private static String FLOOR_NUMBER;
	private static String ROOM_NUMBER;
	private static String USER_LOGIN_FILEPATH;

	// Dates
	private static String RESERVATION_YEAR;
	private static String RESERVATION_MONTH;
	private static String RESERVATION_DAY;

	// Logging
		// Error logging
		private static PrintStream pErr;
		private static FileOutputStream fErr;

		// Status logging
		private static PrintStream pOut;
		private static FileOutputStream fOut;

	// Users
	private static UserPool USER_POOL;

	/**
	 * Main function to run the Automator
	 * @param args Allows 1 argument for the file name of the user logins in .csv format
	 */
	public static void main(String[] args)
	{
		getAndSetSettings();
		setTargetDate();
		setLogging();
		createUsers();
		runAutomator();
		closeLoggingStreams();
	} // End of the main method

	private static void runAutomator()
	{
		int currentCount = 0;
		int offset = 0;
		while ((!USER_POOL.isCompleted()) && (USER_POOL.getNumberOfTerminatedUsers() < 12))
		{
			AM_PM = true;
			int userIndex = -1;

			// Builds a browser connection
			WebDriver browser = new FirefoxDriver();
			browser.manage().window().maximize();

			try
			{
				userIndex = USER_POOL.getNextValidUser();
				if (userIndex == -1)
					throw new CompletedException("Error: All users have been terminated or aborted");
				User user = USER_POOL.getUsers().get(userIndex);

				// Starts automation for user
				browser.get("https://login.library.nyu.edu/users/auth/nyu_shibboleth?auth_type=nyu&institution=NYU");

				// Sleep until the div we want is visible or 15 seconds is over
				FluentWait<WebDriver> fluentWait = new FluentWait<>(browser)
						.withTimeout(20, TimeUnit.SECONDS)
						.pollingEvery(500, TimeUnit.MILLISECONDS)
						.ignoring(NoSuchElementException.class);

				fluentWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form[@id='login']")));
				// Now we're at the login page
				WebElement username = browser.findElement(By.xpath("//form[@id='login']/input[1]"));
				WebElement password = browser.findElement(By.xpath("//form[@id='login']/input[2]"));

				// Signs into the bobst reserve with the user's username and password
				username.sendKeys(user.getUsername());
				password.sendKeys(user.getPassword());
				browser.findElement(By.xpath("//form[@id='login']/input[3]")).click();

				if (browser.getCurrentUrl().equals("https://shibboleth.nyu.edu:443/idp/Authn/UserPassword") ||
						(browser.getCurrentUrl().equals("https://shibboleth.nyu.edu/idp/Authn/UserPassword")))
					throw new InvalidLoginException("User " + currentCount + " had invalid login credentials");

				browser.get("https://rooms.library.nyu.edu/");

				// START OF FUCKING AROUND WITH THE DATEPICKER
				// Error checking that rooms.library.nyu.edu pops up
				int count = 0;
				while ((!browser.getCurrentUrl().equals("https://rooms.library.nyu.edu/")) && (count < 5))
				{
					browser.navigate().refresh();
					Thread.sleep(5000);
					fluentWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='well well-sm']")));
					++count;
				}

				browser.findElement(By.xpath(
						"//form[@class='form-horizontal']/div[@class='well well-sm']" +
								"/div[@class='form-group has-feedback']/div[@class='col-sm-6']/input[1]"
				)).click();

				Thread.sleep(5000);

				// Checks the month and year, utilizes a wait for the year for the form to pop up
				WebElement datePickerYear = fluentWait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath(
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
				while (!datePickerYearText.equals(RESERVATION_YEAR))
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
				while (!datePickerMonthText.equals(RESERVATION_MONTH))
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
				browser.findElement(By.linkText(RESERVATION_DAY)).click();

				// END OF THE FUCKING DATEPICKER

				// Selects the start time
				int timeStart = getTime(USER_POOL.getNumberOfTerminatedUsers() + offset);

				Select reservationHour = new Select(browser.findElement(By.cssSelector("select#reservation_hour")));
				reservationHour.selectByValue(Integer.toString(timeStart));

				Select reservationMinute = new Select(browser.findElement(By.cssSelector("select#reservation_minute")));
				reservationMinute.selectByValue("0");

				// Selects AM/PM
				Select reservationAMPM = new Select(browser.findElement(By.cssSelector("select#reservation_ampm")));
				if (AM_PM)
					reservationAMPM.selectByValue("am");
				else
					reservationAMPM.selectByValue("pm");

				// Selects the time length
				Select timeLength = new Select(browser.findElement(By.cssSelector("select#reservation_how_long")));
				timeLength.selectByValue("120");

				// Generates the room/time picker
				browser.findElement(By.xpath("//button[@id='generate_grid']")).click();

				WebElement alert = null;
				// Checks if the user has already reserved a room for the day
				try
				{
					alert = fluentWait.until(
							ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='alert alert-danger']")));
				}
				catch (TimeoutException e)
				{
					// Does nothing, since it's a good thing
				}
				if (alert != null)
					throw new ReservationException("The user number: " + currentCount + " has already reserved a room for today");

				// Waits for the reservation to pop up
				fluentWait.until(
						ExpectedConditions.presenceOfElementLocated(
								By.xpath("//div[@class='modal-content']/div[@class='modal-body']/div[@class='modal-body-content']")
						));

				WebElement descriptionElement = fluentWait.until(
						ExpectedConditions.presenceOfElementLocated(By.id("reservation_title")));
				descriptionElement.sendKeys(DESCRIPTION);

				// Fills in the duplicate email for the booking
				WebElement duplicateEmailElement = browser.findElement(By.id("reservation_cc"));
				duplicateEmailElement.sendKeys(user.getEmailDuplicate());

				// Selects the row on the room picker
				String roomText = "Bobst " + FLOOR_NUMBER + "-" + ROOM_NUMBER;

				// Locates the room
				WebElement divFind = browser.findElement(
						By.xpath("//form[@id='new_reservation']/table[@id='availability_grid_table']/" +
										"tbody/tr[contains(., '" + roomText + "')]")
				);

				WebElement timeSlot;
				// Selects the timeslot if possible
				try
				{
					timeSlot = divFind.findElement(By.xpath("td[@class='timeslot timeslot_available timeslot_preferred']"));
				}
				catch (NoSuchElementException e)
				{
					throw new TimeslotTakenException("The time slot was already taken for user: " + userIndex + ", moving to next user");
				}

				timeSlot.click();

				// Submits
				browser.findElement(By.xpath("//button[@class='btn btn-lg btn-primary']")).click();

				// Waits a bit for confirmation to occur
				FluentWait<WebDriver> buttonWait = new FluentWait<>(browser)
						.withTimeout(15, TimeUnit.SECONDS)
						.pollingEvery(500, TimeUnit.MILLISECONDS)
						.ignoring(NoSuchElementException.class);

				buttonWait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='alert alert-success']")));

				// Updates the dailyStatus log
				String militaryTime = toMilitaryTime(USER_POOL.getNumberOfTerminatedUsers() + offset);
				System.out.println(militaryTime + ": Reserved");
				Thread.sleep(3000);
				USER_POOL.markUserCompleted(userIndex);
			}
			catch (TimeslotTakenException e)
			{
				// The timeslot has been taken, but the user is still valid.
				System.err.println(e.getMessage());
				++offset;
			}
			catch (CompletedException e)
			{
				System.err.println(e.getMessage());
				cleanup(browser);
				break;
			}
			catch (InterruptedException e)
			{
				System.err.println("Sleep at end was interrupted");
				System.out.println("User number " + currentCount + " status: failed");
				USER_POOL.markUserAborted(userIndex);
			}
			catch (UserNumberException | ReservationException | TimeoutException | InvalidLoginException e)
			{
				System.err.println(e.getMessage());
				System.out.println("User number " + currentCount + " status: failed");
				USER_POOL.markUserAborted(userIndex);
			}
			catch (Exception e)
			{
				System.err.println("Shit, something happened that wasn't caught");
				System.out.println("User number " + currentCount + " status: failed");
				USER_POOL.markUserAborted(userIndex);
				e.printStackTrace();
			}
			finally
			{cleanup(browser);}
		} // End of running through all users
	} // End of the run automator method

	private static void cleanup(WebDriver browser)
	{
		// Logs out
		browser.get("https://rooms.library.nyu.edu/logout");
		try
		{Thread.sleep(10000);}
		catch (InterruptedException e1)
		{System.err.println("Something wrong when trying to sleep after logout");}

		// Deletes cookies
		browser.manage().deleteAllCookies();
		browser.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		// Closes the browser connection
		browser.close();
		try {Thread.sleep(5000);}
		catch (InterruptedException e)
		{System.err.println("Sleep at end was interrupted");}
	} // End of the cleanup method

	private static void createUsers()
	{
		// Checks for user logins .csv file existence
		File userLogins = new File(USER_LOGIN_FILEPATH);

		try
		{
			if (!userLogins.exists() || (userLogins.isDirectory()))
				throw new IOException("userLogins.csv does not exist, or is a directory");
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// Builds an array of users based off of .csv
		FileReader fr = null;
		BufferedReader br = null;
		StringTokenizer st;
		try
		{
			fr = new FileReader(userLogins);
			br = new BufferedReader(fr);
			USER_POOL = new UserPool(new ArrayList<>());

			for (String line; (line = br.readLine()) != null;)
			{
				st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens())
				{
					String username = st.nextToken();
					String password = st.nextToken();
					USER_POOL.getUsers().add(new User(username, password));
				} // End of while loop
			}
		} // End of the try block
		catch (IOException e1)
		{System.err.println("File " + USER_LOGIN_FILEPATH + " could not be found");}
		finally
		{
			try
			{
				if (fr != null)
					fr.close();
				if (br != null)
					br.close();
			}
			catch (IOException e)
			{System.err.println("File error while trying to close file");}
		}
	} // End of the create users method

	private static void closeLoggingStreams()
	{
		// Closes logging streams
		if (pOut != null)
			pOut.close();
		if (pErr != null)
			pErr.close();
		if (fOut != null)
		{
			try
			{fOut.close();}
			catch (IOException e)
			{System.err.println("File output logging stream had errors when closing");}
		}
		if (fErr != null)
		{
			try
			{fErr.close();}
			catch (IOException e)
			{System.err.println("File output error stream had errors when closing");}
		}
	} // End of the close logging stream method


	private static void setLogging()
	{
		// Logging capability stuff
		File logs;
		File errors;
		File status;

		try
		{
			// Creates the directory hierarchy
			logs = new File("logs");
			if (!logs.isDirectory())
				logs.mkdir();
			status = new File("logs/status");
			if (!status.isDirectory())
				status.mkdir();
			errors = new File("logs/errors");
			if (!errors.isDirectory())
				errors.mkdir();

			String reservationDate = RESERVATION_YEAR + "-" + RESERVATION_MONTH + "-" + RESERVATION_DAY;

			fOut = new FileOutputStream(
					"logs/status/" + reservationDate  + ".status");
			fErr = new FileOutputStream(
					"logs/errors/" + reservationDate + ".err");
			pOut = new PrintStream(fOut);
			pErr = new PrintStream(fErr);
			System.setOut(pOut);
			System.setErr(pErr);
		}
		catch (FileNotFoundException ex)
		{System.err.println("Couldn't find the logging file");}
	} // End of the set logging method

	private static void setTargetDate()
	{
		// Gets the reservation date only once at the start
		LocalDate currentDate = LocalDate.now();
		LocalDate reservationDate = currentDate.plusDays(TIME_DELTA);

		// Date in string format
		RESERVATION_YEAR = Integer.toString(reservationDate.getYear());
		RESERVATION_MONTH = "";

		try
		{RESERVATION_MONTH = toMonth(Integer.toString(reservationDate.getMonthValue()));}
		catch (MonthException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}

		RESERVATION_DAY = Integer.toString(reservationDate.getDayOfMonth());
	} // End of the set target date method

	/* Helper Methods */
	private static void getAndSetSettings()
	{
		// Turns off annoying htmlunit warnings
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

		// Setting inheritance stuff
		Properties settings = new Properties();
		InputStream input = null;

		// Settings variables to be changed
		TIME_DELTA = 90;
		DESCRIPTION = "NYU Phi Kappa Sigma Study Session";
		FLOOR_NUMBER = "LL1";
		ROOM_NUMBER = "20";
		USER_LOGIN_FILEPATH = "userLogins.csv";

		try
		{
			input = new FileInputStream("settings");
			settings.load(input);

			// Initializes each value from the defaults to the values in the settings file
			TIME_DELTA = Integer.parseInt(settings.getProperty("timeDelta"));
			DESCRIPTION = settings.getProperty("description");
			FLOOR_NUMBER = settings.getProperty("floorNumber");
			ROOM_NUMBER = settings.getProperty("roomNumber");
			USER_LOGIN_FILEPATH = settings.getProperty("userLoginFile");
		}
		catch (IOException ex)
		{
			System.err.println("Settings file could not be read correctly");
			System.exit(1);
		}
		finally
		{
			if (input != null)
			{
				try
				{input.close();}
				catch (IOException e)
				{System.err.println("Error trying to close stream from settings file");}
			}
		}
	} // End of the set settings class

	/**
	 * Converts the user's time to military time (i.e. 2200) for logging purposes
	 * @param userID The position of the user
	 * @return A string with the military version of time
	 */
	private static String toMilitaryTime(int userID)
	{
		int time = timePreference[userID];
		String stringTime;

		if (time < 10)
			stringTime = Integer.toString(time) + "000";
		else
			stringTime = Integer.toString(time) + "00";
		return stringTime;
	} // End of the timeConvert method

	/**
	 * Turns an int in a string month into the string form of the month, i.e. '01' == January
	 * @param someMonth A string value in the form 'XY', where X and Y are digits, between '01'
	 * and '12'
	 * @throws MonthException when the input could not be converted into a month
	 */
	private static String toMonth(String someMonth) throws MonthException
	{
		String returnMonth = "";
		try
		{
			switch (someMonth) {
				case "1":
					returnMonth = "January";
					break;
				case "2":
					returnMonth = "February";
					break;
				case "3":
					returnMonth = "March";
					break;
				case "4":
					returnMonth = "April";
					break;
				case "5":
					returnMonth = "May";
					break;
				case "6":
					returnMonth = "June";
					break;
				case "7":
					returnMonth = "July";
					break;
				case "8":
					returnMonth = "August";
					break;
				case "9":
					returnMonth = "September";
					break;
				case "10":
					returnMonth = "October";
					break;
				case "11":
					returnMonth = "November";
					break;
				case "12":
					returnMonth = "December";
					break;
				default:
					throw new MonthException("Month could not be converted from: " + someMonth);
			}
		}
		catch (MonthException e)
		{System.out.println(e.getMessage());}
		return returnMonth;
	} // End of the toMonth method

	/**
	 * Gets the time that this current user will be signing up for
	 * @param userNumber The current user number, from 0 -> 11
	 * @return An int between 0 and 24, and sets the global variable AM_PM to correct value
	 */
	private static int getTime(int userNumber) throws UserNumberException
	{
		int timeStart;
		if ((userNumber < 0) || (userNumber > 11))
			throw new UserNumberException("User number is invalid: " + userNumber);
		timeStart = timePreference[userNumber];

		if ((timeStart >= 12) && (timeStart < 24))
		{
			AM_PM = false;
			timeStart -= 12;
		}

		if (timeStart == 0)
			timeStart = 12;

		return timeStart;
	} // End of the getTime method

	/* Custom Exception handling */
	private static class CompletedException extends Throwable {public CompletedException(String s) {super(s);}}
	private static class TimeslotTakenException extends Throwable {public TimeslotTakenException(String s) {super(s);}}
	private static class MonthException extends Throwable {public MonthException(String s) {super(s);}}
	private static class ReservationException extends Throwable {public ReservationException(String s) {super(s);}}
	private static class UserNumberException extends Throwable {public UserNumberException(String s) {super(s);}}
	private static class InvalidLoginException extends Throwable {public InvalidLoginException(String s) {super(s);}}
} // End of the Automator class