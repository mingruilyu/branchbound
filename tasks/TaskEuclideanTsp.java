package tasks;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import system.ComputerImpl;
import api.Space;
import api.Task;

/**
 * TaskEuclideanTsp extends from the Task abstract class. This task solves a
 * Traveling Salesman Problem (TSP).
 * 
 * @author Gongxl
 *
 */
public class TaskEuclideanTsp extends Task<List<Integer>> implements
		Serializable {
	public static final long serialVersionUID = 227L;
	static final public double[][] CITIES = {
		{ 1, 1 },
		{ 8, 1 },
		{ 8, 8 },
		{ 1, 8 },
		{ 2, 2 },
		{ 7, 2 },
		{ 7, 7 },
		{ 2, 7 },
		{ 3, 3 },
		{ 6, 3 },
		{ 6, 6 },
		{ 3, 6 }
	};
	static final public double[][] DISTANCES = initializeDistances();
	static final public double[][] LEAST_COST_EDGES = initializeEdges();
	private int settledCity;
	private double restDistance;
	private double partialDistance;
	private int n;
	private List<Integer> settledCities;
	private int total;
	private double lowerbound;

	/**
	 * construct a TSP task,
	 * 
	 * @param cities
	 *            coordinates of cities
	 * @param settledCity
	 *            the one to settled in this new task
	 * @param space
	 *            to get the space
	 * @param parentId
	 *            inherited from parent
	 * @param slotIndex
	 *            set where should the result go
	 * @param n
	 *            how many cities remain unsettled
	 * @param prevCities
	 *            the cities have been settled
	 * @param total
	 *            the total of cities
	 */
	public TaskEuclideanTsp(int settledCity, double partialDistance, double restDistance, 
			int parentId, int slotIndex, int n, List<Integer> prevCities,
			int total) {
		super(parentId, slotIndex);
		this.settledCity = settledCity;
		this.slotIndex = slotIndex;
		this.n = n;
		this.total = total;
		this.settledCities = new ArrayList<Integer>(prevCities);
		this.argList = new ArrayList<List<Integer>>();
		// ========================================================
		//update the lowerbound in O(1) 
		int lastCity = this.settledCities.get(this.settledCities.size() - 1);
		this.restDistance = restDistance - LEAST_COST_EDGES[settledCity][0] 
							- LEAST_COST_EDGES[settledCity][1];
		this.partialDistance = partialDistance - LEAST_COST_EDGES[lastCity][0] 
							   + DISTANCES[lastCity][settledCity] 
							   + LEAST_COST_EDGES[settledCity][0];
		this.lowerbound = this.restDistance + this.partialDistance;
		// ========================================================
		this.settledCities.add(settledCity);
		
		if (n > 7) {
			for (int i = 0; i < n - 1; i++) {
				this.argList.add(null);
			}
		} else {
			Set<Integer> set = new HashSet<Integer>();
			list2Set(settledCities, set);
			List<ArrayList<Integer>> permutation = generatePermutation(set);
			List<Integer> minList = getMinCost(settledCities, permutation);
			this.argList.add(minList);
			this.missingArgCount = -1;
		}
	}

	/**
	 * convert the List to Set
	 * 
	 * @param a
	 *            list of cities
	 */
	private void list2Set(List<Integer> list, Set<Integer> set) {
		for (int i : list) {
			set.add(i);
		}
	}
	
	/**
	 * get the minimum cost of a list of cities first merge the list of
	 * settledCities with each permutation and find the minimum cost one
	 * 
	 * @param settledCities
	 *            the list of settled cities
	 * @param permutation
	 *            the list of permutations
	 * @return the minimum cost
	 */
	private List<Integer> getMinCost(List<Integer> settledCities,
			List<ArrayList<Integer>> permutation) {
		Double minCost = Double.MAX_VALUE;
		List<Integer> minList = new ArrayList<Integer>();
		for (List<Integer> list : permutation) {
			List<Integer> temp = new ArrayList<Integer>();
			temp.addAll(settledCities);
			temp.addAll(list);
			double cost = calculateCost(temp);
			if (cost < minCost) {
				minCost = cost;
				minList.clear();
				minList.addAll(temp);
			}
		}
		return minList;
	}

	/**
	 * get the cost of travel
	 * 
	 * @param trail
	 *            a list of cities
	 * @return the cost of travel along these city in this order
	 */
	private double calculateCost(List<Integer> trail) {
		double totalCost = 0;
		double distance;
		int lastCity = 0, curCity;
		for (int i = 0; i < trail.size(); i++) {
			curCity = trail.get(i);
			distance = DISTANCES[curCity][lastCity];
			totalCost += distance;
			lastCity = curCity;
		}
		totalCost += DISTANCES[0][lastCity];
		return totalCost;
	}

	/**
	 * generate permutations from 0 to the total number of cities, do not
	 * include these settled cities
	 * 
	 * @param set
	 *            a set of cities which have already been settled
	 * @return a list of city list
	 */
	private List<ArrayList<Integer>> generatePermutation(Set<Integer> set) {
		ArrayList<ArrayList<Integer>> base = new ArrayList<ArrayList<Integer>>();
		base.add(new ArrayList<Integer>());

		for (int i = 1; i < total; i++) {
			if (set.contains(i))
				continue;
			ArrayList<ArrayList<Integer>> newBase = new ArrayList<ArrayList<Integer>>();
			for (ArrayList<Integer> list : base) {
				for (int j = 0; j <= list.size(); j++) {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.addAll(list);
					temp.add(j, i);
					newBase.add(temp);
				}
			}
			base = newBase;
		}
		return base;
	}

	/**
	 * output the taskNO
	 */
	public String toString() {
		return "TSP taskNO " + this.settledCity;
	}

	/**
	 * collect all the argument and get the final result
	 * 
	 * @return the list with the minimum cost of travel
	 */
	private List<Integer> getResult(Space space, double upperbound) {
		List<Integer> minList = this.getArg(0);
		double minCost = calculateCost(minList);
		for (int i = 1; i < this.getArgCount(); i++) {
			List<Integer> tempList = this.getArg(i);
			double tempCost = calculateCost(tempList);
			if (tempCost < minCost) {
				minCost = tempCost;
				minList.clear();
				minList.addAll(tempList);
			}
		}
		if(minCost < upperbound)
			try {
				space.putShared(upperbound);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		return minList;
	}

	/**
	 * the run method check the number if missing argument, if it is -1, then
	 * the result of this task have been done during construction, get the
	 * result and feedback it. if it is 0, means that all the argument have been
	 * selected, then just get the result by calling the get result method, and
	 * feedback the result. if it is larger than 0, need to settle one city each
	 * time when constructing new task
	 */
	@Override
	public void run(Space space) throws RemoteException {
		double upperbound = (double) space.getShared();
		if (this.missingArgCount <= 0) {
			List<Integer> result = null;
			if (this.missingArgCount == -1)
				result = this.getArg(0);
			else result = getResult(space, upperbound);
			this.feedback(space, result);
		} else {
			int parentId = space.getTaskId();
			this.spawn(space, parentId, upperbound);
			space.suspendTask(this);
		}
	}
	
	static private double[][] initializeDistances() {
		double[][] distances = new double[CITIES.length][CITIES.length];
		for (int i = 0; i < CITIES.length; i++)
			for (int j = 0; j < i; j++) {
				distances[i][j] = distances[j][i] = distance(CITIES[i],
						CITIES[j]);
			}
		return distances;
	}

	static private double[][] initializeEdges() {
		double minEdge_1, minEdge_2;
		double[][] leastCostEdges = new double[CITIES.length][2];
		for(int i = 0; i < CITIES.length; i ++) {
			minEdge_1 = Integer.MAX_VALUE;
			minEdge_2 = Integer.MAX_VALUE;
			for(int j = 0; j < i; j ++) {
				if(DISTANCES[i][j] < minEdge_1) {
					minEdge_1 = DISTANCES[i][j];
					minEdge_2 = minEdge_1;
				} else if(DISTANCES[i][j] < minEdge_2) {
					minEdge_2 = DISTANCES[i][j];
				}
			}
			leastCostEdges[i][0] = minEdge_1;
			leastCostEdges[i][1] = minEdge_2;
		}
		return leastCostEdges;
	}
	
	private static double distance(final double[] city1, final double[] city2) {
		final double deltaX = city1[0] - city2[0];
		final double deltaY = city1[1] - city2[1];
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}

	@Override
	public void spawn(Space space, int parentId, double upperbound) throws RemoteException {
		Set<Integer> set = new HashSet<Integer>();
		list2Set(settledCities, set);
		int settledCity = 0;
		this.missingArgCount = 0;
		for (int i = 0; i < n - 1; i++) {
			while (set.contains(settledCity)) settledCity++;
			double newLowerbound = calculateLowerbound(settledCity);
			if(newLowerbound < upperbound) {
				space.issueTask(new TaskEuclideanTsp(settledCity, 
												 	this.partialDistance, 
												 	this.restDistance, 
												 	parentId, i, n - 1, 
												 	settledCities, total));
				this.missingArgCount ++;
			}
			settledCity++;
		}
	}
	
	private double calculateLowerbound(int newCity) {
		int lastCity = this.settledCities.get(this.settledCities.size() - 1);
		double newRestDistance = this.restDistance - LEAST_COST_EDGES[newCity][0]
							- LEAST_COST_EDGES[newCity][1];
		double newPartialDistance = this.partialDistance - LEAST_COST_EDGES[lastCity][0] 
							   + DISTANCES[lastCity][settledCity] 
							   + LEAST_COST_EDGES[newCity][0];
		return newRestDistance + newPartialDistance;
	}	
}
