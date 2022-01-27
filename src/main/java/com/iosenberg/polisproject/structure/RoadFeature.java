package com.iosenberg.polisproject.structure;

import java.util.PriorityQueue;
import java.util.Random;

import com.iosenberg.polisproject.dimension.PPWorldSavedData;
import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
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
	
	public static void generateRoads(ChunkPos source, ChunkPos destination, ChunkPos[] illegalChunks, ChunkGenerator generator) {
		final int xMax = Math.abs(source.x - destination.x);
		final int zMax = Math.abs(source.z - destination.z);
		ChunkPos s; //i,j form of source and destination
		ChunkPos d;
		ChunkPos offset; //world location of (0,0)
		if (source.x < destination.x) { //i == x; j == z;
			if (source.z < destination.z) {
				s = new ChunkPos(0, 0);
				d = new ChunkPos(xMax - 1, zMax - 1);
				offset = source;
			}
			else {
				s = new ChunkPos(0, zMax - 1);
				d = new ChunkPos(xMax - 1, 0);
				offset = new ChunkPos(source.x, destination.z);
			}
		}
		else {
			if (source.z < destination.z) {
				s = new ChunkPos(xMax - 1, 0);
				d = new ChunkPos(0, zMax - 1);
				offset = new ChunkPos(destination.x, source.z);
			}
			else {
				s = new ChunkPos(xMax - 1, zMax - 1);
				d = new ChunkPos(0,0);
				offset = destination;
			}
		}

		byte[][] stDevMap = new byte[xMax][zMax];
		int[][] meanMap = new int[xMax][zMax];
		
		for(int i = 0; i < xMax; i++) {
			for(int j = 0; j < zMax; j++) {
				int x = (offset.x + i) << 4;
				int z = (offset.z + j) << 4;
				int mean = 0;
				int maxHeight = 0;
				int minHeight = Integer.MAX_VALUE;
				for(int k = x; k < x + 16; k += 4) {
					for(int l = z; l < z + 16; l += 4) {
						int height = generator.getBaseHeight(k, l, Heightmap.Type.WORLD_SURFACE);
						mean += height;
						if(height > maxHeight) maxHeight = height;
						if(height < minHeight) minHeight = height;
					}
				}
				stDevMap[i][j] = (byte)(maxHeight-minHeight);
				meanMap[i][j] = mean/16;
			}
		}
		
		//Run a variant of Dijkstra's algorithm
		boolean[][] visited = new boolean[xMax][zMax];
		int[][] distance = new int[xMax][zMax];
		ChunkPos[][] previousChunk = new ChunkPos[xMax][zMax];
		for(int i = 0; i < xMax; i++) {
			for(int j = 0; j < zMax; j++) {
				distance[i][j] = Integer.MAX_VALUE;
			}
		}
		
		//This implementation feels incredibly gross, but Java's implementation of priority queue is gross so oh well
		PriorityQueue<ChunkPos> queue = new PriorityQueue<ChunkPos>((a, b) -> distance[a.x][a.z] - distance[b.x][b.z]);
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
				int tempDist = stDevMap[tx][tz] + Math.abs(meanMap[tx][tz] - meanMap[pos.x][pos.z]) + distance[pos.x][pos.z] + 1;
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
				int tempDist = stDevMap[tx][tz] + Math.abs(meanMap[tx][tz] - meanMap[pos.x][pos.z]) + distance[pos.x][pos.z] + 1;
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
				int tempDist = stDevMap[tx][tz] + Math.abs(meanMap[tx][tz] - meanMap[pos.x][pos.z]) + distance[pos.x][pos.z] + 1;
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
				int tempDist = stDevMap[tx][tz] + Math.abs(meanMap[tx][tz] - meanMap[pos.x][pos.z]) + distance[pos.x][pos.z] + 1;
				if(tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx,tz);
					if(!queue.contains(chunk)) queue.offer(chunk);
				}
			}
			
		}
		
		//Finished algorithm. The path is stored in previousChunk[]
		ChunkPos chunk = d;
		while(!chunk.equals(s)) {
			ChunkPos offsetChunk = new ChunkPos(chunk.x + offset.x, chunk.z + offset.z);
			ChunkPos offsetPreviousChunk = new ChunkPos(previousChunk[chunk.x][chunk.z].x + offset.x, previousChunk[chunk.x][chunk.z].z + offset.z);
			PPWorldSavedData.putRoad(offsetChunk, offsetPreviousChunk); //add road in both directions
			PPWorldSavedData.putRoad(offsetPreviousChunk, offsetChunk);
			System.out.println((chunk.x + offset.x) + "," + (chunk.z + offset.z) + " : " + distance[chunk.x][chunk.z]);
			chunk = previousChunk[chunk.x][chunk.z];
		}
		
		//Remove any illegal chunks from the road
		for(int i = 0; i < illegalChunks.length; i++) {
			PPWorldSavedData.getRoad(illegalChunks[i]);
		}
	}

	@Override
	public boolean place(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos,
			NoFeatureConfig config) {
		CompoundNBT roadNBT = PPWorldSavedData.getRoad(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
		if(roadNBT == null) return false;
		
		System.out.println("uh yuhhhh: " + (pos.getX() >> 4) + "," + (pos.getZ() >> 4));
		int centerX = pos.getX() + 7;
		int centerZ = pos.getZ() + 7;
		BlockPos.Mutable blockpos$Mutable = new BlockPos.Mutable();
		blockpos$Mutable.setX(centerX);
		blockpos$Mutable.setZ(centerZ);
		blockpos$Mutable.setY(generator.getBaseHeight(blockpos$Mutable.getX(), blockpos$Mutable.getZ(), Heightmap.Type.WORLD_SURFACE));
		reader.setBlock(blockpos$Mutable, Blocks.BLACK_WOOL.defaultBlockState(), 4);
		
		if(roadNBT.getBoolean("East")) {
			for(int i = 0; i < 7; i++) {
				blockpos$Mutable.move(Direction.EAST);
				blockpos$Mutable.setY(generator.getBaseHeight(blockpos$Mutable.getX(), blockpos$Mutable.getZ(), Heightmap.Type.WORLD_SURFACE));
				reader.setBlock(blockpos$Mutable, Blocks.BLACK_WOOL.defaultBlockState(), 4);
			}
		}
		if(roadNBT.getBoolean("West")) {
			blockpos$Mutable.setX(centerX);
			blockpos$Mutable.setZ(centerZ);
			
			for(int i = 0; i < 7; i++) {
				blockpos$Mutable.move(Direction.WEST);
				blockpos$Mutable.setY(generator.getBaseHeight(blockpos$Mutable.getX(), blockpos$Mutable.getZ(), Heightmap.Type.WORLD_SURFACE));
				reader.setBlock(blockpos$Mutable, Blocks.BLACK_WOOL.defaultBlockState(), 4);
			}
		}
		if(roadNBT.getBoolean("South")) {
			blockpos$Mutable.setX(centerX);
			blockpos$Mutable.setZ(centerZ);
			
			for(int i = 0; i < 7; i++) {
				blockpos$Mutable.move(Direction.SOUTH);
				blockpos$Mutable.setY(generator.getBaseHeight(blockpos$Mutable.getX(), blockpos$Mutable.getZ(), Heightmap.Type.WORLD_SURFACE));
				reader.setBlock(blockpos$Mutable, Blocks.BLACK_WOOL.defaultBlockState(), 4);
			}
		}
		if(roadNBT.getBoolean("North")) {
			blockpos$Mutable.setX(centerX);
			blockpos$Mutable.setZ(centerZ);
			
			for(int i = 0; i < 7; i++) {
				blockpos$Mutable.move(Direction.NORTH);
				blockpos$Mutable.setY(generator.getBaseHeight(blockpos$Mutable.getX(), blockpos$Mutable.getZ(), Heightmap.Type.WORLD_SURFACE));
				reader.setBlock(blockpos$Mutable, Blocks.BLACK_WOOL.defaultBlockState(), 4);
			}
		}
		
		return true;
	}

}
