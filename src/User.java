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
	} // End of the user's constructor

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmailDuplicate() {
		return emailDuplicate;
	}

	public void setEmailDuplicate(String emailDuplicate) {
		this.emailDuplicate = emailDuplicate;
	}
} // End of the User class