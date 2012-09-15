import java.io.*; 
import java.net.*;
import java.util.*;
import java.lang.*;

public class RTable { 
	private ArrayList<RTEntry> rtable;

	RTable(){
		rtable = new ArrayList<RTEntry>();
	}

	public int size(){
		return rtable.size();
	}

	public RTEntry getEntry(int i){
		return rtable.get(i);
	}


	public synchronized void addEntry(RTEntry newEntry){
		if (this.entryExist(newEntry)){
			//	System.out.println("Ji: Add Fail: RT Entry exist");
			return;
		}
		else if(numofEntryPerBucket(newEntry.bucket) == 3){
			//	System.out.println("Ji: Add Fail: RT Entry reach MAX Number");
			return;
		}

		//	System.out.println("Ji: RT Entry Added!");
		newEntry.setTime(System.currentTimeMillis());
		int rnum = rtable.size();
		for (int j = 0; j <= rnum; j++) {
			if (j == rnum) {
				rtable.add(newEntry);
				break;
			}
			else {
				if (newEntry.bucket < rtable.get(j).bucket) {
					rtable.add(j, newEntry);
					break;
				}
			}
		}
		return;
	}

	public synchronized int numofEntryPerBucket(int bucket){
		int rnum = rtable.size();
		int numofEntry = 0;
		for (int j = 0; j < rnum; j++) {
			if (rtable.get(j).bucket == bucket) {
				numofEntry++;
				if (numofEntry == 3)
					return numofEntry;
			}
		}
		return numofEntry;
	}

	public synchronized boolean entryExist(RTEntry entry){
		for (int i = 0; i < rtable.size(); i++){
			RTEntry myentry = rtable.get(i);
			if (myentry.equals(entry)){
				rtable.get(i).setTime(System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}

	public synchronized void delEntry(RTEntry entry){
		if (!this.entryExist(entry)){
			System.out.println("Ji: Delete Fail: RT Entry not exist");
			return;
		}
		System.out.println("Ji: Entry Deleted");
		rtable.remove(entry);
		return;
	}

	public synchronized void delEntry(String nexthop){
		Iterator<RTEntry> itr = rtable.iterator();
		while(itr.hasNext()){
			RTEntry myentry = itr.next();
			if (myentry.nexthop.equals(nexthop)){
				delEntry(myentry);
			}
		}
		return;
	}

	public synchronized void checkEntryExpire(){
		Iterator<RTEntry> itr = rtable.iterator();
		long curTime = System.currentTimeMillis();
		while (itr.hasNext()){
			RTEntry entry = itr.next();
			if (curTime - entry.sTime > veil_switch.RTEntryTimeout){
				System.out.print("Ji: Remove Expired RTEntry! ");
				itr.remove();
			}
		}
		return;
	}

	public synchronized RTEntry entryLookup(String myVid,int bucket){
		ArrayList<RTEntry> entries = new ArrayList<RTEntry>();
		for (int i = 0; i < rtable.size(); i++){
			RTEntry entry = rtable.get(i);
			if (entry.bucket == bucket){
				// When the switch itself is gateway, use itself
				if (entry.gateway.equals(myVid))
					return entry;
				entries.add(entry);
			}
		}
		if (entries.size() == 0)
			return null;
		Random rn = new Random();
		int random =  rn.nextInt(entries.size());
		System.out.println("Num of entry in entries: " + entries.size() + " random = " + random);
		return entries.get(random);
	}


	public synchronized void printRT(){
		System.out.println("Distance    " + "Nexthop                              " + "Gateway                             " + "Prefix                             " + "Default	" + "Time");
		for (int i = 0; i < rtable.size(); i++)
			rtable.get(i).printRTEntry();
	}
}
