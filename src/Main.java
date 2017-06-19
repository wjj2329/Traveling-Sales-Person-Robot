import Map.City;
import Map.Coordinate;
import RobotFunctions.Decoder;
import RobotFunctions.Robot;
import RobotFunctions.RobotUtils;
import TelnetFunctions.Telnet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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

        String whereCommandForLocalRobot = telnet.sendWhere();
        String whereCommandToOtherRobot = whereCommandForLocalRobot;
        JSONObject localCommand = new JSONObject(whereCommandForLocalRobot);

        // 10 = the other robot's ID as arUco card
        localCommand.remove("10");
        whereCommandForLocalRobot = localCommand.toString();

        JSONObject otherCommand = new JSONObject(whereCommandToOtherRobot);
        JSONObject savedInfoForOtherRobot = otherCommand.getJSONObject("10");
        otherCommand.remove("robot");
        otherCommand.remove("10");
        otherCommand.put("robot", savedInfoForOtherRobot);
        whereCommandToOtherRobot = otherCommand.toString();


        Decoder.updateTerrainField(robot, whereCommandForLocalRobot);
        createMyLists(otherCommand, robot, savedInfoForOtherRobot.getJSONArray("center")); // here
        whereCommandToOtherRobot = otherCommand.toString();
        //telnet.sendStuffToOtherRobot(whereCommandToOtherRobot);
        System.out.println("How many cities? " + robot.getMyCities().size());
        robot.calculatePath();
        while(true)
        {
            String whereCommandForLocalRobot2 = telnet.sendWhere();
            String whereCommandToOtherRobot2 = whereCommandForLocalRobot2;
            JSONObject localCommand2 = new JSONObject(whereCommandForLocalRobot2);

            localCommand2.remove("10");
            whereCommandForLocalRobot2 = localCommand2.toString();

            JSONObject otherCommand2 = new JSONObject(whereCommandToOtherRobot2);
            JSONObject savedInfoForOtherRobot2 = otherCommand2.getJSONObject("10");
            otherCommand2.remove("robot");
            otherCommand2.remove("10");
            otherCommand2.put("robot", savedInfoForOtherRobot2);

            whereCommandToOtherRobot2 = otherCommand2.toString();

            Decoder.updateRobot(robot, whereCommandForLocalRobot2);
            //telnet.sendStuffToOtherRobot(whereCommandToOtherRobot2);

            robot.rotateMe(telnet);
            System.out.println("Robot's node coordinates: " +
                    RobotUtils.convertFromPixelToNode(robot.getCurrentLocation()));
            if (robot.end())
            {
                //System.out.println("Finishing...");
                //telnet.sendSpeed(0,0);
                //return;
            }
            if (robot.shouldIPause())
            {
                System.out.println("I pause");
                telnet.sendSpeed(0, 0);
                Thread.sleep(5000);
            }
            telnet.sendSpeed(2,2);
        }
    }

    private static void createMyLists(JSONObject jason, Robot rob, JSONArray otherRobotCoordinate)
    {
        Coordinate alienCoordinate = new Coordinate(otherRobotCoordinate.getDouble(0),
                otherRobotCoordinate.getDouble(1));
        alienCoordinate = RobotUtils.convertFromPixelToNode(alienCoordinate);
        ArrayList<City> cities = rob.getMyCities();
        ArrayList<City> localCities = new ArrayList<>();
        ArrayList<City> remoteCities = new ArrayList<>();
        Coordinate robotLocation = RobotUtils.convertFromPixelToNode(rob.getCurrentLocation());
        //System.out.println("Actual robot location is " + robotLocation.toString());
        //System.out.println("Converted robot location is " + RobotUtils.convertFromPixelToNode(robotLocation));
        for (int i = 0; i < cities.size(); i++)
        {
            City current = cities.get(i);
            double deltaX = Math.abs(robotLocation.getX() - current.getLocation().getX());
            double deltaY = Math.abs(robotLocation.getY() - current.getLocation().getY());
            double secondDeltaX = Math.abs(alienCoordinate.getX() - current.getLocation().getX());
            double secondDeltaY = Math.abs(alienCoordinate.getY() - current.getLocation().getY());
            double sumRobotLocal = deltaX + deltaY;
            double sumRobotRemote = secondDeltaX + secondDeltaY;
            if (sumRobotLocal < sumRobotRemote)
            {
                localCities.add(current);
            }
            else
            {
                remoteCities.add(current);
            }
        }
        rob.setMyCities(localCities);
        System.out.println("The size of each list: LocalCities " + localCities.size());
        System.out.println("RemoteCities: " + remoteCities.size());

        JSONObject copy = new JSONObject(jason.toString());
//        for (String poop : jason.keySet())
//        {
//            System.out.println("poop is " + poop);
//        }
        // JSON modification
        for (String cityID : copy.keySet())
        {
            System.out.println("CityID: " + cityID);
            if (!cityID.equals("robot") && !cityID.equals("time"))
            {
                for (int i = 0; i < localCities.size(); i++)
                {
                    City localCity = localCities.get(i);
                    System.out.println("What is the local city's ID? " + localCity.getId());
                    if (Integer.parseInt(cityID) == localCity.getId())
                    {
                        // remove from JSON
                        System.out.println("Removing with ID " + cityID);
                        jason.remove(cityID);
                        break;
                    }
                }
            }
        }
    }
}
