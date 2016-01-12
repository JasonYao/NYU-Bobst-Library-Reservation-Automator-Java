package nyu.yao.automator;

/**
 * Selenium imports
 */
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Time imports
 */
import java.time.LocalDate;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Logging file imports
 */
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jason Yao
 * This Java class is an executable that is able to automatically reserve a room at NYU's Bobst Library
 */
public class Automator
{
	/***** Wrapper Methods *****/

	/**
	 * Start of program, wrapper function for all subsequent calls
	 * @param args Commandline arguments (should be none)
     */
	public static void main(String[] args)
	{
		Setup setup = setup();
		runAndCleanup(setup);
	} // End of the main method

	/**
	 * Initial setup of program, including settings input, logging setup, and user pool creation
	 * @return Returns a Setup object containing the program settings, logging streams, and user pool
     */
	private static Setup setup()
	{
		Settings settings = getAndSetSettings();
		Logger logger = setLogging(settings);
		UserPool userPool = createUsers(settings);
		return new Setup(settings, logger, userPool);
	} // End of the setup method

	/**
	 * Wrapper for the actual program execution and graceful cleanup
	 * @param setup A Setup object containing the program settings, logging streams, and user pool
     */
	private static void runAndCleanup(Setup setup)
	{
		runAutomator(setup);
		closeLoggingStreams(setup);
	} // End of the run and clean up method

	/***** Setup Methods *****/

	/**
	 * Initial read in of settings from the settings configuration file
	 * @return Returns a Settings object containing the program settings
     */
	private static Settings getAndSetSettings()
	{
		// Turns off annoying html-unit warnings
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

		// Setting inheritance stuff
		Properties settings = new Properties();
		InputStream input = null;

		int timeDelta;
		String description = null;
		String floorNumber= null;
		String roomNumber= null;
		String userLoginFilePath= null;
		int[] timePreference= null;
		Target target = null;

		try
		{
			try
			{
				input = new FileInputStream("settings");
				settings.load(input);
			}
			catch (IOException e)
			{throw new IOException("Settings file could not be read correctly");}

			// Initializes each value from the defaults to the values in the settings file
			timeDelta = Integer.parseInt(settings.getProperty("timeDelta"));
			description = settings.getProperty("description");
			floorNumber = settings.getProperty("floorNumber");
			roomNumber = settings.getProperty("roomNumber");
			userLoginFilePath = settings.getProperty("userLoginFile");

			// Tokenises the time preference string into an int array
			String[] timePreferenceString = settings.getProperty("timePreference").split(" ");
			timePreference = new int[timePreferenceString.length];

			for (int i = 0; i < timePreferenceString.length; i++)
			{
				try
				{timePreference[i] = Integer.parseInt(timePreferenceString[i]);}
				catch (NumberFormatException e)
				{throw new NumberFormatException("Unable to correctly parse the time preference from the settings file");}
			}
			target = setTargetDate(timeDelta);
		}
		catch (IOException | MonthException | NumberFormatException e)
		{
			System.err.println(e.getMessage());
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

		return new Settings(description, floorNumber, roomNumber, userLoginFilePath, timePreference, target);
	} // End of the get and set settings method

	/**
	 * Sets up logging streams for the program
	 * @param settings a Settings object containing the program settings
	 * @return A Logger object containing the logging streams
     */
	private static Logger setLogging(Settings settings)
	{
		Path logs = Paths.get("logs");
		Path errors = Paths.get("logs/errors");
		Path status = Paths.get("logs/status");
		PrintStream stdErrPrintStream = null; // Error logging
		PrintStream stdOutPrintStream = null; // Status logging

		try
		{
			try
			{
				if (Files.notExists(logs)) {Files.createDirectory(logs);}
				if (Files.notExists(errors)) {Files.createDirectory(errors);}
				if (Files.notExists(status)) {Files.createDirectory(status);}
			}
			catch (IOException e)
			{throw new IOException("Error: unable to create logging directories");}

			String reservationDate = settings.getReservationYear() + "-" + settings.getReservationMonth()
					+ "-" + settings.getReservationDay();

			try
			{
				Path statusFilePath = Paths.get("logs/status/" + reservationDate  + ".status");
				Path errorFilePath = Paths.get("logs/errors/" + reservationDate  + ".err");

				// Creates the logging files if they don't exist yet
				if (!statusFilePath.toFile().exists())
					statusFilePath = Files.createFile(Paths.get("logs/status/" + reservationDate  + ".status"));

				if (!errorFilePath.toFile().exists())
					errorFilePath = Files.createFile(Paths.get("logs/errors/" + reservationDate  + ".err"));

				// Constructs the logging print streams using the new files
				stdOutPrintStream = new PrintStream(statusFilePath.toFile());
				stdErrPrintStream = new PrintStream(errorFilePath.toFile());

				// Sets the logging output to the newly constructed streams
				System.setOut(stdOutPrintStream);
				System.setErr(stdErrPrintStream);
			}
			catch (FileNotFoundException e)
			{throw new IOException("Error: could not find the logging files");}
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return new Logger(stdOutPrintStream, stdErrPrintStream);
	} // End of the set logging method

	/**
	 * Creates a user pool based off of the user login information .csv file
	 * @param settings a Settings object containing the program settings
	 * @return Returns a UserPool object containing all users for the program
     */
	private static UserPool createUsers(Settings settings)
	{
		// Checks for user logins .csv file existence
		Path userLogin = Paths.get(settings.getUserLoginFilePath());

		// Sanity check for user login .csv file
		try
		{
			if (Files.notExists(userLogin) || (Files.isDirectory(userLogin)))
				throw new IOException("Error: The user login info .csv file does not exist, or is a directory");
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// Creates the user pool
		return UserPool.createPool(userLogin);
	} // End of the create users method

	/***** Run & Cleanup Methods *****/
	/**
	 * Wrapper & elegant error handling method for all runs in the automator
	 * @param setup a Setup object containing the program settings, logging streams, and user pool
     */
	private static void runAutomator(Setup setup)
	{
		// Local references for code clarity & efficient call operations
		UserPool pool = setup.getUserPool();

		// Logging stuff
		PrintStream status = setup.getLogger().getStdOutStream();
		PrintStream errors = setup.getLogger().getStdErrStream();

		// Running variables
		int offset = 0; // Represents offset in the timePreference array (say when it's already booked)
		WebDriver browser = null;
		User user = null;

		// Start of iterating through all users
		while (!pool.isCompleted() && pool.getNumberOfTerminatedUsers() < 12)
		{
			try
			{
				int userIndex = pool.getNextValidUserIndex();

				if (userIndex == -1)
					throw new CompletedException("Error: All users have been terminated or aborted");

				user = pool.getUsers().get(userIndex);
				browser = new FirefoxDriver();
				RunAutomator.run(browser, user, offset, setup);
			}
			catch (CompletedException e)
			{
				// All users have already completed their run, cleans up & breaks out of the loop early
				System.err.println(e.getMessage());
				errors.flush();
				cleanup(browser);
				break;
			}
			catch (TimeSlotTakenException e)
			{
				// The time slot has been taken, but the user is still valid.
				System.err.println(e.getMessage());
				errors.flush();
				++offset;
			}
			catch (InterruptedException e)
			{
				// Sleep was interrupted during runtime
				System.err.println("Error: Sleep was interrupted during runtime");
				errors.flush();

				assert user != null;
				System.out.println("User " + user.getUsername() + " status: aborted");
				status.flush();

				pool.markUserAborted(user);
			}
			catch (UserNumberException | ReservationException | TimeoutException | InvalidLoginException e)
			{
				// Something went wrong, and the user is marked as unable to continue
				System.err.println(e.getMessage());
				errors.flush();

				assert user != null;
				System.out.println("User " + user.getUsername() + " status: aborted");
				status.flush();
				pool.markUserAborted(user);
			}
			catch (Exception e)
			{
				// I've seen some shit man.
				System.err.println("Error: Shit, something's on fire");
				errors.flush();

				assert user != null;
				System.out.println("User " + user.getUsername() + " status: aborted");
				status.flush();
				pool.markUserAborted(user);
				e.printStackTrace();
			}
			finally
			{cleanup(browser);}
		} // End of iterating through all users
	} // End of the run automator method

	/**
	 * [Cleanup Method] Closes all logging redirect streams
	 * @param setup A Setup containing all Settings, Loggers, and user pools
     */
	private static void closeLoggingStreams(Setup setup)
	{
		// Closes std.out redirect stream
		if (setup.getLogger().getStdOutStream() != null)
			setup.getLogger().getStdOutStream().close();

		// Closes std.err redirect stream
		if (setup.getLogger().getStdErrStream() != null)
			setup.getLogger().getStdErrStream().close();
	} // End of the close logging streams method

	/***** Helper Methods *****/

	/**
	 * [Helper Method] Turns an int in a string month into the string form of the month, i.e. '01' == January
	 * @param someMonth A string value in the form 'XY', where X and Y are digits, between '01' and '12'
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
	 * [Helper Method] Calculates the target date given a time delta
	 * @param timeDelta The number of days from today that is the target
	 * @return Returns a Target containing the reservation year, month and day
	 * @throws MonthException Throws an exception when the reservation month could not be parsed
     */
	private static Target setTargetDate(int timeDelta) throws MonthException
	{
		// Gets the reservation date only once at the start
		LocalDate currentDate = LocalDate.now();
		LocalDate reservationDate = currentDate.plusDays(timeDelta);

		// Date in string format
		String reservationYear = Integer.toString(reservationDate.getYear());
		String reservationMonth = toMonth(Integer.toString(reservationDate.getMonthValue()));
		String reservationDay = Integer.toString(reservationDate.getDayOfMonth());
		return new Target(reservationYear, reservationMonth, reservationDay);
	} // End of the set target date method

	/**
	 * [Helper Method] Gracefully cleans up all browser-related things, including cache and cookie clearing
	 * @param browser The browser that is bring shutdown
     */
	private static void cleanup(WebDriver browser)
    {
        // Logs out
        browser.get("https://rooms.library.nyu.edu/logout");
        try
        {Thread.sleep(10000);}
        catch (InterruptedException e)
        {System.err.println("Error: Cleanup of browser was interrupted when trying to sleep after logout");}

        // Deletes cookies
        browser.manage().deleteAllCookies();
        browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        // Closes the browser connection
        browser.close();
        try {Thread.sleep(5000);}
        catch (InterruptedException e)
        {System.err.println("Error: Closing of browser was interrupted when trying to sleep after browser close");}
    } // End of the cleanup method

	/* Custom Exception handling */
	protected static class CompletedException extends Throwable {public CompletedException(String s) {super(s);}}
	protected static class TimeSlotTakenException extends Throwable {public TimeSlotTakenException(String s) {super(s);}}
	protected static class MonthException extends Throwable {public MonthException(String s) {super(s);}}
	protected static class ReservationException extends Throwable {public ReservationException(String s) {super(s);}}
	protected static class UserNumberException extends Throwable {public UserNumberException(String s) {super(s);}}
	protected static class InvalidLoginException extends Throwable {public InvalidLoginException(String s) {super(s);}}
} // End of the Automator class