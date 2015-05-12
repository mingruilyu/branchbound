package system;

import java.rmi.Remote;
import java.rmi.RemoteException;

import api.Space;
import api.Task;

public interface Computer extends Remote {
	long executeTask(Task task, Space space) throws RemoteException, InterruptedException;
	void exit() throws RemoteException;
	int getWorkerNo() throws RemoteException;
	void decrementWorkerNo() throws RemoteException;
}
