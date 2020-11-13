package com.example.dp4coruna.mapmanagement;

import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;



class Neighbor {
	int fnum;
	Neighbor next;
	Neighbor(int fnum, Neighbor next) {
		this.fnum = fnum;
		this.next = next;
	}
}

class Waypoint {
	String name;
	Double lat;
	Double lon;
	Neighbor first;
}


/* class Edge {
	int v1, v2;
	Edge(int v1, int v2) {
		this.v1 = v1; this.v2 = v2;
	}
}  */
		
public class GraphWorld {
	
	// all the members in the graph
	Waypoint[] members;
	public GraphWorldState sstate;
	public GraphWorldState gstate;
	
	// hash map to store the (name,num) association
	HashMap<String,Integer> map;
	
	public static String srcnode="Starbucks";
	public static String destnode="ATM";
	
	// initialize graph from file
	public GraphWorld(Scanner sc) {
		// first line is number of people
		int n = Integer.parseInt(sc.nextLine());
		members = new Waypoint[n];
		map = new HashMap<String,Integer>(n*2);
		// next n lines are people's info
		for (int i=0; i < n; i++) {
			String info = sc.nextLine();
			StringTokenizer st = new StringTokenizer(info,"|");
			Waypoint waypoint = new Waypoint();
			waypoint.name = st.nextToken();
			waypoint.lat= Double.parseDouble(st.nextToken()); 
			waypoint.lon = Double.parseDouble(st.nextToken());
			
			waypoint.first = null;
			// add to members
			members[i] = waypoint;
			// add to hash map
			
			System.out.println(" name is "+waypoint.name +" and number is "+i);
			
			map.put(waypoint.name,i);
		}
		// rest are friendships
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			StringTokenizer st = new StringTokenizer(line,"|");
			String p1 = st.nextToken();
			String p2 = st.nextToken();
			int i = map.get(p1);
			int j = map.get(p2);
			members[i].first = new Neighbor(j,members[i].first);
			members[j].first = new Neighbor(i,members[j].first);
		}
		sstate = new GraphWorldState(this,srcnode, 0, null);
		gstate = new GraphWorldState(this, destnode, 9999, null);
	}
}
