package RobotFunctions;

import Map.City;
import Map.Node;

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
        //ArrayList<Node> currentSolution = new ArrayList<>();
        Collections.shuffle(currentSolution);

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
                    // how correct is that Math.random() anyway?
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
