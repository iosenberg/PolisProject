package com.iosenberg.polisproject.structure;

import java.awt.Point;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

import org.apache.commons.lang3.RandomUtils;

import com.iosenberg.polisproject.dimension.PPWorldSavedData;
import com.iosenberg.polisproject.utils.GeneralUtils;
import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class RoadFeature extends Feature<NoFeatureConfig>{
	static final int JUNCTION_SPACE = 64;
	
	public RoadFeature(Codec<NoFeatureConfig> configFactory) {
		super(configFactory);
	}
	
	//Rounds x and z to nearest multiple of 64 for now
	public static ChunkPos findJunction(ChunkPos pos) {
		int lowerX = (pos.x / JUNCTION_SPACE) * JUNCTION_SPACE;
		int upperX = lowerX + JUNCTION_SPACE;
		int lowerZ = (pos.z / JUNCTION_SPACE) * JUNCTION_SPACE;
		int upperZ = lowerZ + JUNCTION_SPACE;
		int x = (pos.x - lowerX > upperX - pos.x) ? upperX : lowerX; 
		int z = (pos.z - lowerZ > upperZ - pos.z) ? upperZ : lowerZ; 
		return new ChunkPos(x, z);
	}
	
	//Returns the input pos rounded down to the nearest JUNCTION_SPACEth
	public static ChunkPos calculateOffset(ChunkPos pos) {
		int x = (pos.x / JUNCTION_SPACE) * JUNCTION_SPACE;
		int z = (pos.z / JUNCTION_SPACE) * JUNCTION_SPACE;
		if (pos.x < 0) x -= JUNCTION_SPACE;
		if (pos.z < 0) z -= JUNCTION_SPACE;
		return new ChunkPos(x, z);
	}
	
	/**
	 * Returns the map of weights off which road generation is based.
	 * If map doesn't exist, creates the map
	 * It's a janky implementation. I'll clean it up another time.
	 * 
	 * @param cornerChunk - ChunkPos of the bottom left corner of the map
	 * @param generator - a chunk generator
	 * @return Point[][] map, where Point p.x is the standard deviation(ish) of a chunk, and p.y is the mean height
	 */
	public static Point[][] getWeightMap(ChunkPos cornerChunk, ChunkGenerator generator) {
		Point[][] weightMap = new Point[JUNCTION_SPACE + 1][JUNCTION_SPACE + 1];
		
		for(int i = 0; i < weightMap.length; i++) {
			for(int j = 0; j < weightMap[0].length; j++) {
				int x = (cornerChunk.x + i) << 4;
				int z = (cornerChunk.z + j) << 4;
				int mean = 0;
				int maxHeight = 0;
				int minHeight = Integer.MAX_VALUE;
				for(int k = x; k < x + 16; k += 4) {
					for(int l = z; l < z + 16; l += 4) {
						int height = generator.getBaseHeight(k, l, Heightmap.Type.OCEAN_FLOOR);
						mean += height;
						if(height > maxHeight) maxHeight = height;
						if(height < minHeight) minHeight = height;
					}
				}
				weightMap[i][j] = new Point(maxHeight - minHeight, mean/16);
			}
		}
		
		return weightMap;
	}
	
	public static ChunkPos[] generateDestinations(ChunkPos source, int max) {
		//List of corners sorted by distance from source
		ArrayList<ChunkPos> destinations = new ArrayList<ChunkPos>(4);
		destinations.add(new ChunkPos(0,0));
		destinations.add(new ChunkPos(0, JUNCTION_SPACE));
		destinations.add(new ChunkPos(JUNCTION_SPACE, 0));
		destinations.add(new ChunkPos(JUNCTION_SPACE, JUNCTION_SPACE));
		destinations.sort((a, b) -> source.x - a.x + source.z - a.z + source.x - b.x + source.z - b.z);
		
		//Removes destinations from the list if they are too far away (by chessboard distance) or if the list is too big
		while(destinations.size() > max || destinations.get(0).getChessboardDistance(source) > 75) {
			destinations.remove(0);
		}
		
		//Return destinations as array
		return destinations.toArray(new ChunkPos[0]);
	}
	
	public static void generateAllRoads(ChunkPos source, int maxDestinations, ChunkPos[] illegalChunks, ChunkGenerator generator) {
		System.out.println("Hello!");
		ChunkPos offset = calculateOffset(source);
		ChunkPos offsetSource = new ChunkPos(source.x - offset.x, source.z - offset.z);
		Point[][] weightMap = getWeightMap(offset, generator);
		ChunkPos[] destinations = generateDestinations(offsetSource, maxDestinations);
		
		System.out.println("Source is " + source.toString() + " and offset source is " + offsetSource.toString());
		
		//Iterate through each destination, generate road
		for(int i = 0; i < destinations.length; i++) {
			ChunkPos[] road = generateRoadChunks(offsetSource, destinations[i], weightMap, offset);
			
			//Add offset
			for(int j = 0; j < road.length; j++) {
				road[j] = new ChunkPos(road[j].x + offset.x, road[j].z + offset.z);
			}
			
			//Add road chunk to roadMap in both directions
			for(int j = 0; j < road.length - 1; j++) {
				PPWorldSavedData.putRoad(road[j], road[j+1]);
				PPWorldSavedData.putRoad(road[j+1], road[j]);
			}
		}
		
		//Remove any illegal chunks from the road
		for(int i = 0; i < illegalChunks.length; i++) {
			PPWorldSavedData.getRoad(illegalChunks[i]);
		}
	}
	
	public static ChunkPos[] generateRoadChunks(ChunkPos s, ChunkPos d, Point[][] weightMap, ChunkPos offset) {
		int xMax = weightMap.length;
		int zMax = weightMap[0].length;
		
		//Run a variant of A* from s to d
		boolean[][] visited = new boolean[xMax][zMax];
		int[][] distance = new int[xMax][zMax];
		ChunkPos[][] previousChunk = new ChunkPos[xMax][zMax];
		for(int i = 0; i < xMax; i++) {
			for(int j = 0; j < zMax; j++) {
				distance[i][j] = Integer.MAX_VALUE;
			}
		}
		
		//This implementation feels incredibly gross, but Java's implementation of priority queue is gross so oh well
		PriorityQueue<ChunkPos> queue = new PriorityQueue<ChunkPos>((a, b) -> distance[a.x][a.z] + GeneralUtils.ManhattanDistance(a, d) - distance[b.x][b.z] - GeneralUtils.ManhattanDistance(b,d));
		distance[s.x][s.z] = 0; 
		queue.offer(s);
		visited[s.x][s.z] = true;
		while(!visited[d.x][d.z]) {
			ChunkPos pos = queue.poll();
			visited[pos.x][pos.z] = true; 
			if(pos.x > 0 && !visited[pos.x - 1][pos.z]) {
				int tx = pos.x - 1; //target x
				int tz = pos.z; //target z
				//"distance" is equal to the stDev of the target chunk + the difference in average height of the to chunks + distance of previous chunk + 1
				int tempDist = weightMap[tx][tz].x + Math.abs(weightMap[tx][tz].y - weightMap[pos.x][pos.z].y) + distance[pos.x][pos.z] + 1;
				//If there's already a road, the cost of building a road there is 0
				if (PPWorldSavedData.containsRoad(new ChunkPos(tx + offset.x, tz + offset.z))) tempDist = distance[pos.x][pos.z];
				if(tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx,tz);
					if(!queue.contains(chunk)) queue.offer(chunk);
				}
			}
			if(pos.x < xMax - 1 && !visited[pos.x + 1][pos.z]) {
				int tx = pos.x + 1; //target x
				int tz = pos.z; //target z
				int tempDist = weightMap[tx][tz].x + Math.abs(weightMap[tx][tz].y - weightMap[pos.x][pos.z].x) + distance[pos.x][pos.z] + 1;
				//If there's already a road, the cost of building a road there is 0
				if (PPWorldSavedData.containsRoad(new ChunkPos(tx + offset.x, tz + offset.z))) tempDist = distance[pos.x][pos.z];
				if(tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx,tz);
					if(!queue.contains(chunk)) queue.offer(chunk);
				}
			}
			if(pos.z > 0 && !visited[pos.x][pos.z - 1]) {
				int tx = pos.x; //target x
				int tz = pos.z - 1; //target z
				int tempDist = weightMap[tx][tz].x + Math.abs(weightMap[tx][tz].y - weightMap[pos.x][pos.z].y) + distance[pos.x][pos.z] + 1;
				//If there's already a road, the cost of building a road there is 0
				if (PPWorldSavedData.containsRoad(new ChunkPos(tx + offset.x, tz + offset.z))) tempDist = distance[pos.x][pos.z];
				if(tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx,tz);
					if(!queue.contains(chunk)) queue.offer(chunk);
				}
			}
			if(pos.z < zMax - 1 && !visited[pos.x][pos.z + 1]) {
				int tx = pos.x; //target x
				int tz = pos.z + 1; //target z
				int tempDist = weightMap[tx][tz].x + Math.abs(weightMap[tx][tz].y - weightMap[pos.x][pos.z].y) + distance[pos.x][pos.z] + 1;
				//If there's already a road, the cost of building a road there is 0
				if (PPWorldSavedData.containsRoad(new ChunkPos(tx + offset.x, tz + offset.z))) tempDist = distance[pos.x][pos.z];
				if(tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx,tz);
					if(!queue.contains(chunk)) queue.offer(chunk);
				}
			}
			
		}
		
		//Finished algorithm. The path is stored in previousChunk[], which will be read into an ArrayList path
		ArrayList<ChunkPos> path = new ArrayList<ChunkPos>();
		ChunkPos chunk = d;
		while(!chunk.equals(s)) {
			path.add(chunk);
			chunk = previousChunk[chunk.x][chunk.z];
		}
		
		return path.toArray(new ChunkPos[0]);
	}

	/**
	 * Very simple road generation, connects s to d in a semi-straight line
	 * 
	 * @param s - Road source block
	 * @param d - Road destination block
	 * @return Array of ChunkPos's representing BlockPos's of the path of the road
	 */
	public static ChunkPos[] generateRoadBlocks(ChunkPos s, ChunkPos d) {
		ArrayList<ChunkPos> path = new ArrayList<ChunkPos>();
		int x = s.x;
		int z = s.z;
		
		path.add(s);
		
		while(x != d.x && z != d.z) {
			if(x < d.x) x++;
			else x--;
			if(z < d.z) z++;
			else z--;
			
			path.add(new ChunkPos(x,z));
		}
		while(x < d.x) {
			x++;
			path.add(new ChunkPos(x,z));
		}
		while(x > d.x) {
			x--;
			path.add(new ChunkPos(x,z));
		}
		while(z < d.z) {
			z++;
			path.add(new ChunkPos(x,z));
		}
		while(z > d.z) {
			z--;
			path.add(new ChunkPos(x,z));
		}
		
		path.add(d);
		
		return path.toArray(new ChunkPos[0]);
	}
	
	

	@Override
	public boolean place(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos,
			NoFeatureConfig config) {
		
		//Gets chunk
		ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		
		//Loads roadNBT from chunk. Return false and don't generate road if chunk is not in PPWorldSavedData.RoadMap
		CompoundNBT roadNBT = PPWorldSavedData.getRoad(chunk);
		if(roadNBT == null) return false;
		boolean east = roadNBT.getBoolean("East");
		boolean west = roadNBT.getBoolean("West");
		boolean south = roadNBT.getBoolean("South");
		boolean north = roadNBT.getBoolean("North");
	
		//Build weight map, 
		Point[][] weightMap = new Point[16][16];
		
		//Calculate height, stored in weightMap.y, and calculates average height
		int mean = 0;	
		for(int i = 0; i < 16; i++) {
			for(int j = 0; j < 16; j++) {
				int height = generator.getBaseHeight(pos.getX() + i, pos.getZ() + j, Heightmap.Type.WORLD_SURFACE);
				weightMap[i][j] = new Point(0, height);
				mean += height;
			}
		}
		
		mean /= (16 * 16);
		
		//Stores difference between height and average height in weightMap.x
		for(int i = 0; i < 16; i++) {
			for(int j = 0; j < 16; j++) {
				weightMap[i][j].x = Math.abs(weightMap[i][j].y - mean);
			}
		}
		
		//Add sides of road to generate:
		ArrayList<ChunkPos> startList = new ArrayList<ChunkPos>(4);
		//Add East
		if(east) {
			if(roadNBT.contains("EastStart")) {
				BlockPos tempPos = BlockPos.of(roadNBT.getLong("EastStart"));
				startList.add(new ChunkPos(tempPos.getX(), tempPos.getZ()));
			}
			else {
				int lowestIndex = 7;
				for(int i = 0; i < 16; i++) {
					if(weightMap[15][i].x < weightMap[15][i].x) lowestIndex = i;
				}
				startList.add(new ChunkPos(15, lowestIndex));
			}
		}
		//Add West
		if(west) {
			if(roadNBT.contains("WestStart")) {
				BlockPos tempPos = BlockPos.of(roadNBT.getLong("WestStart"));
				startList.add(new ChunkPos(tempPos.getX(), tempPos.getZ()));
			}
			else {
				int lowestIndex = 7;
				for(int i = 0; i < 16; i++) {
					if(weightMap[0][i].x < weightMap[0][i].x) lowestIndex = i;
				}
				startList.add(new ChunkPos(0, lowestIndex));
			}
		}
		//Add South
		if(south) {
			if(roadNBT.contains("SouthStart")) {
				BlockPos tempPos = BlockPos.of(roadNBT.getLong("SouthStart"));
				startList.add(new ChunkPos(tempPos.getX(), tempPos.getZ()));
			}
			else {
				int lowestIndex = 7;
				for(int i = 0; i < 16; i++) {
					if(weightMap[i][15].x < weightMap[lowestIndex][15].x) lowestIndex = i;
				}
				startList.add(new ChunkPos(lowestIndex, 15));
			}
		}
		//Add North
		if(north) {
			if(roadNBT.contains("NorthStart")) {
				BlockPos tempPos = BlockPos.of(roadNBT.getLong("NorthStart"));
				startList.add(new ChunkPos(tempPos.getX(), tempPos.getZ()));
			}
			else {
				int lowestIndex = 7;
				for(int i = 0; i < 16; i++) {
					if(weightMap[i][0].x < weightMap[lowestIndex][0].x) lowestIndex = i;
				}
				startList.add(new ChunkPos(lowestIndex, 0));
			}
		}
		
		//Randomize order
		startList.sort((a, b) -> RandomUtils.nextInt(0, 2) -1);
		
		//Final map of roads
		int[][] roadMap = new int[16][16];
		
		//For each road after the first, generate a road to the first. Then add road path to roadMap
		for(int i = 1; i < startList.size(); i++) {
			ChunkPos[] path = generateRoadBlocks(startList.get(0), startList.get(i));
			for(ChunkPos pathChunk : path) {
				for(int k = -1; k < 2; k++) {
					for(int l = -1; l < 2; l++) {
						int pathX = pathChunk.x + k;
						int pathZ = pathChunk.z + l;
						if(pathX > -1 && pathX < 16 && pathZ > -1 && pathZ < 16) {
							//Adds each adjacent block to path block to roadMap, updates to be greatest adjacent height.
							roadMap[pathX][pathZ] = Math.max(roadMap[pathX][pathZ], weightMap[pathChunk.x][pathChunk.z].y);
						}
					}
				}
				weightMap[pathChunk.x][pathChunk.z].y = 1;
			}
		}
			
		//Generate all road blocks
		for(int i = 0; i < 16; i++) {
			for(int j = 0; j < 16; j++) {
				if(roadMap[i][j] != 0) {
					int x = pos.getX() + i;
					int z = pos.getZ() + j;
					int y = roadMap[i][j] - 1;
					reader.setBlock(new BlockPos(x, y, z), Blocks.STONE_BRICKS.defaultBlockState(), 4);
					while(y < weightMap[i][j].y) {
						y++;
						reader.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 4);
					}
				}
			}
		}
		
		//Add connecting blocks to adjacent road chunks
		PPWorldSavedData.updateRoadStarts(chunk, startList.toArray(new ChunkPos[0]));
		
		return true;
	}
}
