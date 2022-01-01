package com.iosenberg.polisproject.structure;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableMap;
import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.structure.city.AbstractCityManager;
import com.iosenberg.polisproject.structure.city.AbstractCityManager.Piece;
import com.iosenberg.polisproject.structure.city.DebugCityManager;
import com.iosenberg.polisproject.structure.city.FordFulkerson;
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
		return GenerationStage.Decoration.TOP_LAYER_MODIFICATION;
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
		
//		boolean notdone = true;
//		int counter = 1;
//		while(notdone) {
//			int min = 22-counter;
//			int max = 21+counter;
//			for(int i = 0; i < counter; i++) {
//				if(Math.abs(pointArray[min][i].x - height) < 5 && pointArray[min][i].y == biomeModeIndex) {
//					queue.add(new Point(min,i));
//					marked[min][i] = true;
//					notdone = false;
//				}
//				if(Math.abs(pointArray[i][max].x - height) < 5 && pointArray[i][max].y == biomeModeIndex) {
//					queue.add(new Point(i, max));
//					marked[i][max] = true;
//					notdone = false;
//				}
//				if(Math.abs(pointArray[max][max-i].x - height) < 5 && pointArray[max][max-i].y == biomeModeIndex) {
//					queue.add(new Point(max,max-i));
//					marked[max][max-i] = true;
//					notdone = false;
//				}
//				if(Math.abs(pointArray[max-i][min].x - height) < 5 && pointArray[max-i][min].y == biomeModeIndex) {
//					queue.add(new Point(max-i, min));
//					marked[max-i][min] = true;
//					notdone = false;
//				}
//			}
//			counter++;
//		}
		
		
		
		for(int i = 0; i < 44; i++) {
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
				if(p.x > 0) if(!marked[p.x-1][p.y]) {
					queue.add(new Point(p.x-1,p.y));
					marked[p.x-1][p.y] = true; 
				}
				if(p.x < 43) if(!marked[p.x+1][p.y]) {
					queue.add(new Point(p.x+1,p.y));
					marked[p.x+1][p.y] = true; 
				}
				if(p.y > 0) if(!marked[p.x][p.y-1]) {
					queue.add(new Point(p.x,p.y-1));
					marked[p.x][p.y-1] = true; 
				}
				if(p.y < 43) if(!marked[p.x][p.y+1]) {
					queue.add(new Point(p.x,p.y+1));
					marked[p.x][p.y+1] = true; 
				}
			}
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
