package nyu.yao.automator;
/**
 * Class to contain all program settings, loggers, and user pools
 * @author Jason Yao
 */
public class Setup
{
    // Pseudo-immutable object attributes (no public setters)
    private Settings settings;
    private Logger logger;
    private UserPool userPool;

    /**
     * Constructor for the Setup class
     * @param settings Settings containing reservation locations, dates, and login file information
     * @param logger Logger containing streams to the newly created streams
     * @param userPool UserPool containing a filled pool of created users
     */
    public Setup(Settings settings, Logger logger, UserPool userPool)
    {
        setSettings(settings);
        setLogger(logger);
        setUserPool(userPool);
    } // End of the setup constructor

    /* Getters & Setters */
    public Settings getSettings() {return settings;}
    private void setSettings(Settings settings) {this.settings = settings;}
    public Logger getLogger() {return logger;}
    private void setLogger(Logger logger) {this.logger = logger;}
    public UserPool getUserPool() {return userPool;}
    private void setUserPool(UserPool userPool) {this.userPool = userPool;}
} // End of the setup class
