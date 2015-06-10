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
import java.time.*;
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

	/**
	 * Main function to run the Automator
	 * @param args Allows 1 argument for the file name of the user logins in .csv format
	 */
	public static void main(String[] args)
	{
		// Turns off annoying htmlunit warnings
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

		// Setting inheritance stuff
		Properties settings = new Properties();
		InputStream input = null;

		// Settings variables to be changed
		int timeDelta = 90;
		String description = "NYU Phi Kappa Sigma Study Session";
		String floorNumber = "LL1";
		String roomNumber = "20";
		String userLoginsFilePath = "userLogins.csv";

		try
		{
			input = new FileInputStream("settings");
			settings.load(input);

			// Initializes each value from the defaults to the values in the settings file
			timeDelta = Integer.parseInt(settings.getProperty("timeDelta"));
			description = settings.getProperty("description");
			floorNumber = settings.getProperty("floorNumber");
			roomNumber = settings.getProperty("roomNumber");
			userLoginsFilePath = settings.getProperty("userLoginFile");
		}
		catch (IOException ex)
		{System.err.println("Settings file could not be read correctly");}
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

		// Gets the reservation date only once at the start
		LocalDate currentDate = LocalDate.now();
		LocalDate reservationDate = currentDate.plusDays(timeDelta);

		// Date in string format
		String reservationYear = Integer.toString(reservationDate.getYear());
		String reservationMonth = "";

		try
		{reservationMonth = toMonth(Integer.toString(reservationDate.getMonthValue()));}
		catch (MonthException e)
		{System.out.println(e.getMessage());}

		String reservationDay = Integer.toString(reservationDate.getDayOfMonth());

		// Logging capability stuff
		File logs = null;

		// Error logging
		PrintStream pErr = null;
		FileOutputStream fErr = null;
		File errors = null;

		// Status logging
		PrintStream pOut = null;
		FileOutputStream fOut = null;
		File status = null;

		try
		{
			// Creates the directory hierarchy
			logs = new File("~/projects/NYU-Bobst-Library-Reservation-Automator-Java/logs");
			if (!logs.isDirectory())
				logs.mkdir();
			status = new File("~/projects/NYU-Bobst-Library-Reservation-Automator-Java/logs/status");
			if (!status.isDirectory())
				status.mkdir();
			errors = new File("~/projects/NYU-Bobst-Library-Reservation-Automator-Java/logs/errors");
			if (!errors.isDirectory())
				errors.mkdir();

			fOut = new FileOutputStream(
					"~/projects/NYU-Bobst-Library-Reservation-Automator-Java/logs/status/" + reservationDate.toString() + ".status");
			fErr = new FileOutputStream(
					"~/projects/NYU-Bobst-Library-Reservation-Automator-Java/logs/errors/" + reservationDate.toString() + ".err");
			pOut = new PrintStream(fOut);
			pErr = new PrintStream(fErr);
			System.setOut(pOut);
			System.setErr(pErr);

		}
		catch (FileNotFoundException e2)
		{System.err.println("Couldn't find the logging file");}

		// Checks for user logins .csv file existence
		File userLogins = new File(userLoginsFilePath);

		try
		{
			if (!userLogins.exists() || (userLogins.isDirectory()))
				throw new IOException("userLogins.csv does not exist, or is a directory");
		}
		catch (IOException e)
		{System.err.println(e.getMessage());}

		// Builds an array of users based off of .csv
		// Opens file stream
		FileReader fr = null;
		BufferedReader br = null;
		StringTokenizer st = null;
		ArrayList<User> users = new ArrayList<User>();
		try {
			fr = new FileReader(userLogins);
			br = new BufferedReader(fr);
			boolean lineSkip = true;

			for (String line; (line = br.readLine()) != null; )
			{
				if (lineSkip)
					lineSkip = false;
				else
				{
					boolean timestampSkip = true;
					st = new StringTokenizer(line, ",");
					while (st.hasMoreTokens())
					{
						// Advances past the timeStamp
						if (timestampSkip)
						{
							timestampSkip = false;
							st.nextToken();
						}
						else
						{
							String username = st.nextToken();
							String password = st.nextToken();

							// Advances past the years until graduation
							while (st.hasMoreTokens())
								st.nextToken();
							users.add(new User(username, password));
						}
					} // End of while loop
				} // End of else
			} // End of the for loop
		} // End of the try block
		catch (IOException e1)
		{System.err.println("File could not be found");}
		finally
		{
			try
			{
				fr.close();
				br.close();
			}
			catch (IOException e)
			{System.err.println("File error while trying to close file");}
		}

		// Start of going through the registration with each user
		for (int i = 0; i < users.size(); ++i)
		{
			// Resets the AM at the end of the loop, since it's a static variable
			AM_PM = true;

			// Builds a browser connection
			WebDriver browser = new FirefoxDriver();

			//HtmlUnitDriver browser = new HtmlUnitDriver(BrowserVersion.CHROME);
			//browser.setJavascriptEnabled(true);

			try
			{
				// Starts automation for user
				System.out.println("User number: " + i + " status: starting");

				browser.get("https://login.library.nyu.edu/pds?func=load-login&institute=NYU&calling_system=https:"
						+ "login.library.nyu.edu&url=https%3A%2F%2Frooms.library.nyu.edu%2Fvalidate%3Freturn_url%3Dhttps"
						+ "%253A%252F%252Frooms.library.nyu.edu%252F%26https%3A%2F%2Flogin.library.nyu.edu_action%3Dnew%2"
						+ "6https%3A%2F%2Flogin.library.nyu.edu_controller%3Duser_sessions");

				// Sleep until the div we want is visible or 15 seconds is over
				FluentWait<WebDriver> fluentWait = new FluentWait<WebDriver>(browser)
						.withTimeout(20, TimeUnit.SECONDS)
						.pollingEvery(500, TimeUnit.MILLISECONDS)
						.ignoring(NoSuchElementException.class);

				fluentWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='shibboleth']")));

				browser.findElement(By.xpath("//div[@id='shibboleth']/p[1]/a")).click();

				fluentWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form[@id='login']")));

				// Now we're at the login page
				WebElement username = browser.findElement(By.xpath("//form[@id='login']/input[1]"));
				WebElement password = browser.findElement(By.xpath("//form[@id='login']/input[2]"));

				// Signs into the bobst reserve with the user's username and password
				username.sendKeys(users.get(i).getUsername());
				password.sendKeys(users.get(i).getPassword()); 
				browser.findElement(By.xpath("//form[@id='login']/input[3]")).click();

				if (browser.getCurrentUrl().equals("https://shibboleth.nyu.edu:443/idp/Authn/UserPassword") ||
						(browser.getCurrentUrl().equals("https://shibboleth.nyu.edu/idp/Authn/UserPassword")))
					throw new InvalidLoginException("User " + i + " had invalid login credentials");

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
				while (!datePickerYearText.equals(reservationYear))
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

				// ALters month
				while (!datePickerMonthText.equals(reservationMonth))
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
				browser.findElement(By.linkText(reservationDay)).click();

				// END OF THE FUCKING DATEPICKER

				// Selects the start time
				int timeStart = getTime(i);

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
					throw new ReservationException("The user number: " + i + " has already reserved a room for today");

				// Waits for the reservation to pop up
				fluentWait.until(
						ExpectedConditions.presenceOfElementLocated(
								By.xpath("//div[@class='modal-content']/div[@class='modal-body']/div[@class='modal-body-content']")
								));

				WebElement descriptionElement = fluentWait.until(
						ExpectedConditions.presenceOfElementLocated(By.id("reservation_title")));
				descriptionElement.sendKeys(description);

				// Fills in the duplicate email for the booking
				WebElement duplicateEmailElement = browser.findElement(By.id("reservation_cc"));
				duplicateEmailElement.sendKeys(users.get(i).getEmailDuplicate());

				// Selects the row on the room picker
				String roomText = "Bobst " + floorNumber + "-" + roomNumber;

				// Locates the room
				WebElement divFind = browser.findElement(
						By.xpath(
								"//form[@id='new_reservation']/table[@id='availability_grid_table']/tbody/tr[contains(., '" + roomText + "')]")
						);

				WebElement timeSlot = null;

				// Tries to get the next best time if it doesn't work
				try
				{
					timeSlot = divFind.findElement(By.xpath("td[@class='timeslot timeslot_available timeslot_preferred']"));
				}
				catch (NoSuchElementException e)
				{
					System.err.println("The timeslot was already taken for user: " + i + ", taking next best time");
					boolean found = false;

					// Continuously clicks the next button and checks until a time is found
					while (found == false)
					{
						try
						{
							// Clicks the button once
							browser.findElement(By.xpath("//div[@class='rebuild_grid rebuild_grid_next']")).click();

							// Rechecks to find the timeSlot
							timeSlot = divFind.findElement(By.xpath("td[@class='timeslot timeslot_available timeslot_preferred']"));

							// If it gets to this point, the timeslot is found, sets found = true
							found = true;
						}
						catch (NoSuchElementException ex)
						{
							// Still didn't find an available timeslot, continues search
						}	
					} // End of while loop
				} // End of finding a time if original preference could not be found

				timeSlot.click();

				// Submits
				browser.findElement(By.xpath("//button[@class='btn btn-lg btn-primary']")).click();

				// Waits a bit for confirmation to occur
				FluentWait<WebDriver> buttonWait = new FluentWait<WebDriver>(browser)
						.withTimeout(15, TimeUnit.SECONDS)
						.pollingEvery(500, TimeUnit.MILLISECONDS)
						.ignoring(NoSuchElementException.class);

				buttonWait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='alert alert-success']")));

				// Final status update
				System.out.println("User number " + i + " status: successful");

				// Updates the dailyStatus log
				String militaryTime = toMilitaryTime(i);
				System.out.println(militaryTime + ": Reserved");
				Thread.sleep(3000);
			}
			catch (UserNumberException e)
			{
				System.err.println(e.getMessage());
				System.out.println("User number " + i + " status: failed");

				// Updates the dailyStatus log
				String militaryTime = toMilitaryTime(i);
				System.out.println(militaryTime + ": Not Reserved");
			}
			catch(ReservationException e)
			{
				System.err.println(e.getMessage());
				System.out.println("User number " + i + " status: failed");

				// Updates the dailyStatus log
				String militaryTime = toMilitaryTime(i);
				System.out.println(militaryTime + ": Not Reserved");
			}
			catch(TimeoutException e)
			{
				System.err.println(e.getMessage());
				System.out.println("User number " + i + " status: failed");

				// Updates the dailyStatus log
				String militaryTime = toMilitaryTime(i);
				System.out.println(militaryTime + ": Not Reserved");
			}
			catch(InvalidLoginException e)
			{
				System.err.println(e.getMessage());
				System.out.println("User number " + i + " status: failed");

				// Updates the dailyStatus log
				String militaryTime = toMilitaryTime(i);
				System.out.println(militaryTime + ": Not Reserved");
			}
			catch (InterruptedException e)
			{
				System.err.println("Sleep at end was interrupted");
				System.out.println("User number " + i + " status: failed");

				// Updates the dailyStatus log
				String militaryTime = toMilitaryTime(i);
				System.out.println(militaryTime + ": Not Reserved");
			}
			catch (Exception e)
			{
				System.err.println("Shit, something happened that wasn't caught");
				e.printStackTrace();

				// Updates the dailyStatus log
				String militaryTime = toMilitaryTime(i);
				System.out.println(militaryTime + ": Not Reserved");
			}
			finally
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
				System.out.println("Browser is now closed");
				try {Thread.sleep(5000);}
				catch (InterruptedException e)
				{System.err.println("Sleep at end was interrupted");}
			}
		} //End of for loop

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
	} // End of the main method

	/* Helper Methods */

	/**
	 * Converts the user's time to military time (i.e. 2200) for logging purposes
	 * @param userID The position of the user
	 * @return A string with the military version of time
	 */
	private static String toMilitaryTime(int userID)
	{
		int time = timePreference[userID];
		String stringTime = "";

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
			if (someMonth.equals("1"))
				returnMonth = "January";
			else if (someMonth.equals("2"))
				returnMonth = "February";
			else if (someMonth.equals("3"))
				returnMonth = "March";
			else if (someMonth.equals("4"))
				returnMonth = "April";
			else if (someMonth.equals("5"))
				returnMonth = "May";
			else if (someMonth.equals("6"))
				returnMonth = "June";
			else if (someMonth.equals("7"))
				returnMonth = "July";
			else if (someMonth.equals("8"))
				returnMonth = "August";
			else if (someMonth.equals("9"))
				returnMonth = "September";
			else if (someMonth.equals("10"))
				returnMonth = "October";
			else if (someMonth.equals("11"))
				returnMonth = "November";
			else if (someMonth.equals("12"))
				returnMonth = "December";
			else
				throw new MonthException("Month could not be converted from: " + someMonth);

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
		int timeStart = 0;
		if ((userNumber < 0) || (userNumber > 11))
			throw new UserNumberException("User number is invalid: " + userNumber);
		timeStart = timePreference[userNumber];

		if ((timeStart >= 12) && (timeStart < 24))
		{
			setAM_PM(false);
			timeStart -= 12;
		}

		if (timeStart == 0)
			timeStart = 12;

		return timeStart;
	} // End of the getTime method

	/* Getters and Setters */

	/**
	 * @return the aM_PM
	 */
	public static boolean isAM_PM()
	{return AM_PM;}

	/**
	 * @param aM_PM the aM_PM to set
	 */
	public static void setAM_PM(boolean aM_PM)
	{AM_PM = aM_PM;}

	/**
	 * @return the timePreference
	 */
	public static int[] getTimePreference()
	{return timePreference;}

	/**
	 * @param timePreference the timePreference to set
	 */
	public static void setTimePreference(int[] timePreference)
	{Automator.timePreference = timePreference;}
} // End of the Automator class