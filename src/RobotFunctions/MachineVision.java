package RobotFunctions;

import Map.City;
import Map.Coordinate;
import Map.Node;
import Map.TerrainMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Alex on 6/14/17.
 * Class designed to encapsulate the machine vision,
 *  from the camera to the code.
 */
public class MachineVision
{

    /**
     * Wrapper function for the simulated annealing TSP
     * Use this one. The other one is only necessary as public for the Junit test.
     * ROB THE ROBOT! 🤖
     */
    public ArrayList<Node> computeBasicPath(ArrayList<City> allArUcoCards, TerrainMap mrMap, Robot rob)
    {
        ArrayList<City> cities = computeTSP(allArUcoCards);
        ArrayList<Node> result = new ArrayList<>();

        // Convert robot's current location to node coordinates
        rob.setCurrentLocation(RobotUtils.convertFromPixelToNode(rob.getCurrentLocation()));

        // Boop!
        Node robotCity = mrMap.getMyMap()[(int) Math.round(rob.getCurrentLocation().getX())]
                [(int) Math.round(rob.getCurrentLocation().getY())];
        result.add(robotCity);
        // Convert pixel coordinates to world
        for (int p = 0; p < mrMap.getMyMap().length; p++)
        {
            for (int q = 0; q < mrMap.getMyMap()[p].length; q++)
            {
                // just grabbing a handy dandy reference to that
                Coordinate pixelLocation = mrMap.getMyMap()[p][q].getLocation();
                pixelLocation = RobotUtils.convertFromPixelToNode(pixelLocation);
                // might be a bit redundant with pointers, but oh well
                mrMap.getMyMap()[p][q].setLocation(pixelLocation);
            }
        }

        // Skip the last one, since we will be doing current to current + 1
        for (int i = 0; i < cities.size() - 1; i++)
        {
            City city1 = cities.get(i);
            City city2 = cities.get(i + 1);
            // Because we already know that we're going from city1 to city2, and so on
            // location should be castable to an integer value
            int xDiff = (int) Math.round(city1.getLocation().getX() - city2.getLocation().getX());
            int yDiff = (int) Math.round(city1.getLocation().getY() - city2.getLocation().getY());
            Node loc = mrMap.getMyMap()[(int) Math.round(city1.getLocation().getX())]
                    [(int) Math.round(city1.getLocation().getY())];
            // This is so sketchy
            if (result.contains(loc))
            {
                continue;
            }
            result.add(loc);

            // Even though we aren't checking for out of bounds, we shouldn't need to check
            // If it goes OOB, we jacked something up.
            for (int a = 1; a <= xDiff; a++)
            {
                Node res = mrMap.getMyMap()[(int) Math.round(city1.getLocation().getX()) + a]
                        [(int) Math.round(city1.getLocation().getY())];
                result.add(res);
            }
            for (int b = 1; b <= yDiff; b++)
            {
                Node res = mrMap.getMyMap()[(int) Math.round(city1.getLocation().getX())]
                        [(int) Math.round(city1.getLocation().getY()) + b];
                result.add(res);
            }

            // Is this cat necessary? Or is it already added by definition?
            Node loc2 = mrMap.getMyMap()[(int) Math.round(city2.getLocation().getX())]
                    [(int) Math.round(city2.getLocation().getY())];
            if (!result.contains(loc2))
            {
                result.add(loc2);
            }
        }
        calculateDegrees(result);
        return result;
    }

    /**
     * In which we calculate the degrees between each node in the path
     */
    private void calculateDegrees(ArrayList<Node> path)
    {
        for (int i = 0; i < path.size(); i++)
        {
            if(i < path.size() - 1)
            {
                path.get(i).setDegree(RobotUtils.calculateAngle(path.get(i), path.get(i + 1)));
            }
            else
            {
                path.get(i).setDegree(path.get(i - 1).getDegree());
            }
        }
    }

    /**
     * Ascertains if the tile we are checking is out of bounds.
     * Don't use this rn.
     */
    private boolean outOfBounds(int xFactor, int yFactor)
    {
        return xFactor < 0 || xFactor >= RobotUtils.gridDimensionX
                || yFactor < 0 || yFactor >= RobotUtils.gridDimensionY;
    }

    /**
     * Should be better than randomly shuffling.
     *  That way, our simulated annealing approach
     *  at least beats greedy.
     */
    public ArrayList<City> greedySolution(ArrayList<City> initialList)
    {
        City owlCity = initialList.get(0);
        // The solution. Add the start city
        ArrayList<City> soul = new ArrayList<>();
        soul.add(owlCity);

        for (int theCrystals = 0; theCrystals < initialList.size(); theCrystals++)
        {
            int closest = Integer.MAX_VALUE;
            City city1 = initialList.get(theCrystals);
            City closestCity = null; // closest city to city1
            for (City aCity : initialList)
            {
                City city2 = aCity;
                double distanceBetween = RobotUtils.distance(city1.getLocation(), city2.getLocation());
                if (!city1.equals(city2) && !soul.contains(city2) && distanceBetween < closest)
                {
                    closestCity = city2;
                }
            }
            if (closestCity != null)
            {
                soul.add(closestCity);
            }
        }
        return soul;
    }

    /**
     * The two robots will divide up the grid in half.
     * Each will use a simulated annealing approach
     *  to compute a path to each of the "cities",
     *  implemented as arUco cards.
     *
     *  currentSolution = all the cities, aka arUco cards
     * @return path
     */
    public ArrayList<City> computeTSP(ArrayList<City> currentSolution)
    {
        //Collections.shuffle(currentSolution);
        currentSolution = greedySolution(currentSolution);

        double temperature = 1.0;
        double min = 0.0001;
        double alpha = 0.9;
        while (temperature > min)
        {
            for (int i = 0; i < 100; i++)
            {
                double currentCost = calculateCost(currentSolution);
                ArrayList<City> neighbor = calculateNeighbor(currentSolution);
                double neighborCost = calculateCost(neighbor);
                if (neighborCost < currentCost
                        || acceptanceProbability(currentCost, neighborCost, temperature) > Math.random())
                {
                    currentSolution = neighbor;
                }
            }
            temperature *= alpha;
        }
        return currentSolution;
    }

    /**
     * Returns cost of path, based on distance.
     */
    private double calculateCost(ArrayList<City> path)
    {
        double sol = 0.0;
        for (int i = 0; i < path.size(); i++)
        {
            if (i < path.size() - 1)
            {
                sol += RobotUtils.distance(path.get(i).getLocation(), path.get(i + 1).getLocation());
            }
        }
        return sol;
    }

    /**
     * Switches two nodes in the current solution path
     */
    private ArrayList<City> calculateNeighbor(ArrayList<City> current)
    {
        Random random = new Random();
        int position = random.nextInt(current.size());
        int position2 = random.nextInt(current.size());
        Collections.swap(current, position, position2);
        return current;
    }

    /**
     * Returns the acceptance probability
     */
    private double acceptanceProbability(double costCurrent, double costNeighbor, double temperature)
    {
        return Math.pow(Math.E, ((costCurrent - costNeighbor) / temperature));
    }

    /**
     * Similar to the computeTSP() method,
     *  only the robots will move in pretty arcs,
     *  like two graceful trains.  ☆ﾟ°˖* ᕕ( ᐛ )ᕗ
     *
     * But we do need a practical application for my good ol' homedog Dr. Seppi.
     *  Working on that.
     *  DONUTS!
     * @return path
     */
    public ArrayList<City> computePrettyTSP()
    {
        return null;
    }
}
