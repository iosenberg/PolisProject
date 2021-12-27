package com.iosenberg.polisproject.structure;

import java.util.Map;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableMap;
import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.structure.city.AbstractCityManager;
import com.mojang.serialization.Codec;

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

	public CityStructure(Codec<NoFeatureConfig> codec) {
		super(codec);
	}
	
	@Override
	public IStartFactory<NoFeatureConfig> getStartFactory() {
		return CityStructure.Start::new;
	}
	
	@Override
	public GenerationStage.Decoration step() {
		return GenerationStage.Decoration.SURFACE_STRUCTURES;
	}
	
	//Need to rewrite
	@Override
	protected boolean isFeatureChunk(ChunkGenerator generator, BiomeProvider biomeProviderIn, long seed,
			SharedSeedRandom seedRand, int chunkX, int chunkZ, Biome biomeIn, ChunkPos chunkPos,
			NoFeatureConfig config) {
		//maybe check standard deviation of the height of blocks????
		//Also make sure it doesn't spawn too close to a village, or anything else that might cause a big overlap
		return true;
	}
	
	public static class Start extends StructureStart<NoFeatureConfig> {
		public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ,
				MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}
		
		//Need to rewrite
		@Override
		public void generatePieces(DynamicRegistries dynamicRegistryManager, ChunkGenerator generator,
				TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {
			
			//Rotation rotation = Rotation.NONE;
			
			int x = (chunkX << 4) + 7;
			int z = (chunkZ << 4) + 7;
			
			int y = generator.getBaseHeight(x, z, Heightmap.Type.WORLD_SURFACE);
			
			//Add pieces function???? Figure this out later
			PolisProject.LOGGER.log(Level.DEBUG, "Biome is " + biomeIn.getBiomeCategory().toString() + ", " + generator.getBiomeSource().getNoiseBiome(x >> 2, y, z >> 2).getBiomeCategory().toString());
			PolisProject.LOGGER.log(Level.DEBUG, "So the number I'm passing in is " + biomeMap.get(biomeIn.getBiomeCategory().toString()));
			this.pieces.add(new AbstractCityManager.Piece(templateManagerIn, x, y, z, biomeMap.get(biomeIn.getBiomeCategory().toString())));
//			this.pieces.add(new AbstractCityManager.Piece(new BlockPos(x,y,z), chunkX, chunkZ, map));//CityHashMap.get(chunkX, chunkZ)));
			this.calculateBoundingBox();
		}
	}
}
