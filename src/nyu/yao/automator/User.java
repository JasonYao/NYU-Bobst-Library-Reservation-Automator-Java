package nyu.yao.automator;

/**
 * User class for creating User objects that contain login credentials for NYU services
 * @author Jason Yao
 */
public class User
{
	private String username;
	private String password;
	private String emailDuplicate;
	private String status;

	/**
	 * Constructor for the User class
	 * @param username The user's username (netid, e.g. jy1299) used to login to NYU services
	 * @param password The user's password used to login to NYU services
	 */
	public User(String username, String password)
	{
		this.username = username;
		this.password = password;
		this.emailDuplicate = username + "+NYU@nyu.edu";
		this.status = "not started";
	} // End of the user's constructor

	/* Getters & Setters */
	public String getStatus() {return status;}
	public void setStatus(String status) {this.status = status;}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public String getEmailDuplicate() {
		return emailDuplicate;
	}
} // End of the User class