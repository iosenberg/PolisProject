package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableMap;
import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.structure.city.AbstractCityManager.Piece;
import com.mojang.serialization.Codec;

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
	
	//Need to rewrite
	@Override
	protected boolean isFeatureChunk(ChunkGenerator generator, BiomeProvider biomeProviderIn, long seed,
			SharedSeedRandom seedRand, int chunkX, int chunkZ, Biome biomeIn, ChunkPos chunkPos,
			NoFeatureConfig config) {
		if (!biomeIn.getBiomeCategory().toString().equals("DESERT")) return false; //temporary, just here to not waste time while generating
		
		ChunkPos villageChunk = Structure.VILLAGE.getPotentialFeatureChunk(generator.getSettings().getConfig(Structure.VILLAGE), seed, seedRand, chunkX, chunkZ);
		if(Math.sqrt((villageChunk.x - chunkX)^2 + (villageChunk.z - chunkZ)^2) < 19) return false; //19 is the number required for the radius to contain all of the city's borders
		
		//maybe check standard deviation of the height of blocks????
		//Also make sure it doesn't spawn too close to a village, or anything else that might cause a big overlap
		int x = chunkX << 4;
		int z = chunkZ << 4;
		
		//int size = 44;
		
		Point[][] pointArray = new Point[44][44];
		
		int[] modeHeightArray = new int[130];
		int[] modeBiomeArray = new int[17];
		for(int i = 0; i < 44; i++) {
			for(int j = 0; j < 44; j++) {
				int tempx = x + i*4 - 80; //-80 to offset the center of the structure
				int tempz = z + j*4 - 80;
				int thisHeight = generator.getBaseHeight(tempx, tempz, Heightmap.Type.WORLD_SURFACE);
				int thisBiome = biomeMap.get(biomeProviderIn.getNoiseBiome(tempx/4, thisHeight, tempz/4).getBiomeCategory().toString());
				pointArray[i][j] = new Point(thisHeight, thisBiome);
				if(thisHeight < 130) modeHeightArray[thisHeight] ++;
				modeBiomeArray[thisBiome]++;
			}
		}
		
		int heightModeIndex = 0;
		for(int i=0;i<modeHeightArray.length;i++) if (modeHeightArray[i] > modeHeightArray[heightModeIndex]) heightModeIndex = i;
		height = heightModeIndex;
		
		int biomeModeIndex = 0;
		for(int i=0;i<modeBiomeArray.length;i++) if (modeBiomeArray[i] > modeBiomeArray[biomeModeIndex]) biomeModeIndex = i;
		
		boolean[][] smallcityMap = new boolean[44][44];
		boolean[][] marked = new boolean[44][44];

		LinkedList<Point> queue = new LinkedList<Point>();		
		
		//Step 1: Add bad 
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
			if(Math.abs(pointArray[p.x][p.y].x - height) > 4 || pointArray[p.x][p.y].y != biomeModeIndex) {
				smallcityMap[p.x][p.y] = true;
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
		
		System.out.println("Step 1:");
		for(int i = 0; i < 44; i++) {
			for(int j = 0; j < 44; j++) {
				System.out.print(smallcityMap[i][j] ? 1 : 0);
			}
			System.out.println();
		}
		
//		marked = new boolean[44][44];
		
		//Step 2: Remove shlongs p one
		for (int i = 0; i < 44; i++ ) {
			for (int j = 0; j < 44; j++) {
				queue.add(new Point(i,j));
//				if(!smallcityMap[i][j]) {
//					if(i == 0) 
//						smallcityMap[i][j] = true;
//					else if (i == 43)
//						smallcityMap[i][j] = true;
//					else if (smallcityMap[i-1][j] && smallcityMap[i+1][j])
//						smallcityMap[i][j] = true;
//					if(j == 0)
//						smallcityMap[i][j] = true;
//					else if (j == 43)
//						smallcityMap[i][j] = true;
//					else if (smallcityMap[i][j-1] && smallcityMap[i][j+1])
//						smallcityMap[i][j] = true;
//					if(smallcityMap[i][j]) {
//						if(i > 0) if(!smallcityMap[i-1][j]) 
//							queue.add(new Point(i-1,j));
//						if(i < 43) if(!smallcityMap[i+1][j]) 
//							queue.add(new Point(i+1,j));
//						if(j > 0) if(!smallcityMap[i][j-1]) 
//							queue.add(new Point(i,j-1));
//						if(j < 43) if(!smallcityMap[i][j+1])
//							queue.add(new Point(i,j+1));
//					}
//				}
			}
		}
		
		while(!queue.isEmpty()) {
			Point p = queue.poll();
			int i = p.x; //i'm lazy, lol
			int j = p.y;
			if(!smallcityMap[i][j]) {
				boolean liedgent = i != 0;
				boolean hiedgent = i != 43;
				boolean ljedgent = j != 0;
				boolean hjedgent = j != 43;
				
				//Checks each opposite pair of cells around it
				if((liedgent && hiedgent) && (smallcityMap[i-1][j] && smallcityMap[i+1][j]))
					smallcityMap[i][j] = true;
				if((ljedgent && hjedgent) && (smallcityMap[i][j-1] && smallcityMap[i][j+1]))
					smallcityMap[i][j] = true;
				if(liedgent && hiedgent && ljedgent && hjedgent) {
						if (smallcityMap[i+1][j+1] && smallcityMap[i-1][j-1])
							smallcityMap[i][j] = true;
						if (smallcityMap[i-1][j+1] && smallcityMap[i+1][j-1])
							smallcityMap[i][j] = true;
				}
				
				//If value has changed, enqueue live neighbors
				if(smallcityMap[i][j]) {
					if(i > 0) if(!smallcityMap[i-1][j]) 
						queue.add(new Point(i-1,j));
					if(i < 43) if(!smallcityMap[i+1][j]) 
						queue.add(new Point(i+1,j));
					if(j > 0) if(!smallcityMap[i][j-1]) 
						queue.add(new Point(i,j-1));
					if(j < 43) if(!smallcityMap[i][j+1])
						queue.add(new Point(i,j+1));
				}
			}
		}
		
		System.out.println("Step 2:");
		for(int i = 0; i < 44; i++) {
			for(int j = 0; j < 44; j++) {
				System.out.print(smallcityMap[i][j] ? 1 : 0);
			}
			System.out.println();
		}
		
		byte[][] islandMap = new byte[44][44];
		LinkedList<Integer> islandSizeList = new LinkedList<Integer>();
		byte islandCounter = 1;
		marked = new boolean[44][44];
		
		for(int i = 0; i < 44; i++) {
			for(int j = 0; j < 44; j++) {
				if(islandMap[i][j] == 0 && !smallcityMap[i][j]) {
					queue.add(new Point(i,j));
					int size = 0;
					while(!queue.isEmpty()) {
						Point p = queue.poll();
						islandMap[p.x][p.y] = islandCounter;
						if(p.x != 0 && !marked[p.x-1][p.y] && !smallcityMap[p.x-1][p.y]) {
							queue.add(new Point(p.x-1,p.y)); 
							marked[p.x-1][p.y] = true; 
						}
						if(p.x != 43 && !marked[p.x+1][p.y] && !smallcityMap[p.x+1][p.y]) {
							queue.add(new Point(p.x+1,p.y));
							marked[p.x+1][p.y] = true; 
						}
						if(p.y != 0 && !marked[p.x][p.y-1] && !smallcityMap[p.x][p.y-1]) {
							queue.add(new Point(p.x,p.y-1));
							marked[p.x][p.y-1] = true;
						}
						if(p.y != 43 && !marked[p.x][p.y+1] && !smallcityMap[p.x][p.y+1]) {
							queue.add(new Point(p.x,p.y+1));
							marked[p.x][p.y+1] = true;
						}
						size++;
					}
					islandSizeList.add(size);
					islandCounter++;
					
					System.out.println(islandCounter-1);
					for(int k = 0; k < 44; k++) {
						for(int l = 0; l < 44; l++) {
							System.out.print(islandMap[k][l]);
						}
						System.out.println();
					}
				}
			}
		}
		
		//Finds largest island
		int maxSizeIndex = 0;
		for(int i = 1; i < islandSizeList.size(); i++)
				if(islandSizeList.get(i) > islandSizeList.get(maxSizeIndex))
						maxSizeIndex = i;
		
		//If island is not large enough, returns false: no city
		if(islandSizeList.get(maxSizeIndex) < 5) return false;
		
		//Sets all cells not in the island to true;
		for(int i = 0; i < 44; i++)
			for(int j = 0; j < 44; j++)
				if(!smallcityMap[i][j] && islandMap[i][j] != maxSizeIndex)
					smallcityMap[i][j] = true;
		
		System.out.println("Step 3:");
		for(int i = 0; i < 44; i++) {
			for(int j = 0; j < 44; j++) {
				System.out.print(smallcityMap[i][j] ? 1 : 0);
			}
			System.out.println();
		}
		
		cityMap = new boolean[176][176];
		
		for(int i = 0; i < 176; i++) 
			for(int j = 0; j < 176; j++) 
				cityMap[i][j] = !smallcityMap[i/4][j/4]; //I'll think of a more efficient way to handle the boolean map later
		
		if (biomeModeIndex == 12 /* desert */) return true;
		return false;
	}
	
	public static class Start extends StructureStart<NoFeatureConfig> {
		public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ,
				MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
			this.boundingBox = mutableBoundingBox;
		}
		
		//Need to rewrite
		@Override
		public void generatePieces(DynamicRegistries dynamicRegistryManager, ChunkGenerator generator,
				TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {
			
			this.getBoundingBox();
			
			//Rotation rotation = Rotation.NONE;
			
			int x = (chunkX << 4) - 80; //+ 7;
			int z = (chunkZ << 4) - 80; //+ 7;
			BiomeProvider biomesource = generator.getBiomeSource();
//			int y = generator.getBaseHeight(x, z, Heightmap.Type.WORLD_SURFACE);
			
			//Generate map. 176
//			Point[][] cityMap = new Point[176][176];
//			
//			//iterates by blocks of 4, fills in each point in cityMap with x = height, y = biome?
//			for (int i = 0; i < 88; i++) {
//				for (int j = 0; j < 88; j++) {
//					int xi = x+i*2;
//					int zj = z+j*2;
//					int height = generator.getFirstFreeHeight(xi, zj, Heightmap.Type.WORLD_SURFACE);
//					int biome = biomeMap.get(generator.getBiomeSource().getNoiseBiome(x/4+i/2, height, z/4+j/2).getBiomeCategory().toString());
//					cityMap[i*2][j*2] = new Point(height, biome);
//					cityMap[i*2+1][j*2] = new Point(height, biome);
//					cityMap[i*2][j*2+1] = new Point(height, biome);
//					cityMap[i*2+1][j*2+1] = new Point(height, biome);
//				}
//			}
			
			
		//Generate map. 88
			Point[][] shityMap = new Point[44][44];
			int[] heightModeList = new int[120];
			int[] biomeModeList = new int[17];
			for (int i = 0; i < 44; i++) {
				for (int j = 0; j < 44; j++) {
					int height = generator.getFirstFreeHeight(x+i*4, z+j*4, Heightmap.Type.WORLD_SURFACE);
					int biome = biomeMap.get(biomesource.getNoiseBiome(x/4+i, height, z/4+j).getBiomeCategory().toString());
					shityMap[i][j] = new Point(height, biome);
					biomeModeList[biome]++;
					if(height < 121) heightModeList[height]++;
				}
			}
			
			int heightModeIndex = 0;
			for(int i=0;i<heightModeList.length;i++) if (heightModeList[i] > heightModeList[heightModeIndex]) heightModeIndex = i;
			
			int biomeModeIndex = 0;
			for(int i=0;i<biomeModeList.length;i++) if (biomeModeList[i] > biomeModeList[biomeModeIndex]) biomeModeIndex = i;
			
//			byte[][] cityMap = FordFulkerson.placeCity(shityMap, 0);
			
			//Add pieces function???? Figure this out later
//			PolisProject.LOGGER.log(Level.DEBUG, "Biome is " + biomeIn.getBiomeCategory().toString() + ", " + generator.getBiomeSource().getNoiseBiome(x >> 2, y, z >> 2).getBiomeCategory().toString());
//			PolisProject.LOGGER.log(Level.DEBUG, "So the number I'm passing in is " + biomeMap.get(biomeIn.getBiomeCategory().toString()));
//			this.pieces.add(new AbstractCityManager.Piece(templateManagerIn, x, y, z, biomeMap.get(biomeIn.getBiomeCategory().toString())));
//			this.pieces.add(new AbstractCityManager.Piece(new BlockPos(x,y,z), chunkX, chunkZ, map));//CityHashMap.get(chunkX, chunkZ)));

			x += 87;
			z += 87;
			BlockPos blockpos = new BlockPos(x, generator.getFirstFreeHeight(x, z, Heightmap.Type.WORLD_SURFACE), z);
			DebugCityManager.height(heightModeIndex);
			DebugCityManager.map(cityMap);
			
//			int x = chunkX << 4;
//			int z = chunkZ << 4;
			int y = generator.getBaseHeight(x, z, Heightmap.Type.WORLD_SURFACE);
			DebugCityManager.generatePieces(templateManagerIn, new BlockPos(x, y, z), Rotation.NONE, this.pieces, random);
//			this.pieces.add(new AbstractCityManager.Piece(blockpos, chunkX, chunkZ, cityMap, height));
			
			this.calculateBoundingBox();
			
			System.out.println("Bounding box of first piece: ");
			System.out.println(this.pieces.get(0).getBoundingBox().toString());
			
		}
	}
}
