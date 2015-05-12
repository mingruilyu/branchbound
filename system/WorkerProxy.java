package system;

import java.rmi.RemoteException;

import space.SpaceImpl;
import api.Task;

public class WorkerProxy extends Thread {
	private boolean running;
	private SpaceImpl space;
	private Computer computer;
	private int computerProxyId;
	public WorkerProxy(SpaceImpl space, Computer computer, int computerProxyId) {
		this.computer = computer;
		this.space = space;
		this.computerProxyId = computerProxyId;
	}
	
	@Override
	public void run() {
		super.run();
		while(this.running) {
			Task task = null;
			try {
				task = this.space.fetchTask();
				this.computer.executeTask(task, this.space);
			} catch (RemoteException | InterruptedException e) {
				e.printStackTrace();
				try {
					this.space.taskQueue.put(task);
					this.computer.decrementWorkerNo();
					if(this.computer.getWorkerNo() == 0)
						this.space.deleteComputerProxy(this.computerProxyId);
					break;
				} catch (InterruptedException | RemoteException e1) {
					e1.printStackTrace();
				}
				this.running = false;
			}
		}
	}
}
