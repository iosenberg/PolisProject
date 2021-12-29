package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.LinkedList;

/*I have no idea where else to put this. It uses FF algorithm to place
 * a city on the given terrain. Might move it into AbstractCityManager or CityStructure later
 */
/*
 * Also, this implementation is based on that from geeksforgeeks.org.
 * https://www.geeksforgeeks.org/ford-fulkerson-algorithm-for-maximum-flow-problem/
 */
public class FordFulkerson {
	//for now, this takes in a Point[176][176], where x = terrain height, and y = biome
	public static byte[][] placeCity(Point[][] mapIn, int biome) {
		int size = mapIn.length; //assuming square
		
		byte[][] G = new byte[size * size + 2][size * size + 2];
		int s = size*size; //source
		int t = size*size+1; //sink
		
		//Create graph:
		//for each vertex in G:
		for(int i = 0; i < size; i++) { //i is multiplied by size to get the right coordinate in a 1d array
			for(int j = 0; j < size; j++) {
				//adds an edge up and right with capacity = difference in height of those blocks
				//only adds up and right per block, but works out that all adjacent blocks have a pair of edges

				if(i>0) { //add edge in both directions in direction of -i
					byte c = (byte)(Byte.MAX_VALUE - 3*Math.abs(mapIn[i][j].x - mapIn[i-1][j].x));
					G[i*size + j][(i-1)*size + j] = c;
					G[(i-1)*size + j][i*size + j] = c;
				}
				if(j<size-1) { //add edge in both directions in direction of +j
					byte c = (byte)(Byte.MAX_VALUE - 3*Math.abs(mapIn[i][j].x - mapIn[i][j+1].x));
					G[i*size + j][i*size + j + 1] = c;
					G[i*size + j + 1][i*size + j] = c;
				}
				
				//adds an edge from source (Likelihood to be in cut A), c = distance from the center
				G[s][i*size+j] = (byte)(2*size - 2*Math.sqrt((size-i)^2 + (size-j)^2) + 53);
				
				//adds an edge to sink (Likelihood to be in cut B (NOT in cut A), c = whether biome matches desired biome
				//This might be simplified to an edge connecting all vertices with different biomes but that sounds time expensive and for now I just need a capacity B
				G[i*size+j][t] = (byte)(2*Math.sqrt((size-i)^2 + (size-j)^2) + (mapIn[i][j].y == biome ? 10 : 70));
			}
		}
		
		for(int i = 0; i < size+2; i++) {
			for(int j = 0; j < size+2; j++) {
				if (G[i][j] < 0) {
					System.out.println("Too big!");
					G[i][j] = Byte.MAX_VALUE;		
				}
			}
		}
		
		G = fordFulkerson(G, s, t); //overwrites G to be final residual graph;
		boolean[] cutA = new boolean[size*size+2];
		bfs(G, s, t, new int[size*size+2], cutA);
		System.out.println("Done.");
		byte[][] returnMap = new byte[176][176];
		int scale = 176 / size; //Used to scale whatever the input size is back to 176
		
		
		for(int i = 0; i < size; i++) {
			for( int j = 0; j < size; j++) {
				int scaledi = i * scale;
				int scaledj = j * scale;
				for (int k = 0; k < scale; k++) {
					for (int l = 0; l < scale; l++) {
						//Fills in each square scaled up (e.g. if scale = 2, fills in 4 blocks per i,j, to fill in the whole graph
						if(cutA[i*size + j]) returnMap[scaledi + k][scaledj + l] = (byte)mapIn[i][j].x;
						else System.out.println((scaledi+k) + "," + (scaledj+l));
					}
				}
			}
		}
		
		return returnMap;
	}
	
	

	
	
	/*Credit to the rest of this to https://www.geeksforgeeks.org/ford-fulkerson-algorithm-for-maximum-flow-problem/.
	 * I copy and pasted for debug purposes, but if it works, I'll rewrite the code
	 */
	
    /* Returns true if there is a path from source 's' to
    sink 't' in residual graph. Also fills parent[] to
    store the path */
  static boolean bfs(byte rGraph[][], int s, int t, int parent[], boolean visited[])
  {
      // Create a visited array and mark all vertices as
      // not visited
//      boolean visited[] = new boolean[V];
//      for (int i = 0; i < V; ++i)
//          visited[i] = false;
	  int V = visited.length;

      // Create a queue, enqueue source vertex and mark
      // source vertex as visited
      LinkedList<Integer> queue
          = new LinkedList<Integer>();
      queue.add(s);
      visited[s] = true;
      parent[s] = -1;

      // Standard BFS Loop
      while (queue.size() != 0) {
          int u = queue.poll();

          for (int v = 0; v < V; v++) {
              if (visited[v] == false
                  && rGraph[u][v] > 0) {
                  // If we find a connection to the sink
                  // node, then there is no point in BFS
                  // anymore We just have to set its parent
                  // and can return true
                  if (v == t) {
                      parent[v] = u;
                      return true;
                  }
                  queue.add(v);
                  parent[v] = u;
                  visited[v] = true;
              }
          }
      }

      // We didn't reach sink in BFS starting from source,
      // so return false
      return false;
  }

  // Returns tne maximum flow from s to t in the given
  // graph
  static byte[][] fordFulkerson(byte graph[][], int s, int t)
  {
      int u, v;
      int V = t+1;

      // Create a residual graph and fill the residual
      // graph with given capacities in the original graph
      // as residual capacities in residual graph

      // Residual graph where rGraph[i][j] indicates
      // residual capacity of edge from i to j (if there
      // is an edge. If rGraph[i][j] is 0, then there is
      // not)
      byte rGraph[][] = new byte[V][V];

      for (u = 0; u < V; u++)
          for (v = 0; v < V; v++)
              rGraph[u][v] = graph[u][v];

      // This array is filled by BFS and to store path
      int parent[] = new int[V];

//      int max_flow = 0; // There is no flow initially

      // Augment the flow while there is path from source
      // to sink
      while (bfs(rGraph, s, t, parent, new boolean[V])) {
          // Find minimum residual capacity of the edhes
          // along the path filled by BFS. Or we can say
          // find the maximum flow through the path found.
          int path_flow = Integer.MAX_VALUE;
          for (v = t; v != s; v = parent[v]) {
              u = parent[v];
              path_flow
                  = Math.min(path_flow, rGraph[u][v]);
          }

          // update residual capacities of the edges and
          // reverse edges along the path
          for (v = t; v != s; v = parent[v]) {
              u = parent[v];
              rGraph[u][v] -= path_flow;
              rGraph[v][u] += path_flow;
          }

          // Add path flow to overall flow
//          max_flow += path_flow;
      }

      // Return the overall flow
      return rGraph;
  }
}
