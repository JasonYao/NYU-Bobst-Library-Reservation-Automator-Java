/**
 * 
 */

/**
 * @author Jason
 *
 */
public class User
{
	private String username;
	private String password;
	private String email;
	private String emailDuplicate;
	private String status;

	/**
	 * Constructor for the User class
	 * @param username
	 * @param password
	 */
	public User(String username, String password)
	{
		this.username = username;
		this.password = password;
		this.email = username + "@nyu.edu";
		this.emailDuplicate = username + "+NYU@nyu.edu";
		this.status = "not started";

	} // End of the user's constructor

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