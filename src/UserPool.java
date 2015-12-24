import java.util.ArrayList;

public class UserPool
{
    private ArrayList<User> users;
    private int numberOfTerminatedUsers = 0;
    private int numberOfAbortedUsers = 0;

    public UserPool(ArrayList<User> users) {this.users = users;}

    public boolean isCompleted()
    {return numberOfAbortedUsers + numberOfTerminatedUsers == users.size();} // End of the is completed method

    public void markUserCompleted(int indexOfCompletedUser)
    {
        users.get(indexOfCompletedUser).setStatus("completed");
        ++numberOfTerminatedUsers;
    } // End of the mark user terminated method

    public void markUserAborted(int indexOfAbortedUser)
    {
        users.get(indexOfAbortedUser).setStatus("aborted");
        ++numberOfAbortedUsers;
    } // End of the mark user aborted method

    public int getNextValidUser()
    {
        for (int i = 0; i < users.size(); ++i)
        {
            if (users.get(i).getStatus().equals("not started"))
                return i;
        }
        return -1;
    } // End of the get next valid user method

    public int getNumberOfTerminatedUsers() {return numberOfTerminatedUsers;}

    public ArrayList<User> getUsers() {return users;}
} // End of the user pool class
