package com.iosenberg.polisproject.dimension;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;

import com.iosenberg.polisproject.PolisProject;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

//Based on https://github.com/TelepathicGrunt/WorldBlender/blob/1.16/src/main/java/com/telepathicgrunt/worldblender/dimension/WBWorldSavedData.java
//I'll make it a bit more personalized and relevant to the mod once I understand how WorldSavedData works lol
public class PPWorldSavedData extends WorldSavedData{
	private static final String ROAD_DATA = PolisProject.MODID + "RoadMap";
	private static final PPWorldSavedData CLIENT_DUMMY = new PPWorldSavedData();
	private Map<String,RoadChunk> map = new HashMap<>();
	
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
		wbAltarMade = data.getBoolean("WBALtarMade");
	}

	@Override
	public CompoundNBT save(CompoundNBT data) {
		data..putBoolean("WBAltarMade", wbAltarMade);
		return data;
	}

	
	public void put(Point chunk, Point chunkto, Point chunkfrom) {
		String key = chunk.x + "," + chunk.y;
		if(map.containsKey(key)) 
			map.get(key).add(chunkto, chunkfrom);
		else
			map.put(key, new RoadChunk(chunk, chunkto, chunkfrom));
	}
	
	public void put(Point chunk, Point chunkto, BlockPos startpos) {
		String key = chunk.x + "," + chunk.y;
		if(map.containsKey(key))
			map.get(key).add(chunkto, startpos);
		else
			map.put(key, new RoadChunk(chunk, chunkto, startpos));
	}
	
	
	public class RoadChunk {
		int x;
		int z;
		BlockPos start; //If this is the start of the road (will have no inbound chunk)
		Point[] chunks = {null, null, null, null}; //east X+, west X-, south Z+, north Z-
		
		RoadChunk(Point chunk, Point chunkto, Point chunkfrom) {
			x = chunk.x;
			z = chunk.y;
			chunks[getIndex(chunkto)] = chunkto;
			chunks[getIndex(chunkfrom)] = chunkfrom;
		}
			
		RoadChunk(Point chunk, Point chunkto, BlockPos startpos) {
			x = chunk.x;
			z = chunk.y;
			chunks[getIndex(chunkto)] = chunkto;
			start = startpos;
		}	
		
		int getIndex(Point chunkpos) {
			int chunkX = chunkpos.x;
			int chunkZ = chunkpos.y;
			if (chunkX > x) return 0;
			if (chunkX < x) return 1;
			if (chunkZ > z) return 2;
			if (chunkZ < z) return 3;
			PolisProject.LOGGER.log(Level.DEBUG, "Uh oh!, trying to move to itself!");
			return -1;
		}
		
		void add(Point chunkto, Point chunkfrom) {
			if(chunks[getIndex(chunkto)] == null)
				chunks[getIndex(chunkto)] = (chunkto);
			if(chunks[getIndex(chunkfrom)] == null)
				chunks[getIndex(chunkfrom)] = (chunkfrom);
		}
		
		void add(Point chunkto, BlockPos startpos) {
			if(chunks[getIndex(chunkto)] == null)
				chunks[getIndex(chunkto)] = chunkto;
			start = startpos;
		}
	}
}
