package nyu.yao.automator;
import java.io.PrintStream;

/**
 * Class to contain all logging streams
 */
public class Logger
{
    // Pseudo-immutable object attributes (no public setters)
    private PrintStream stdOutStream;
    private PrintStream stdErrStream;

    /**
     * Constructor for the Logger class
     * @param stdOutStream PrintStream where the std.out is being redirected to
     * @param stdErrStream PrintStream where the std.err is being redirected to
     */
    public Logger(PrintStream stdOutStream, PrintStream stdErrStream)
    {
        setStdOutStream(stdOutStream);
        setStdErrStream(stdErrStream);
    } // End of the constructor

    /* Getters & Setters */
    public PrintStream getStdOutStream() {return stdOutStream;}
    private void setStdOutStream(PrintStream stdOutStream) {this.stdOutStream = stdOutStream;}
    public PrintStream getStdErrStream() {return stdErrStream;}
    private void setStdErrStream(PrintStream stdErrStream) {this.stdErrStream = stdErrStream;}
} // End of the logger class
