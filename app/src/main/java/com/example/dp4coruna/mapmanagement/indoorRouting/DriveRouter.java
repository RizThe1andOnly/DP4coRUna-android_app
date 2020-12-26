package com.example.dp4coruna.mapmanagement.indoorRouting;

import android.util.Log;
import com.example.dp4coruna.mapmanagement.indoorRouting.ComputeRoute;
import com.example.dp4coruna.mapmanagement.indoorRouting.GraphWorld;
import com.example.dp4coruna.mapmanagement.indoorRouting.GraphWorldState;
import com.example.dp4coruna.mapmanagement.indoorRouting.DataStructures.Neighbor;

import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.FileNotFoundException;
import java.util.Scanner;





public class DriveRouter {

	public DriveRouter() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		 File file = new File("C:\\Users\\Akshay\\Desktop\\LocationProj_11_3_20\\DP4coRUna-android_app-master\\app\\src\\main\\java\\com\\example\\dp4coruna\\mapmanagement");
		//File file = new File("router\\Nodes.txt");
		
		Scanner scanner=new Scanner(file);
		
		GraphWorld graph = new GraphWorld(scanner,null,null);

		for(int x=0;x<graph.members.length;x++) {
			
			System.out.println("Member is "+graph.members[x].name);
			
			for(Neighbor ptr = graph.members[x].first; ptr!=null; ptr=ptr.next) {
				
				
				System.out.println("Neighbor of member "+ graph.members[x].name +" is " + graph.members[ptr.fnum].name);
				
			}
		}
		
		 ComputeRoute route = new ComputeRoute(graph);
		    LinkedList<GraphWorldState> full_path = new LinkedList<GraphWorldState>();
		    LinkedList<GraphWorldState> path = route.compute_path();

		      if (path == null) {
		        System.out.println("No path");
		        return;
		      }
	          System.out.println("RETURN FROM COMPUTE PATH = " + path);
		      full_path.addAll(path);
		      full_path.removeLast();

		      GraphWorldState last_state = path.peekLast();
		      if (!last_state.equals(route.world.gstate)) {
		    	  System.out.println("Something is not right");
		       // route.world.update_start(last_state);
		      }
		      else {
		        System.out.println("Completed successfully");
		      }
		      
		      ListIterator<GraphWorldState> iter = full_path.listIterator(0);
			    while (iter.hasNext()) {
			      GraphWorldState next_state = iter.next();
			      System.out.println(next_state);
			      Log.d("PRINTING SHORTEST PATH",next_state.nodename+"");
			    }
			 //   System.out.println(route.world.gstate.);
				Log.d("DESTINATION",route.world.destnode +"");
	}

}
