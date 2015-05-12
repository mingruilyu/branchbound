package jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tasks.TaskEuclideanTsp;
import tasks.TaskFibonacci;
import api.Job;
import api.Space;
import api.Task;

public class JobEuclideanTsp implements Job<List<Integer>>, Serializable{
	public static final long serialVersionUID = 227L;
	private int n;
	private double[][] cities;
	private boolean prefetchFlag;
	public JobEuclideanTsp(int n, double[][] cities) {
		this.n = n;
		this.cities = cities;
	}
	@Override
	public Task<List<Integer>> toTask(Space space) {
		List<Integer> prevCities = new ArrayList<Integer> ();
		double restDistance = 0;
		for(int i = 1; i < TaskEuclideanTsp.LEAST_COST_EDGES.length; i ++) {
			
		}
		return new TaskEuclideanTsp(0, TaskEuclideanTsp.LEAST_COST_EDGES[0][0], restDistance,
									Task.NO_PARENT, Task.NO_PARENT, n-1, prevCities, n);
	}
	@Override
	public String toString() {
		
		return "JobEuclideanTsp: " + this.n;
	}	
}
