package system;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import space.SpaceImpl;
import api.Space;
import api.Task;

public class ComputerProxy extends Thread {
	final private Computer computer;
	final private SpaceImpl space;
	final public int id;
	public ComputerProxy(SpaceImpl space, Computer computer, int id) {
		this.computer = computer;
		this.space = space;
		this.id = id;
	}
	
	public void startWorker() {
		try {
			int workerNo = this.computer.getWorkerNo();
			for(int i = 0; i < workerNo; i ++)
				new WorkerProxy(this.space, this.computer, this.id).start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
