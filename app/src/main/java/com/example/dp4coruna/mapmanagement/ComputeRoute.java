package com.example.dp4coruna.mapmanagement;

import java.util.Vector;
import java.util.LinkedList;
//import java.util.PriorityQueue;
import java.util.ListIterator;
import java.util.ListIterator;
import java.util.Iterator;
import java.io.*;
import org.apache.commons.collections.buffer.*;


public class ComputeRoute {

	public GraphWorld world;
	// public PriorityQueue<GraphWorldState> open;
	public PriorityBuffer open;
	public Vector<GraphWorldState> closed;
	
	
	
	public ComputeRoute(GraphWorld gw) {
		// TODO Auto-generated constructor stub
		this.world = gw;
	//	this.open = new PriorityQueue<GraphWorldState>();
		this.open = new PriorityBuffer();
		this.closed = new Vector<GraphWorldState>();
	}

	public LinkedList<GraphWorldState> reconstruct_path(GraphWorldState goal)
	  {
	    LinkedList<GraphWorldState> path = new LinkedList<GraphWorldState>();
	    GraphWorldState state = goal;

	    path.push(state);
	    
	    	while ( !(state = state.parent).equals(world.sstate)) {
	  	      path.push(state);
	  	    }
	    
	  
	 
	    
	    path.push(state);

	    
	    LinkedList<GraphWorldState> unblocked_path = new LinkedList<GraphWorldState>(); 
	    
	    
	    ListIterator<GraphWorldState> iter;
	    
	    	iter = path.listIterator(0);
	    	while (iter.hasNext()) {
	    		
	  	      GraphWorldState next_state = iter.next();
	  	     
	  	        unblocked_path.add(next_state);
	  	        
	  	      
	  	    }
	    

	    return unblocked_path;
	  }
	
	
	 public boolean is_state_in_closed(GraphWorldState state)
	  {
	    for (int i = 0; i < closed.size(); i++) {
	      if (closed.elementAt(i).equals(state)) {
	        return true;
	      }
	    }
	    return false;
	  }

	 
	  public GraphWorldState find_state_in_open(GraphWorldState state)
	  {
	    Iterator<GraphWorldState> iter = open.iterator();
	    while (iter.hasNext()) {
	      GraphWorldState next_state = iter.next();
	      if (state.equals(next_state)) {
	        return next_state;
	      }
	    }
	    
	    return null;
	  }
	
	public LinkedList<GraphWorldState> compute_path()
	  {
	   // this.open = new PriorityQueue<GraphWorldState>();
		
		this.open = new PriorityBuffer();
	    this.closed = new Vector<GraphWorldState>();
	    //reverse
	  
	       open.add(this.world.sstate);
	    
	   
        System.out.println("Starting in comput path");
	    while (open.size() > 0) {

	      GraphWorldState state = (GraphWorldState) open.remove();
	      
	      System.out.println("State in compute path is " + state);

	      // no state in open has a lower f-value, else it would have been expanded
	      //reverse
	   //   if (state.equals(world.gstate)) {
	      
	      
	    	  if (state.equals(world.gstate)) {
	  	        return reconstruct_path(state);
	  	      }
	      
	      closed.add(state);
	    //  System.out.println("Before neighborstates ");
	      
	      Vector<GraphWorldState> gw_states = state.neighborstates();
	      for (int i = 0; i < gw_states.size(); i++) {
	        GraphWorldState successor_state = gw_states.elementAt(i);
	    //    System.out.println("Neighborstate " + successor_state);
	  
	        if (is_state_in_closed(successor_state)) {
	          continue;
	        }

	        GraphWorldState old_open_state = find_state_in_open(successor_state);
	   
	            if (old_open_state != null) {
	     	          if (old_open_state.g() > successor_state.g()) {
	     	            open.remove(old_open_state);
	     	    
	     	            open.add(successor_state);
	     	          
	     	          }
	     	        }
	     	        else {
	     	          open.add(successor_state);
	     	        }
	      } //for
	      
	    }  // while
	    return null;
	  }
	

}
