package system;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.lang.Runtime;

import api.Space;
import api.Task;

public class ComputerImpl extends UnicastRemoteObject implements Computer {
	public static final long serialVersionUID = 227L; 	
	private long startTime;
	private long endTime;
	private boolean multithreadFlag;
	private int workerNo;
	public static final String MULTITHREAD_ON = "MT_ON";
	public static final String MULTITHREAD_OFF = "MT_OFF";
	public ComputerImpl(boolean multithreadFlag) throws RemoteException {
		super();
		this.multithreadFlag = multithreadFlag;		
		if(this.multithreadFlag)
			this.workerNo = Runtime.getRuntime().availableProcessors();
		else this.workerNo = 1;
    }
	
	public static void main(String[] args) {
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		
		try {
			ComputerImpl computer;
			if(args[1].equals(ComputerImpl.MULTITHREAD_ON)) {
				computer = new ComputerImpl(true);
				System.out.println("Multithread is on");
			}
			else {
				computer = new ComputerImpl(false);
				System.out.println("Multithread is off");
			}
			String url = "rmi://" + args[0] + ":" + Space.PORT + "/"
					+ Space.SERVICE_NAME;
			Space space = (Space) Naming.lookup(url);
			space.register(computer);
		} catch (Exception e) {
			System.out.println("ComputeEngine Exception");
			e.printStackTrace();
		}
		System.out.println("Computer is running");
	}
	
	@Override
	public void exit() throws RemoteException {			
		System.exit(0);
	}
	
	@Override
	public int getWorkerNo() throws RemoteException {
		return this.workerNo;
	}

	@Override
	public long executeTask(Task task, Space space) throws RemoteException, InterruptedException {
        this.startTime = System.currentTimeMillis();	
		task.run(space);
		this.endTime = System.currentTimeMillis();
		return this.endTime-this.startTime;
	}

	@Override
	public void decrementWorkerNo() throws RemoteException {
		this.workerNo --;
	}
}
