package com.example.dp4coruna.mapmanagement.indoorRouting;

import android.util.Log;
import com.example.dp4coruna.mapmanagement.indoorRouting.DataStructures.Neighbor;
import com.example.dp4coruna.mapmanagement.indoorRouting.DataStructures.Waypoint;

import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;




/* class Edge {
	int v1, v2;
	Edge(int v1, int v2) {
		this.v1 = v1; this.v2 = v2;
	}
}  */
		
public class GraphWorld {
	
	// all the members in the graph
	public Waypoint[] members;
	public GraphWorldState sstate;
	public GraphWorldState gstate;
	
	// hash map to store the (name,num) association
	HashMap<String,Integer> map;
	
	public static String srcnode="";
	public static String destnode="";
	
	// initialize graph from file
	public GraphWorld(Scanner sc, String srcnode_input, String destnode_input) {

		//set source and destination:
		if(srcnode_input != null && destnode_input != null){
			srcnode = srcnode_input;
			destnode = destnode_input;
		}

		// first line is number of people
		int n = Integer.parseInt(sc.nextLine());
		Log.i("FromGraphWorld","Num Of Lines " + n);
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
			Log.i("FromGraphWorld"," name is "+waypoint.name +" and number is "+i);
			
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
