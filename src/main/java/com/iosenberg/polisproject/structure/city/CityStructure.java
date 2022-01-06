package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableMap;
import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.dimension.PPWorldSavedData;
import com.iosenberg.polisproject.structure.city.AbstractCityManager.Piece;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.storage.WorldSavedData;

public class CityStructure extends Structure<NoFeatureConfig>{
	
	//Basically a custom enumeration of biomes. Referenced and stored for map-building purposes
	private static final Map<String, Integer> biomeMap = ImmutableMap.<String, Integer>builder().put("NONE", 0)
			.put("TAIGA", 1).put("EXTREME_HILLS", 2).put("JUNGLE", 3).put("MESA", 4).put("PLAINS", 5).put("SAVANNA", 6)
			.put("ICY", 7).put("THE_END", 8).put("BEACH", 9).put("FOREST", 10).put("OCEAN", 11).put("DESERT", 12)
			.put("RIVER", 13).put("SWAMP", 14).put("MUSHROOM", 15).put("NETHER", 16).build();
	private static boolean[][] cityMap;
	private static int height;

	public CityStructure(Codec<NoFeatureConfig> codec) {
		super(codec);
	}
	
	@Override
	public IStartFactory<NoFeatureConfig> getStartFactory() {
		return CityStructure.Start::new;
	}
	
	@Override
	public GenerationStage.Decoration step() {
		// TODO Revisit after adding structures
		return GenerationStage.Decoration.RAW_GENERATION;
	}
	
	//During this function, I generate the map of the terrain which is passed into the StructurePieces class.
	@Override
	protected boolean isFeatureChunk(ChunkGenerator generator, BiomeProvider biomeProviderIn, long seed,
			SharedSeedRandom seedRand, int chunkX, int chunkZ, Biome biomeIn, ChunkPos chunkPos,
			NoFeatureConfig config) {
		if (!biomeIn.getBiomeCategory().toString().equals("DESERT")) return false; //temporary, just here to not waste time while generating
		
		ChunkPos villageChunk = Structure.VILLAGE.getPotentialFeatureChunk(generator.getSettings().getConfig(Structure.VILLAGE), seed, seedRand, chunkX, chunkZ);
		if(Math.sqrt((villageChunk.x - chunkX)^2 + (villageChunk.z - chunkZ)^2) < 19) return false; //19 is the number required for the radius to contain all of the city's borders
		
		int x = chunkX << 4;
		int z = chunkZ << 4;
		
		//44 is hardcoded into everything, but if that becomes a hassle, I'll make a constant
		//int size = 44;
		
		
		//Get height and biome of each cell, write it into heightArray and biomeArray
		//Also get the mode height and biome for later use
		int[][] heightArray = new int[44][44];
		int[][] biomeArray = new int[44][44];
		
		int[] modeHeightArray = new int[130];
		int[] modeBiomeArray = new int[17];
		for(int i = 0; i < 44; i++) {
			for(int j = 0; j < 44; j++) {
				int tempx = x + i*4 - 80; //-80 to offset the center of the structure
				int tempz = z + j*4 - 80;
				
				int thisHeight = generator.getBaseHeight(tempx, tempz, Heightmap.Type.WORLD_SURFACE);
				int thisBiome = biomeMap.get(biomeProviderIn.getNoiseBiome(tempx/4, thisHeight, tempz/4).getBiomeCategory().toString());

				heightArray[i][j] = thisHeight;
				biomeArray[i][j] = thisBiome;
				
				if(thisHeight < 130) modeHeightArray[thisHeight] ++; //prevents out of bounds
				modeBiomeArray[thisBiome]++;
			}
		}
		
		//Calculate mode height
		int heightModeIndex = 0;
		for(int i=0;i<modeHeightArray.length;i++) if (modeHeightArray[i] > modeHeightArray[heightModeIndex]) heightModeIndex = i;
		height = heightModeIndex;
		
		//Calculate mode biome
		int biomeModeIndex = 0;
		for(int i=0;i<modeBiomeArray.length;i++) if (modeBiomeArray[i] > modeBiomeArray[biomeModeIndex]) biomeModeIndex = i;
		
		
		//Start of city placement algorithm.
		
		//Holds the map of cells. true = this cell will not be in the city. false = this cell will be in the city
		boolean[][] smallCityMap = new boolean[44][44];
		boolean[][] marked = new boolean[44][44];

		LinkedList<Point> queue = new LinkedList<Point>();		
		
		//Step 1: Starting from the outside, mark true every invalid cell
		for(int i = 0; i < 43; i++) {
			queue.add(new Point(0,i));
			marked[0][i] = true;
			queue.add(new Point(i, 43));
			marked[i][43] = true;
			queue.add(new Point(43,43-i));
			marked[43][43-i] = true;
			queue.add(new Point(43-i, 0));
			marked[43-i][0] = true;
		}
		
		while(!queue.isEmpty()) {
			Point p = queue.poll();
			if(Math.abs(heightArray[p.x][p.y] - height) > 4 || biomeArray[p.x][p.y] != biomeModeIndex) {
				smallCityMap[p.x][p.y] = true;
				if(p.x > 0 && !marked[p.x-1][p.y]) {
					queue.add(new Point(p.x-1,p.y));
					marked[p.x-1][p.y] = true; 
				}
				if(p.x < 43 && !marked[p.x+1][p.y]) {
					queue.add(new Point(p.x+1,p.y));
					marked[p.x+1][p.y] = true; 
				}
				if(p.y > 0 && !marked[p.x][p.y-1]) {
					queue.add(new Point(p.x,p.y-1));
					marked[p.x][p.y-1] = true; 
				}
				if(p.y < 43 && !marked[p.x][p.y+1]) {
					queue.add(new Point(p.x,p.y+1));
					marked[p.x][p.y+1] = true; 
				}
			}
		}
		
		//Step 2: Smooth edges, valid cells with three invalid neighbors
		for (int i = 0; i < 44; i++ ) {
			for (int j = 0; j < 44; j++) {
				queue.add(new Point(i,j));
			}
		}
		
		while(!queue.isEmpty()) {
			Point p = queue.poll();
			int i = p.x; //i'm lazy, lol
			int j = p.y;
			if(!smallCityMap[i][j]) {
				boolean notLowesti = i != 0;
				boolean notHighesti = i != 43;
				boolean notLowestj = j != 0;
				boolean notHighestj = j != 43;
							
				//Checks each opposite pair of cells around it
				if((notLowesti && notHighesti) && (smallCityMap[i-1][j] && smallCityMap[i+1][j]))
					smallCityMap[i][j] = true;
				if((notLowestj && notHighestj) && (smallCityMap[i][j-1] && smallCityMap[i][j+1]))
					smallCityMap[i][j] = true;
				//Checks diagonals
				if(notLowesti && notHighesti && notLowestj && notHighestj) {
						if (smallCityMap[i+1][j+1] && smallCityMap[i-1][j-1])
							smallCityMap[i][j] = true;
						if (smallCityMap[i-1][j+1] && smallCityMap[i+1][j-1])
							smallCityMap[i][j] = true;
				}
				//Check if cell is on the side
				if(!notLowesti || !notHighesti || !notLowestj || !notHighestj)
					smallCityMap[i][j] = true;
				
				//If value has changed, enqueue valid neighbors
				if(smallCityMap[i][j]) {
					if(i > 0) if(!smallCityMap[i-1][j]) 
						queue.add(new Point(i-1,j));
					if(i < 43) if(!smallCityMap[i+1][j]) 
						queue.add(new Point(i+1,j));
					if(j > 0) if(!smallCityMap[i][j-1]) 
						queue.add(new Point(i,j-1));
					if(j < 43) if(!smallCityMap[i][j+1])
						queue.add(new Point(i,j+1));
				}
			}
		}

		//Step three: Calculate size of biggest "island." Remove all smaller islands
		byte[][] islandMap = new byte[44][44];
		LinkedList<Integer> islandSizeList = new LinkedList<Integer>();
		islandSizeList.add(0); //since the first island is labeled 1
		byte islandCounter = 1;
		marked = new boolean[44][44];
		
		for(int i = 0; i < 44; i++) {
			for(int j = 0; j < 44; j++) {
				if(islandMap[i][j] == 0 && !smallCityMap[i][j]) {
					queue.add(new Point(i,j));
					int size = 0;
					while(!queue.isEmpty()) {
						Point p = queue.poll();
						islandMap[p.x][p.y] = islandCounter;
						size++;
						if(p.x != 0 && !marked[p.x-1][p.y] && !smallCityMap[p.x-1][p.y]) {
							queue.add(new Point(p.x-1,p.y)); 
							marked[p.x-1][p.y] = true; 
						}
						if(p.x != 43 && !marked[p.x+1][p.y] && !smallCityMap[p.x+1][p.y]) {
							queue.add(new Point(p.x+1,p.y));
							marked[p.x+1][p.y] = true; 
						}
						if(p.y != 0 && !marked[p.x][p.y-1] && !smallCityMap[p.x][p.y-1]) {
							queue.add(new Point(p.x,p.y-1));
							marked[p.x][p.y-1] = true;
						}
						if(p.y != 43 && !marked[p.x][p.y+1] && !smallCityMap[p.x][p.y+1]) {
							queue.add(new Point(p.x,p.y+1));
							marked[p.x][p.y+1] = true;
						}
					}
					islandSizeList.add(size);
					islandCounter++;
				}
			}
		}
		
		//Finds largest island
		int maxSizeIndex = 0;
		for(int i = 1; i < islandSizeList.size(); i++)
				if(islandSizeList.get(i) > islandSizeList.get(maxSizeIndex))
						maxSizeIndex = i;
		
		//If island is not large enough, returns false: no city
		if(islandSizeList.get(maxSizeIndex) < 1100) return false;
		
		//Sets all cells not in the island to true;
		for(int i = 0; i < 44; i++)
			for(int j = 0; j < 44; j++)
				if(!smallCityMap[i][j] && islandMap[i][j] != maxSizeIndex)
					smallCityMap[i][j] = true;

		
		//Generate places of interest? by splitting city into smaller polygons
		//This is messy. Need to find a better way to calculate a centroid-esque location
		//Find centroid:
		int lowesti = -1;
		int highesti = -1;
		int lowestj = -1;
		int highestj = -1;
		for(int i=0;i<44;i++) {
			for(int j=0;j<44;j++) {
				if(!smallCityMap[i][j]) {
					lowesti = i;
					break;
				}
			}
			if(lowesti != -1) break;
		}
		for(int i=43;i>=0;i--) {
			for(int j=0;j<44;j++) {
				if(!smallCityMap[i][j]) {
					highesti = i;
					break;
				}
			}
			if(highesti != -1) break;
		}
		for(int j=0;j<44;j++) {
			for(int i=0;i<44;i++) {
				if(!smallCityMap[i][j]) {
					lowestj = j;
					break;
				}
			}
			if(lowestj != -1) break;
		}
		for(int j=43;j>=0;j--) {
			for(int i=0;i<44;i++) {
				if(!smallCityMap[i][j]) {
					highestj = j;
					break;
				}
			}
			if(highestj != -1) break;
		}
		
		
		Point centroid = new Point((highesti + lowesti)/2,(highestj + lowestj)/2);
		
		//Gonna try point of inaccessibility instead of centroid
		
		LinkedList[] districtList = {
				new LinkedList<Point>(), //LILJ
				new LinkedList<Point>(), //LIHJ
				new LinkedList<Point>(), //HILJ
				new LinkedList<Point>()  //HIHJ
		};
		
		for(int i=0;i<44;i++) {
			for(int j=0;j<44;j++) {
				if(!smallCityMap[i][j]) {
					if(i<centroid.x) { //LI
						if(j<centroid.y) { //LILJ
							districtList[0].add(new Point(i,j));
						}	
						else { //LIHJ
							districtList[1].add(new Point(i,j));
						}
					}
					else { //HI
						if(j<centroid.y) { //HILJ
							districtList[2].add(new Point(i,j));
						}
						else { //HIHJ
							districtList[3].add(new Point(i,j));
						}
					}
				}
			}
		}
		
		int[] minDists = {Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
		Point[] PoI = new Point[4]; //Points of Inaccessibility / Places of Interest
		for(int i=0; i < 4; i++) {
			LinkedList<Point> list = districtList[i];
			for(int j = 0; j < list.size(); j++) {
				int dist = 0;
				for(int k = 0; k < districtList[i].size(); k++) {
					//Add distance from j to k to dist
					dist += Math.abs(list.get(j).x - list.get(k).x) + Math.abs(list.get(j).y - list.get(k).y);
				}
				if(dist < minDists[i]) {
					minDists[i] = dist;
					PoI[i] = list.get(j);
				}
			}
		}
		
		
//		//All this is just a complex print statment
//		byte[][] printmap = new byte[44][44];
//		for(int i = 0;i<44;i++) {
//			for(int j = 0;j<44;j++) {
//				printmap[i][j] = (byte) (smallCityMap[i][j] ? 0 : 1);
//			}
//		}
//		
//		for(int i = 0; i < 4;i++) {
//			for(int j = 0; j < districtList[i].size(); j++) {
//				printmap[((Point)districtList[i].get(j)).x][((Point)districtList[i].get(j)).y] = (byte)(i+1);
//			}
//			printmap[PoI[i].x][PoI[i].y] = 8; 
//		}
//		
//		for(int i = 0;i<44;i++) {
//			for(int j=0;j<44;j++) {
//				System.out.print(printmap[i][j] == 0 ? "." : printmap[i][j]);
//			}
//			System.out.println();
//		}
//		//End print statement
		
		
		//Rewrites the smaller map into a map of appropriate size
		cityMap = new boolean[176][176];
		
		for(int i = 0; i < 176; i++) 
			for(int j = 0; j < 176; j++) 
				cityMap[i][j] = !smallCityMap[i/4][j/4]; //I'll think of a more efficient way to handle the boolean map later
		
		byte[] map = new byte[44*44];
		for(int i = 0;i < 44*44; i++) {
			map[i] = (byte)(smallCityMap[i/44][i%44] ? 0 : 1);
		}
		long[] anchors = {
				BlockPos.asLong(PoI[0].x*4 + x - 80, heightModeIndex, PoI[0].y*4 + z - 80),
				BlockPos.asLong(PoI[1].x*4 + x - 80, heightModeIndex, PoI[1].y*4 + z - 80),
				BlockPos.asLong(PoI[2].x*4 + x - 80, heightModeIndex, PoI[2].y*4 + z - 80),
				BlockPos.asLong(PoI[3].x*4 + x - 80, heightModeIndex, PoI[3].y*4 + z - 80)
		};
		
		byte byteHeight = (byte)(heightModeIndex + Byte.MIN_VALUE);
		byte biome = (byte)biomeModeIndex;
		
		//TODO: Later, these will be written into WorldSaveData
		PPWorldSavedData.putCity(chunkX, chunkZ, byteHeight, biome, map, anchors);
		DebugCityManager.height(heightModeIndex);
		DebugCityManager.map(cityMap);
		
		if (biomeModeIndex == 12 /* desert */) return true; //Just to make sure. This won't be needed once there are more biome cities
		return false;
	}
	
	
	
	public static class Start extends StructureStart<NoFeatureConfig> {
		public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ,
				MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
			this.boundingBox = mutableBoundingBox;
		}
		
		@Override
		public void generatePieces(DynamicRegistries dynamicRegistryManager, ChunkGenerator generator,
				TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {
			
			int x = chunkX << 4;
			int z = chunkZ << 4;
			int y = generator.getBaseHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);
			
			CompoundNBT city = PPWorldSavedData.getCity(chunkX, chunkZ);
			int biome = city.getByte("biome");
			
			DebugCityManager.start(templateManagerIn, new BlockPos(x, y, z), city, this.pieces, random);
			
			this.calculateBoundingBox();
		}
	}
}
