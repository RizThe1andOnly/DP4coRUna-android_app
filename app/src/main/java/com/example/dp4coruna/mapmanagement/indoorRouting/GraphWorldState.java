package com.example.dp4coruna.mapmanagement.indoorRouting;

import com.example.dp4coruna.mapmanagement.indoorRouting.DataStructures.Neighbor;

import java.util.Vector;




public class GraphWorldState implements Comparable<GraphWorldState> {
	
	public GraphWorld gworld;
	public String nodename;
	public double gval;
	public double hval;
	public int sval;
	GraphWorldState parent;
	

	public GraphWorldState(GraphWorld gw, String name, double g, GraphWorldState from)  {
		// TODO Auto-generated constructor stub
		this.gworld=gw;
		this.nodename=name;
		this.gval=g;
		
		int i = gw.map.get(GraphWorld.destnode);
		int j = gw.map.get(name);
		Double lat2=gw.members[i].lat;
		Double lat1 = gw.members[j].lat;
		Double lon2=gw.members[i].lon;
		Double lon1 = gw.members[j].lon;
		
		
	 
		lon1 = Math.toRadians(lon1); 
        lon2 = Math.toRadians(lon2); 
        lat1 = Math.toRadians(lat1); 
        lat2 = Math.toRadians(lat2); 
  
        // Haversine formula  
        double dlon = lon2 - lon1;  
        double dlat = lat2 - lat1; 
        
        double a = Math.pow(Math.sin(dlat / 2), 2);                
        double c = 2 * Math.asin(Math.sqrt(a)); 
  
        // Radius of earth in kilometers. Use 3956  
        // for miles 
        double r = 6371; 
        double lat_d=(c * r); 

        // do the same for lon
         a = Math.pow(Math.sin(dlon / 2), 2);                
         c = 2 * Math.asin(Math.sqrt(a)); 
         double lon_d=(c * r);
	    // add the two 
	    this.hval= lat_d + lon_d;
		
		
		this.sval=0;
		this.parent=from;
	}
	
	public String toString() {
		return "(" + nodename + " h =  " + hval + " g = " + gval +  ")";
	}

	public double g() {
		return gval;
	}
	public double h() {
		// return Math.abs(gworld.gstate.row - row) + Math.abs(gworld.gstate.col - col);

		if(nodename.equals("XXIForever") || (nodename.equals("Apple")) || (nodename.equals("WillowbrookMall-Bloomingdale"))){
			return this.hval+1000;

		}
		return hval;
	}
	public double f() {
		return g() + h();
	}
	
	public int compareTo(GraphWorldState other) {
		
		double f_diff=this.f() - other.f();
		
		if (f_diff == 0) {
			
			
				
				double g_diff=this.g()-other.g();
				if (g_diff != 0)  return (int) g_diff;
				return (int) (this.h() - other.h());
			
		
		}
		return (int) f_diff;
	}
	
	public boolean equals(GraphWorldState other) {
		return nodename.equalsIgnoreCase(other.nodename);
	}


	public Vector<GraphWorldState> neighborstates() {
		
		Vector<GraphWorldState> states = new Vector<GraphWorldState>();
		GraphWorldState gwstate;
		int v = gworld.map.get(nodename);
		
		Double lat1=gworld.members[v].lat;
		Double lon1 = gworld.members[v].lon;
		
		for(Neighbor ptr = gworld.members[v].first; ptr!=null; ptr=ptr.next) {
			
		//	System.out.println("Inside neighborstate i is " + i);
			 lat1=gworld.members[v].lat;
			 lon1 = gworld.members[v].lon;
			
			Double lat2=gworld.members[ptr.fnum].lat;
			Double lon2 = gworld.members[ptr.fnum].lon;
			
			lon1 = Math.toRadians(lon1); 
	        lon2 = Math.toRadians(lon2); 
	        lat1 = Math.toRadians(lat1); 
	        lat2 = Math.toRadians(lat2); 
	  
	        // Haversine formula  
	        double dlon = lon2 - lon1;  
	        double dlat = lat2 - lat1; 
	        double a = Math.pow(Math.sin(dlat / 2), 2) 
	                 + Math.cos(lat1) * Math.cos(lat2) 
	                 * Math.pow(Math.sin(dlon / 2),2); 
	              
	        double c = 2 * Math.asin(Math.sqrt(a)); 
	  
	        // Radius of earth in kilometers. Use 3956  
	        // for miles 
	        double r = 6371; 
	   
	        double dist = (c * r); 
		//	System.out.println("In Neighbors " + gworld.members[ptr.fnum].name + " g = " + gval + " dist = " + dist);
			 gwstate = new GraphWorldState(gworld, gworld.members[ptr.fnum].name, gval+dist, this);
		//	System.out.println("Checking validity of state " + gwstate);
			  
		states.add(gwstate);
			    
		} // for
		return states;
	}
}

