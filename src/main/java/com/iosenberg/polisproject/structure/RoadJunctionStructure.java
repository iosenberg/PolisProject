package com.iosenberg.polisproject.structure;

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

public class RoadJunctionStructure extends Structure<NoFeatureConfig>{
	public RoadJunctionStructure(Codec<NoFeatureConfig> codec) {
		super(codec);
	}
	
	@Override
	public IStartFactory<NoFeatureConfig> getStartFactory() {
		return RoadJunctionStructure.Start::new;
	}
	
	@Override
	public GenerationStage.Decoration step() {
		return GenerationStage.Decoration.UNDERGROUND_STRUCTURES;//don't remember why this one. Need to read up again on what happens where
	}
	
	@Override
	protected boolean isFeatureChunk(ChunkGenerator generator, BiomeProvider biomeProviderIn, long seed,
			SharedSeedRandom seedRand, int chunkX, int chunkZ, Biome biomeIn, ChunkPos chunkPos,
			NoFeatureConfig config) {
		ChunkPos cityChunk = Structure.VILLAGE.getPotentialFeatureChunk(generator.getSettings().getConfig(Structure.VILLAGE), seed, seedRand, chunkX, chunkZ);
		if(Math.sqrt((cityChunk.x - chunkX)^2 + (cityChunk.z - chunkZ)^2) < 40) return false;
		String biome = biomeIn.getBiomeCategory().toString();
		if(biome.equals("OCEAN") || biome.equals("EXTREME_HILLS")) return false;
		//check to make sure not too close to city or village
		//check to make sure not in illegal biome, like ocean or river (or mountain?)
		return true;
	}
	
	public static class Start extends StructureStart<NoFeatureConfig> {
		public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ, MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }
		
		@Override
		public void generatePieces(DynamicRegistries dynamicRegistryManager, ChunkGenerator generator,
				TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn,
				NoFeatureConfig config) {

			Rotation rotation = Rotation.NONE;
			int x = (chunkX << 4) + 7;
			int z = (chunkZ << 4) + 7;
			int y = generator.getBaseHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG);
			BlockPos blockpos = new BlockPos(x, y, z);
			
			RoadJunctionStructurePiece.start(templateManagerIn, blockpos, rotation, this.pieces, this.random);
			
			this.calculateBoundingBox();
		}
	}
}
