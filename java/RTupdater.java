import java.util.Timer;
import java.util.TimerTask;

public class RTupdater implements Runnable {

	public void run(){


		// ***********************************************
		// INITIALIZE Timer for Route Table Update
		// ***********************************************
		int initialDelay = 30000; // start after 30 seconds
		int period = 10000;        // repeat every 10 seconds
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
//				veil_switch.printRT();
				clientHandler.updateRdvTable();
				clientHandler.printRdvTable();
			}
		};
		timer.scheduleAtFixedRate(task, initialDelay, period);


	}

}
