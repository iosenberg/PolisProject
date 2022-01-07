package com.iosenberg.polisproject.dimension;

import java.awt.Point;

import com.iosenberg.polisproject.PolisProject;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

//Based on https://github.com/TelepathicGrunt/WorldBlender/blob/1.16/src/main/java/com/telepathicgrunt/worldblender/dimension/WBWorldSavedData.java
//I'll make it a bit more personalized and relevant to the mod once I understand how WorldSavedData works lol
public class PPWorldSavedData extends WorldSavedData{
	private static final String ROAD_DATA = PolisProject.MODID + "RoadMap";
	private static final PPWorldSavedData CLIENT_DUMMY = new PPWorldSavedData();
	private static CompoundNBT roadMap = new CompoundNBT(); //A map of Roads. 
	private static CompoundNBT cityMap = new CompoundNBT(); //A map of cities.
	//Road is an NBT with the key "x,y", and Boolean East (x+), West (x-), South (z+), North (z-)
	
	public PPWorldSavedData() {
		super(ROAD_DATA);
	}
	
	public static PPWorldSavedData get(World world) {
		if (!(world instanceof ServerWorld)) {
			return CLIENT_DUMMY;
		}
		
		DimensionSavedDataManager storage = ((ServerWorld) world).getDataStorage();
		return storage.computeIfAbsent(PPWorldSavedData::new, ROAD_DATA);
	}

	@Override
	public void load(CompoundNBT data) {
		roadMap = (CompoundNBT) data.get("roadMap");
	}

	@Override
	public CompoundNBT save(CompoundNBT data) {
		data.put("RoadMap", roadMap);
		data.put("CityMap", cityMap);
		return data;
	}
	
	public static void putCity(int chunkX, int chunkZ, byte height, byte biome, byte[] map, long[] anchors) {
		CompoundNBT newCity = new CompoundNBT();

		newCity.putByte("height", height);
		newCity.putByte("biome", biome);
		newCity.putByteArray("map", map);
		newCity.putLongArray("anchors", anchors); //stores blockpos of anchors
		
		cityMap.put(chunkX + "," + chunkZ, newCity);
	}
	
	public static CompoundNBT getCity(int chunkX, int chunkZ) {
		CompoundNBT city = cityMap.getCompound(chunkX + "," + chunkZ);
		cityMap.remove(chunkX + "," + chunkZ);
		return city;
	}
	
	public static void putRoad(int chunkX, int chunkZ) {
		CompoundNBT road = new CompoundNBT();
		roadMap.put(chunkX + "," + chunkZ, road);
	}

	//Puts a RoadNBT into roadMap. Key is "x,y" of the chunk, and it contains a boolean for each direction, whether it connects to a road
	//These functions need to be cleaned up a lot. Need to stop using Points and need to fix y to z
	public void put(Point chunk, Point chunkto, Point chunkfrom) {
		int x = chunk.x;
		int y = chunk.y;
		Boolean East = false;
		Boolean West = false;
		Boolean South = false;
		Boolean North = false;
		if (chunkto.x > x || chunkfrom.x > x) East = true;
		if (chunkto.x < x || chunkfrom.x < x) West = true;
		if (chunkto.y > y || chunkfrom.y > y) South = true;
		if (chunkto.y < y || chunkfrom.y < y) North = true;
		String key = chunk.x + "," + chunk.y;
		if(roadMap.contains(key)) {
			roadMap.getCompound(key).putBoolean("East", roadMap.getCompound(key).getBoolean("East") || East);
			roadMap.getCompound(key).putBoolean("West", roadMap.getCompound(key).getBoolean("West") || West);
			roadMap.getCompound(key).putBoolean("South", roadMap.getCompound(key).getBoolean("South") || South);
			roadMap.getCompound(key).putBoolean("North", roadMap.getCompound(key).getBoolean("North") || North);
	}
		else {
			CompoundNBT newRoad = new CompoundNBT();
			newRoad.putBoolean("East", East);
			newRoad.putBoolean("West", West);
			newRoad.putBoolean("South", South);
			newRoad.putBoolean("North", North);
			roadMap.put(key, newRoad);
		}
	}
	
	public static CompoundNBT getRoad(String key) {
		if(!roadMap.contains(key)) return null;
		CompoundNBT road = roadMap.getCompound(key);
		roadMap.remove(key);
		return road;
	}
	
	//I don't think I need this, but I might, so I'm just commenting it out for now
//	public void put(Point chunk, Point chunkto, BlockPos startpos) {}
}
