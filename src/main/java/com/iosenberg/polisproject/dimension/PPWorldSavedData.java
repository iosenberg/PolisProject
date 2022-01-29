package com.iosenberg.polisproject.dimension;

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
	
	/**
	 * Places a new city NBT in World Saved Data
	 * 
	 * @param chunk - the chunk of the City structure. Key for a city in WorldSavedData is chunk.toString()
	 * @param height - the height of the floor of the city stored as (realHeight - Byte.MAX_VALUE)
	 * @param biome - the enumerated biome category of the city
	 * @param map - a 2 dimensional array of booleans, each representing whether the matching BlockPos should be part of the City structure
	 * @param anchors - an array of BlockPos holding the locations of anchors within the city
	 */
	public static void putCity(ChunkPos chunk, byte height, byte biome, byte[] map, long[] anchors) {
		CompoundNBT newCity = new CompoundNBT();

		newCity.putByte("height", height);
		newCity.putByte("biome", biome);
		newCity.putByteArray("map", map);
		newCity.putLongArray("anchors", anchors);
		
		cityMap.put(chunk.toString(), newCity);
	}
	
	public static CompoundNBT getCity(ChunkPos chunk) {
		CompoundNBT city = cityMap.getCompound(chunk.toString());
		cityMap.remove(chunk.toString());
		return city;
	}
	
	/**
	 * Places a new road NBT in World Saved Data, along with calculated direction of adjacent roads
	 * Notes that East = +x, West = -x, South = +z, North = -z
	 * 
	 * @param chunk - the chunk of Road feature. Key for a road in WorldSavedData is chunk.toString()
	 * @param previousChunk - an adjacent chunk
	 */
	public static void putRoad(ChunkPos chunk, ChunkPos previousChunk) {
		int x = chunk.x;
		int z = chunk.z;
		Boolean East = false;
		Boolean West = false;
		Boolean South = false;
		Boolean North = false;
		if (previousChunk.x > x) East = true;
		if (previousChunk.x < x) West = true;
		if (previousChunk.z > z) South = true;
		if (previousChunk.z < z) North = true;
		String key = chunk.toString();
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
	
	public static CompoundNBT getRoad(ChunkPos chunk) {
		String key = chunk.toString();
		if(!roadMap.contains(key)) return null;
		CompoundNBT road = roadMap.getCompound(key);
		roadMap.remove(key);
		return road;
	}
	
	public static boolean containsRoad(ChunkPos chunk) {
		return(roadMap.contains(chunk.toString()));
	}
}
