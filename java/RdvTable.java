import java.io.*; 
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.Timer;
import java.util.TimerTask;

public class RdvTable { 
	ArrayList<RdvTableEntry> rdvtable;
	RdvTable(){
		rdvtable = new ArrayList<RdvTableEntry>();
	}
	public synchronized boolean addEntry(RdvTableEntry rdvEntry){
		if (this.edgeExist(rdvEntry.edge))
			return false;
		rdvEntry.setTime(System.currentTimeMillis());
		rdvtable.add(rdvEntry);
		return true;
	}

	public synchronized boolean addEdge(RdvEdge edge){
		if (this.edgeExist(edge))
			return false;
		RdvTableEntry rdvEntry = new RdvTableEntry(edge);
		rdvEntry.setTime(System.currentTimeMillis());
		rdvtable.add(rdvEntry);
		return true;
	}

	public synchronized boolean delEdge(RdvEdge edge){
		for (int i = 0; i < rdvtable.size(); i++){
			RdvTableEntry rdvEntry = rdvtable.get(i);
			if (rdvEntry.edge.equals(edge)){
				rdvtable.remove(rdvEntry);
				System.out.print("Ji: Rdv Edge deleted!!");
				return true;
			}
		}
		return false;
	}

	public boolean edgeExist(RdvEdge edge){
		for (int i = 0; i < rdvtable.size(); i++){
			RdvEdge myedge = rdvtable.get(i).edge;
			if (myedge.equals(edge)){
				rdvtable.get(i).setTime(System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}
	public boolean edgeExist(String src,String dst){
		RdvEdge edge = new RdvEdge(src,dst);
		return edgeExist(edge);
	}

	public synchronized boolean delEntry(RdvTableEntry rdvEntry){
		return rdvtable.remove(rdvEntry);
	}

	public synchronized void updateEntry(){
		Iterator itr = rdvtable.iterator();
		long	curTime = System.currentTimeMillis();
		while (itr.hasNext()){
			RdvTableEntry entry = (RdvTableEntry) itr.next();
			if ((curTime - entry.sTime) > veil_switch.RdvEntryTimeout){
				System.out.print("Remove RdvEntry! Edge:");
				entry.edge.printEdge();
				itr.remove();
			}
		}
		return;
	}

	public synchronized String findEntry(String svid, String k_str){
		StringBuffer gwList = new StringBuffer("");
		int k = Integer.parseInt(k_str);
		String host =new String();
		host = Integer.toBinaryString(Integer.parseInt(svid, 16));
		host = veil_switch.leftPadString(host, veil_switch.K, '0');
		String list = new String("");
		for (int i = 0; i < rdvtable.size(); i++){
			RdvEdge myedge = rdvtable.get(i).edge;
			String src = Integer.toBinaryString(Integer.parseInt( myedge.src, 16));
			String dst = Integer.toBinaryString(Integer.parseInt( myedge.dst, 16));
			src = veil_switch.leftPadString(src, veil_switch.K, '0');
			dst = veil_switch.leftPadString(dst, veil_switch.K, '0');

			if ((veil_switch.vidDist(host,src) <= (k-1))&&(veil_switch.vidDist(host,dst) == k)){
				gwList.append(src);
				if (!veil_switch.multipleRoute){
					list = gwList.toString();
					return list;
				}


			}
			else if ((veil_switch.vidDist(host,dst) <= (k-1))&&(veil_switch.vidDist(host,src) == k)){
				gwList.append(dst);
				if (!veil_switch.multipleRoute){
					list = gwList.toString();
					return list;
				}
			}
			if (gwList.length() == 24) {
				list = gwList.toString();
				return list;
			}

		}
		list = gwList.toString();
		if (list.equals(""))
			return list;

		list = veil_switch.rightPadString(list, 96, '1');
		return list;
	}

	public void printRdvTable(){
		System.out.println("");
		System.out.println("************************************************    RDV Table	*************************************************");
		System.out.println("	" + "Source	" + "	Destination	" + "Time");
		for (int i = 0; i < rdvtable.size(); i++)
			rdvtable.get(i).printRdvEntry();
	}
}
