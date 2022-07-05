package com.iosenberg.polisproject.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class GeneralUtils {
	//Manhattan distance for A* heuristic
	public static int ManhattanDistance(ChunkPos a, ChunkPos b) {
		return Math.abs(a.x - b.x) + Math.abs(a.z - b.z);
	}
	
	public static int ManhattanDistance(BlockPos a, BlockPos b) {
		return Math.abs(a.getX() - b.getX()) + Math.abs(a.getZ() - b.getZ());
	}
}
