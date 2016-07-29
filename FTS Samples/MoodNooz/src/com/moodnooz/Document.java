package com.moodnooz;

public class Document {
	
	protected String link, title, source, date, description, body, simplifiedSource;
	public static final String IRISHTIMES = "The Irish Times";
	public static final String GUARDIAN = "Guardian";
	public static final String BBC = "BBC";
	public static final String UNKNOWN = "Others";
	
	public Document(String l, String t, String s, String d, String des, String b) {
		link = l;
		title = t;
		description = des;
		body = b;
		simplifiedSource = s;
		if(s.equals("a"))
			source = IRISHTIMES;
		else if(s.equals("b"))
			source = GUARDIAN;
		else if(s.equals("c"))
			source = BBC;
		else
			source = UNKNOWN;
		
		// d is in format "Fri May 03 05:00:00 UTC 2013"
		// want to convert to "Fri, May 03 2013"
		String[] components = d.split(" ");
		date = components[0] + ", " + components[1] + " " + components[2] + " " + components[5];
	}
}
