package nyu.yao.automator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A "pool" of Users that can be thought of as a working set of Users for the program run
 * @author Jason Yao
 */
public class UserPool
{
    // Pseudo-immutable object attributes (no public setters)
    private ArrayList<User> users;
    private int numberOfTerminatedUsers = 0;
    private int numberOfAbortedUsers = 0;

    /**
     * Constructor for the User Pool class
     * @param users An empty arraylist of type User
     */
    public UserPool(ArrayList<User> users) {this.users = users;}

    /* Class Methods */

    /**
     * Checks whether the pool is completely run through
     * @return Returns true when the pool is completely run, false if there are users remaining
     */
    public boolean isCompleted()
    {return numberOfAbortedUsers + numberOfTerminatedUsers == users.size();} // End of the is completed method

    /**
     * Marks a given user as completed from the run
     * @param completedUser The user that is to be terminated
     */
    public void markUserCompleted(User completedUser)
    {
        completedUser.setStatus("completed");
        ++numberOfTerminatedUsers;
    } // End of the mark user terminated method

    /**
     * Marks a given user as aborted from the run
     * @param abortedUser The user that is to be aborted
     */
    public void markUserAborted(User abortedUser)
    {
        abortedUser.setStatus("aborted");
        ++numberOfAbortedUsers;
    } // End of the mark user aborted method

    /**
     * Gets the next valid user in the pool
     * @return Returns the next valid user's index
     */
    public int getNextValidUserIndex()
    {
        for (int i = 0; i < users.size(); ++i)
        {
            if (users.get(i).getStatus().equals("not started"))
                return i;
        }
        return -1;
    } // End of the get next valid user method

    /**
     * Creates a user pool given a path to the user login file
     * @param userLogin A Path to the user login information file
     * @return Returns a filled UserPool based upon the information in the user login file
     */
    public static UserPool createPool(Path userLogin)
    {
        // Readers, tokenisers, streams, and files
        File login = userLogin.toFile();
        FileReader loginFileReader = null;
        BufferedReader loginBufferedReader = null;
        StringTokenizer tokeniser;
        UserPool userPool = null;

        try
        {
            loginFileReader = new FileReader(login);
            loginBufferedReader = new BufferedReader(loginFileReader);
            userPool = new UserPool(new ArrayList<>());

            for (String line; (line = loginBufferedReader.readLine()) != null;)
            {
                tokeniser = new StringTokenizer(line, ",");
                while (tokeniser.hasMoreTokens())
                {
                    String username = tokeniser.nextToken();
                    String password = tokeniser.nextToken();
                    userPool.getUsers().add(new User(username, password));
                } // End of adding a single user to the pool
            } // End of adding users to the pool
        }
        catch (IOException e)
        {
            System.err.println("File " + login.toString() + " could not be found");
            System.exit(1);
        }
        finally
        {
            try
            {
                if (loginFileReader != null)
                    loginFileReader.close();
                if (loginBufferedReader != null)
                    loginBufferedReader.close();
            }
            catch (IOException e)
            {System.err.println("File error while trying to close file");}
        }
        return userPool;
    } // End of the create pool method

    /* Getters & Setters */
    public int getNumberOfTerminatedUsers() {return numberOfTerminatedUsers;}
    public ArrayList<User> getUsers() {return users;}
} // End of the user pool class
