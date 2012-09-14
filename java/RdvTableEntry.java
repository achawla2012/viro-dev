import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class RdvTableEntry { 
	RdvEdge edge;
	long sTime;
	RdvTableEntry(RdvEdge edge){
		this.edge = edge;
	}
	public void setTime(long time){
		sTime = time;
		return;
	}

	public void printRdvEntry() {
		long curTime = System.currentTimeMillis();
		float elapse = (curTime - sTime)/1000F;
		System.out.println("	" + edge.src + "	" + edge.dst + "	" + elapse);
		return;
	}

}
