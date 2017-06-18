import RobotFunctions.Decoder;
import RobotFunctions.Robot;
import RobotFunctions.RobotUtils;
import TelnetFunctions.Telnet;

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
    public static void main (String[] args) throws IOException, InterruptedException
    {
        System.out.println("Initializing program and robot...");
        Telnet telnet = new Telnet();
        Robot robot = new Robot();
        Decoder.updateTerrainField(robot, telnet.sendWhere());
        robot.calculatePath();
        while(true)
        {
            Decoder.updateRobot(robot,telnet.sendWhere());
            robot.rotateMe(telnet);
            System.out.println("Robot's node coordinates: " +
                    RobotUtils.convertFromPixelToNode(robot.getCurrentLocation()));
            if (robot.end())
            {
                telnet.sendSpeed(0, 0);
                return;
            }
            //System.out.println("shouldIPause() = " + robot.shouldIPause());
            if (robot.shouldIPause())
            {
                System.out.println("I pause");
                telnet.sendSpeed(0, 0);
                Thread.sleep(10000);
            }
            telnet.sendSpeed(2,2);
        }
    }
}
