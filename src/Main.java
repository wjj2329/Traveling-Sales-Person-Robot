import RobotUtils.Robot;
import TelnetUtils.Telnet;

import java.io.IOException;

/**
 * Created by williamjones on 6/14/17.
 * Main class for final project.
 */
public class Main
{
    /**
     * Main method. Creates telnet and robot, and
     *  functions as the primary control of movement.
     * @param args TBA
     */
    public static void main (String[] args) throws IOException
    {
        System.out.println("Initializing program and robot...");
        Telnet telnet = new Telnet();
        Robot robot = new Robot();
    }
}
