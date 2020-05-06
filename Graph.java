package ShortestMiddPath;

/*
* Author: Shelby Kimmel
* Creates a adjacency list object to store information about the graph of roads, and contains the main functions used to
* run the Bellman Ford algorithm

*/

import java.io.File;
import java.util.Scanner;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.lang.Math;
import java.io.File;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Graph {

	// Object that contains an adjacency list of a road network, and a dictionary from elements of the list to indices from 0 to |V|-1
	HashMap<Integer, ArrayList<Road>> adjList;
	HashMap<Integer,Integer> nodeDict;
	HashMap<Integer,Integer> nodeDictReverse;


	public Graph(String file) throws IOException{
		// We will store the information about the road graph in an adjacency list
		// We will use a HashMap to store the Adjacency List, since each vertex in the graph has a more or less random integer name.
		// Each element of the HashMap will be an ArrayList containing all roads (edges) connected to that vertex
		adjList = new HashMap<>();
		nodeDict = null;

		// Based on https://stackoverflow.com/questions/49599194/reading-csv-file-into-an-arrayliststudent-java
		String line = null;
		BufferedReader br = new BufferedReader(new FileReader(file));
		if ((line=br.readLine())==null){
			return;
		}
		while ((line = br.readLine())!=null) {
			String[] temp = line.split(",");
			//Assume all roads are two-way, and using ArcMiles as distance:
			this.addToList(new Road(Integer.parseInt(temp[60]),Integer.parseInt(temp[61]),temp[9],Double.parseDouble(temp[31])));
			this.addToList(new Road(Integer.parseInt(temp[61]),Integer.parseInt(temp[60]),temp[9],Double.parseDouble(temp[31])));
		}

		//For dynamic programming, we will have an array with indices 0 to |V|-1,
		// where |V| is the number of vertices. Thus we need to associate each element of adjList with a number between 0 and |V|-1
		// We will use a Dictionary (HashMap) to do this.
		nodeDict = new HashMap<>();
		int j = 0;
		for (Integer nodeName: adjList.keySet()){
			nodeDict.put(nodeName, j);
			j++;
		}

		//creates a hashmap that is the inverse of nodeDict, with keys as values and values as keys
		nodeDictReverse = new HashMap<>();
		int k = 0;
		for (Integer nodeName: adjList.keySet()){
			nodeDictReverse.put(k, nodeName);
			k++;
		}
	}

	// get functions
	public HashMap<Integer, ArrayList<Road>> getAdjList(){
		return adjList;
	}
	public HashMap<Integer,Integer> getDict(){
		return nodeDict;
	}

	//Adds the Road (edge) to the appropriate list of the adjacency list, used by the constructor method
	//Based on https://stackoverflow.com/questions/12134687/how-to-add-element-into-arraylist-in-hashmap
	public synchronized void addToList(Road road) {
		Integer node = road.getStart();
    	ArrayList<Road> roadList = this.getAdjList().get(node);

    	// if node is not already in adjacency list, we creat a list for it
    	if(roadList == null) {
    	    roadList = new ArrayList<Road>();
    	    roadList.add(road);
   		    this.getAdjList().put(node, roadList);
  	  	}
  	  	else {
        	// add if item is not already in list
        	if(!roadList.contains(road)) roadList.add(road);
    	}

    }

	//fills in an array containing the shortest path length from startNode to any
	//other given node in the graph.
	//returns array containing the shortest path length from startNode to any
	//other given node in the graph.
	public Double[][] ShortestDistance(Integer startNode){

		int vMag = nodeDict.size();
		Double[][] dpArray = new Double[vMag][vMag];

		//intializes starting node first column with 0
		int startInd = nodeDict.get(startNode);
		dpArray[startInd][0] = 0.0;

		//initializes first column with positive infinity
		for(int i = 0; i <= vMag-1; i++) {
			if(i != startInd) {
				dpArray[i][0] = Double.POSITIVE_INFINITY;
			}
		}

		//fills in array
		for(int i = 1; i <= vMag-1; i++) {
			for(Map.Entry<Integer, ArrayList<Road>> entry : adjList.entrySet()) {
				Integer key = entry.getKey();
				ArrayList<Road> roads = entry.getValue();
				int keyTrans = nodeDict.get(key);
				dpArray[keyTrans][i] = dpArray[keyTrans][i-1];
				for(Road r : roads) {
					Integer node = r.endNode;
					Integer nodeTrans = nodeDict.get(node);
					if(dpArray[nodeTrans][i-1] + r.miles < dpArray[keyTrans][i]) {
						dpArray[keyTrans][i] = dpArray[nodeTrans][i-1] + r.miles;
					}
				}
			}
		}

		return dpArray;
	}

	//works backwards through dpArray to create the shortest path to endNode
	//from the startNode that dpArray was created with
    public void ShortestPath(Integer endNode, Double[][] dpArray){

		ArrayList<String> path = new ArrayList<String>();

		//translates endNode to index for dpArray
		//checks if a path was found
		Integer endNTrans = nodeDict.get(endNode);
		int vMag = nodeDict.size();
		if(dpArray[endNTrans][vMag-1] == Double.POSITIVE_INFINITY) {
			System.out.println("No path found");
		}

		//works backwards through dpArray to get shortest path
		int i = vMag - 1;
		int v = endNTrans;
		while(i > 0) {
			if(dpArray[v][i] != dpArray[v][i-1]) {
				Integer nodeVal = nodeDictReverse.get(v);
				ArrayList<Road> roads = adjList.get(nodeVal);
				for(Road r : roads) {
					//System.out.println("v: " + v + " u: " + nodeDict.get(r.endNode));
					Integer u = nodeDict.get(r.endNode);
					if(dpArray[v][i] == dpArray[u][i-1] + r.miles) {
						path.add(0, r.name);
						v = u;
						break;
					}
				}
			}
			i--;
		}

		//prints the path that was found
		for(int k = 0; k < path.size(); k++) {
			if(k != path.size() - 1) {
				System.out.print(path.get(k) + ", ");
			} else {
				System.out.print(path.get(k) + ".");
			}

		}
	}

}
