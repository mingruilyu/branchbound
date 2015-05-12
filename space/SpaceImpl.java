package space;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import system.Computer;
import system.ComputerImpl;
import system.ComputerProxy;
import api.Job;
import api.Space;
import api.Task;

public class SpaceImpl extends UnicastRemoteObject implements Space {
	public BlockingQueue<Task> taskQueue;
	private Map<Integer, Task> waitingQueue;
	int taskCount = 0;
	private BlockingQueue resultQueue;
	private Map<Integer, ComputerProxy> computerList;
	private final static String RUNNABLE_ON = "SR_ON";
	private final static String RUNNABLE_OFF = "SR_OFF";
	private Object shared;
	public SpaceImpl() throws RemoteException {
		this.taskQueue = new LinkedBlockingQueue<Task>();
		this.computerList = Collections.synchronizedMap(new HashMap<Integer, ComputerProxy>());
		this.waitingQueue = Collections.synchronizedMap(new HashMap<Integer, Task>());
		this.resultQueue = new LinkedBlockingQueue<Task>();
	}

	public void deleteComputerProxy(int proxyId) {
		computerList.remove(proxyId);
	}
	
	@Override
	public void register(Computer computer) throws RemoteException {
		int id = computerList.size();
		ComputerProxy computerProxy = new ComputerProxy(this, computer, id);
		this.computerList.put(id, computerProxy);
		computerProxy.startWorker();
	}

	@Override
	public <T> Task<T> fetchTask() throws RemoteException, InterruptedException {
		return this.taskQueue.take();
	}

	public static void main(String[] args) throws RemoteException,
			NotBoundException {
		Space space = null;
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try {
			space = new SpaceImpl();
			Registry registry = LocateRegistry.createRegistry(Space.PORT);
			registry.rebind(Space.SERVICE_NAME, space);
			System.out.println("Space in on, waiting for connection ...");
			SpaceWorker spaceWorker = new SpaceWorker(space);
			if(args[0].equals(SpaceImpl.RUNNABLE_ON)) {
				spaceWorker.start();
				System.out.println("Space Runnable is on");
            } else System.out.println("Space Runnable is off");
		} catch (Exception e) {
			System.out.println("Space Exception");
			e.printStackTrace();
		}
	}

	@Override
	public <T> void issueTask(Task<T> task) throws RemoteException {
		try {
			this.taskQueue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getTaskId() throws RemoteException {
		return this.taskCount ++;
	}
	
	@Override
	public <T> int suspendTask(Task<T> task)
			throws RemoteException {
		synchronized (waitingQueue) {
			int id = this.waitingQueue.size();
			this.waitingQueue.put(id, task);
			return id;
		}
	}

	@Override
	public <T> void insertArg(T arg, int id, int slotIndex)
			throws RemoteException {
		Task task = null;
		synchronized (waitingQueue) {
			task = this.waitingQueue.get(id);
			task.insertArg(arg, slotIndex);
		}
		if (task.isReady())
			this.issueTask(task);

	}

	@Override
	public <T> T take() throws RemoteException, InterruptedException {
		return (T) resultQueue.take();
	}

	@Override
	public <T> void setupResult(T result) throws RemoteException,
			InterruptedException {
		this.resultQueue.put(result);
	}

	@Override
	public <T> void startJob(Job<T> job) throws RemoteException,
			InterruptedException {
		this.taskQueue.put(job.toTask(this));
	}

	@Override
	public Object getShared() throws RemoteException {
		return shared;
	}

	@Override
	public void putShared(Object shared) throws RemoteException {
		this.shared = shared;
	}
}
