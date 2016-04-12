package nyu.yao.automator;
/**
 * Class to contain all settings used for this program.
 * @author Jason Yao
 */
public class Settings
{
    // Pseudo-immutable object attributes (no public setters)
    private String description;
    private String floorNumber;
    private String roomNumber;
    private String userLoginFilePath;
    private int[] timePreference;

    private String reservationYear;
    private String reservationMonth;
    private String reservationDay;

    /**
     * Constructor for the Settings class
     * @param description The description of the room reservation
     * @param floorNumber The floor number of the room that is being reserved
     * @param roomNumber The room number of the room that is being reserved
     * @param userLoginFilePath The file path to the user login .csv file
     * @param timePreference The given time preference array of times, given as a space-delimited int array
     * @param target A Target containing the target reservation year, month, and day
     */
    protected Settings (String description, String floorNumber,
                        String roomNumber, String userLoginFilePath, int[] timePreference, Target target)
    {
        setDescription(description);
        setFloorNumber(floorNumber);
        setRoomNumber(roomNumber);
        setUserLoginFilePath(userLoginFilePath);
        setTimePreference(timePreference);
        setReservationYear(target.getReservationYear());
        setReservationMonth(target.getReservationMonth());
        setReservationDay(target.getReservationDay());
    } // End of the settings constructor

    /* Getters & Setters */
    public String getDescription() {return description;}
    private void setDescription(String description) {this.description = description;}
    public String getFloorNumber() {return floorNumber;}
    private void setFloorNumber(String floorNumber) {this.floorNumber = floorNumber;}
    public String getRoomNumber() {return roomNumber;}
    private void setRoomNumber(String roomNumber) {this.roomNumber = roomNumber;}
    public String getUserLoginFilePath() {return userLoginFilePath;}
    private void setUserLoginFilePath(String userLoginFilePath) {this.userLoginFilePath = userLoginFilePath;}
    public int[] getTimePreference() {return timePreference;}
    private void setTimePreference(int[] timePreference) {this.timePreference = timePreference;}
    public String getReservationYear() {return reservationYear;}
    private void setReservationYear(String reservationYear) {this.reservationYear = reservationYear;}
    public String getReservationMonth() {return reservationMonth;}
    private void setReservationMonth(String reservationMonth) {this.reservationMonth = reservationMonth;}
    public String getReservationDay() {return reservationDay;}
    private void setReservationDay(String reservationDat) {this.reservationDay = reservationDat;}
} // End of the settings class
