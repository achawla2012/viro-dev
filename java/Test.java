public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Get current time
		long start = System.currentTimeMillis();
		try{
		Thread.sleep(10000);
		}catch (Exception e) {
		System.out.println("Sleep fail ");
		}
			
		// Get elapsed time in milliseconds
		long elapsedTimeMillis = System.currentTimeMillis()-start;

		// Get elapsed time in seconds
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("elapsedTimeSec = " + elapsedTimeSec);


//		byte b = (byte) Integer.parseInt(args[0]);
//		String str = Integer.toString( ( b & 0xff ) + 0x100, 16 ).substring( 1 );
//		System.out.println("OUTPUT =" + str);
//		String a = new String("aaa");
//		String b = new String("bbb");
//		String c = new String("ccc");
//		String d = new String("ddd");
//		String e = new String("eee");
//		String f = new String("fff");
//		String g = new String("ggg");
//		String h = new String("hhh");
//		RdvEdge ab = new RdvEdge(a, b);
//		RdvEdge ba = new RdvEdge(b, a);
//		RdvEdge cd = new RdvEdge(c, d);
//		RdvEdge dc = new RdvEdge(d, c);
//		RdvEdge ef = new RdvEdge(e, f);
//		RdvEdge fe = new RdvEdge(f, e);
//		RdvEdge gh = new RdvEdge(g, h);
//		RdvEdge hg = new RdvEdge(h, g);
//		RdvTableEntry e1 = new RdvTableEntry(ab);
//		RdvTableEntry e2 = new RdvTableEntry(cd);
//		RdvTableEntry e3 = new RdvTableEntry(ef);
//		RdvTableEntry e4 = new RdvTableEntry(gh);
//		RdvTableEntry e5 = new RdvTableEntry(ba);
//		RdvTableEntry e6 = new RdvTableEntry(dc);
//		RdvTableEntry e7 = new RdvTableEntry(fe);
//		RdvTableEntry e8 = new RdvTableEntry(hg);
//		RdvTable rt = new RdvTable();
//		rt.addEntry(e1);
//		rt.addEntry(e2);
//		rt.addEntry(e3);
//		rt.addEntry(e4);
//		rt.addEntry(e5);
//		rt.addEntry(e6);
//		rt.addEntry(e7);
//		rt.addEntry(e8);
//		rt.printRdvTable();
	}

}

