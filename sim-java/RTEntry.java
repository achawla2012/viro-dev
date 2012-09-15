import java.util.*;
import java.lang.*;

public class RTEntry { 
	int bucket;
	String nexthop;
	String gateway;
	String prefix;
	String d_gateway;
	long sTime;

	RTEntry(int bucket,String nexthop,String gateway,String prefix,String d_gateway){
		this.bucket = bucket;
		this.nexthop = nexthop;
		this.gateway = gateway;
		this.prefix = prefix;
		this.d_gateway = d_gateway;
		this.sTime = 0;
	}

	public void setTime(long time){
		sTime = time;
		return;
	}

	public void printRTEntry() {
		long curTime = System.currentTimeMillis();
		float elapse = (curTime - sTime)/1000F;
		System.out.println("   " + bucket + "        " + nexthop + "     " + gateway + "    " + prefix + "    " + d_gateway + "	" + elapse);
		return;
	}

	public boolean equals(Object o)
	{
		if (this == null) return false;
		final RTEntry other=(RTEntry)o;
		if (this.nexthop.equals(other.nexthop) && this.gateway.equals(other.gateway) && this.bucket == other.bucket)
			return true;
		else
			return false;
	}
}
