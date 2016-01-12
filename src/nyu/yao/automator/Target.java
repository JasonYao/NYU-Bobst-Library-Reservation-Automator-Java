package nyu.yao.automator;
/**
 * Class to contain the target date
 * @author Jason Yao
 */
public class Target
{
    // Pseudo-immutable object attributes (no public setters)
    private String reservationYear;
    private String reservationMonth;
    private String reservationDay;

    /**
     * Constructor for the Target class
     * @param reservationYear The year of the target date of the reservation
     * @param reservationMonth The month of the target date of the reservation
     * @param reservationDay The day of the target date of the reservation
     */
    public Target(String reservationYear, String reservationMonth, String reservationDay)
    {
        setReservationYear(reservationYear);
        setReservationMonth(reservationMonth);
        setReservationDay(reservationDay);
    } // End of the constructor

    /* Getters & Setters */
    public String getReservationYear() {return reservationYear;}
    private void setReservationYear(String reservationYear) {this.reservationYear = reservationYear;}
    public String getReservationMonth() {return reservationMonth;}
    private void setReservationMonth(String reservationMonth) {this.reservationMonth = reservationMonth;}
    public String getReservationDay() {return reservationDay;}
    private void setReservationDay(String reservationDay) {this.reservationDay = reservationDay;}
} // End of the target class
