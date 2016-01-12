package nyu.yao.automator;

/**
 * Class to contain the time tuple containing the reservation time & whether it's AM or PM
 * @author Jason Yao
 */
public class TimeTuple
{
    // Pseudo-immutable object attributes (no public setters)
    private int timeStart;
    private boolean am_pm;

    /**
     * Constructor for the Time Tuple
     * @param timeStart int The time the registration is for
     * @param am_pm boolean am_pm whether it is am or not (true is am, false is pm)
     */
    public TimeTuple(int timeStart, boolean am_pm)
    {
        setTimeStart(timeStart);
        setAm_pm(am_pm);
    } // End of the constructor

    /* Getters & Setters */
    public int getTimeStart() {return timeStart;}
    private void setTimeStart(int timeStart) {this.timeStart = timeStart;}
    public boolean isAm_pm() {return am_pm;}
    private void setAm_pm(boolean am_pm) {this.am_pm = am_pm;}
} // End of the time tuple class
