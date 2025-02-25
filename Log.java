import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple way to log messages
 */
public class Log {

  /**
   * Our logger for our program
   */
  private static final Logger logger = Logger.getLogger("program");

  /**
   * env variable on whether or not to enable logging messages
   */
  private static final boolean ENABLED = Boolean.parseBoolean(System.getenv("logging"));

  /**
   * Logs an informational message.
   *
   * @param message The message to log.
   */
  public static void info(String message) {
    if (ENABLED)
      logger.log(Level.INFO, message);
  }

  /**
   * Logs a warning message.
   *
   * @param message The message to log.
   */
  public static void warning(String message) {
    if (ENABLED)
      logger.log(Level.WARNING, message);
  }

  /**
   * Logs an error message.
   *
   * @param message The message to log.
   */
  public static void error(String message) {
    if (ENABLED)
      logger.log(Level.SEVERE, message);
  }

  public static void printStackTrace(Exception e) {
    if (ENABLED)
      e.printStackTrace();
  }
}
