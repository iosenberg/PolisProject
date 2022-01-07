package com.iosenberg.polisproject.structure;

import java.util.Random;

import com.iosenberg.polisproject.dimension.PPWorldSavedData;
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
	public RoadFeature(Codec<NoFeatureConfig> configFactory) {
		super(configFactory);
	}
	
	public static void generateRoads(ChunkPos source, ChunkPos destination, ChunkPos[] illegalChunks, ChunkGenerator generator) {
		final int xMax = Math.abs(source.x - destination.x);
		final int zMax = Math.abs(source.z - destination.z);
		ChunkPos s; //i,j form of source and destination
		ChunkPos d;
		ChunkPos offset; //world location of (0,0)
		if (source.x < destination.x) { //x == i; j == z;
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
		for(int i = 0; i < xMax; i++) {
			for(int j = 0; j < zMax; j++) {
				distance[i][j] = Integer.MAX_VALUE;
			}
		}
		
		//mnyeh, I don't feel like implementing a priority queue rn
	}

	@Override
	public boolean place(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos,
			NoFeatureConfig config) {
		String key = (pos.getX() >> 4) + "," + (pos.getZ() >> 4);
		CompoundNBT roadNBT = PPWorldSavedData.getRoad(key);
		if(roadNBT == null) return false;
		BlockPos.Mutable blockpos$Mutable = new BlockPos.Mutable();//.setPos(new BlockPos(x,y,z));
		for (int i=0;i<16;i++)
			for (int j=0;j<16;j++) {
				blockpos$Mutable.setX(pos.getX() + i);
				blockpos$Mutable.setZ(pos.getZ() + j);
				blockpos$Mutable.setY(generator.getBaseHeight(blockpos$Mutable.getX(), blockpos$Mutable.getZ(), Heightmap.Type.WORLD_SURFACE));
				reader.setBlock(blockpos$Mutable, Blocks.BLACK_WOOL.defaultBlockState(), 4);
			}	
		return true;
	}

}
