import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class RdvEdge{ 
	String src;
	String dst;
	RdvEdge(String s,String d){
		src = new String(s);
		dst = new String(d);
	}
	public void printEdge(){
		System.out.println("src = " + src + "  dst = " + dst);
	}
	public boolean equals(Object o)
	{
		if (this == null) return false;
		final RdvEdge other=(RdvEdge)o;
		if ((this.src.equals(other.src)&&this.dst.equals(other.dst))||(this.src.equals(other.dst)&&this.dst.equals(other.src)))
			return true;
		else
			return false;
	}
}
