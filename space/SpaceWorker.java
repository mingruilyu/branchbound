package space;

import java.rmi.RemoteException;
import api.Space;

public class SpaceWorker extends Thread{
	private Space space;

	public SpaceWorker(Space space) {
		this.space = space;
	}
	@Override
	public void run() {
		super.run();
		int count = 0;
		while(true){
			try {
				this.space.fetchTask().run(this.space);
				count++;
			} catch (RemoteException | InterruptedException e) {
				e.printStackTrace();
				System.out.println("Space Worker Exception");
			}	
		}
	}
}
