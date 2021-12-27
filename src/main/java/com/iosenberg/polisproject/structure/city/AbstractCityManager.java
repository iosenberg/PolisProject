package com.iosenberg.polisproject.structure.city;

import java.util.Random;

import org.apache.logging.log4j.Level;

import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.init.PPStructures;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

public abstract class AbstractCityManager {
	
	public abstract byte[][] generateMap(ChunkGenerator generator, int chunkX, int chunkZ);
	
	public abstract int generatePieces();
	
	//CityPiece, which is inhereted by all city managers
	public static class Piece extends StructurePiece {
		//Map of colored blocks which corrsponds to CityStructure.biomeMap. For debug purposes
		private static final BlockState[] colorMap= {
			Blocks.BLACK_STAINED_GLASS.defaultBlockState(),			//none
			Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState(),	//taiga
			Blocks.GRAY_STAINED_GLASS.defaultBlockState(),			//extreme hills
			Blocks.GREEN_STAINED_GLASS.defaultBlockState(),			//jungle
			Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),		//mesa
			Blocks.LIME_STAINED_GLASS.defaultBlockState(),			//plains
			Blocks.BROWN_STAINED_GLASS.defaultBlockState(),			//savanna
			Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),	//icy
			Blocks.BLACK_STAINED_GLASS.defaultBlockState(),			//the end
			Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),		//beach
			Blocks.GREEN_STAINED_GLASS.defaultBlockState(),			//forest
			Blocks.CYAN_STAINED_GLASS.defaultBlockState(),			//ocean
			Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),		//desert
			Blocks.BLUE_STAINED_GLASS.defaultBlockState(),			//river
			Blocks.GREEN_STAINED_GLASS.defaultBlockState(),			//swamp
			Blocks.RED_STAINED_GLASS.defaultBlockState(),			//mushroom
			Blocks.BLACK_STAINED_GLASS.defaultBlockState()			//nether
		};
		
		public int biome;
	
		public Piece(TemplateManager templateManagerIn, CompoundNBT tagCompound) {
			super(PPStructures.CITY_PIECE, tagCompound);
		}
		
		//Temporary for debug purposes
		public Piece(TemplateManager templateManagerIn, int x, int y, int z, int biomeInt) {
			super(PPStructures.CITY_PIECE, 0);
			this.boundingBox = new MutableBoundingBox(x-90, y-30, z-90,
					x + 90, y+30, z + 90);
			this.biome = biomeInt;
			PolisProject.LOGGER.log(Level.DEBUG, "Biome is " + this.biome);
		}

		@Override
		protected void addAdditionalSaveData(CompoundNBT tagCompound) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean postProcess(ISeedReader worldIn, StructureManager structureManager,
				ChunkGenerator chunkGenerator, Random rand, MutableBoundingBox mbb, ChunkPos chunkPos,
				BlockPos blockPos) {

			int x = chunkPos.x << 4;
			int z = chunkPos.z << 4;
			int y = 100;//blockPos.getY() + 10;
			PolisProject.LOGGER.log(Level.DEBUG, "Biomeint is " + biome + " so the block tower is " + colorMap[biome].toString());
			
			for (int i = 5; i < 100; i++) {
				this.placeBlock(worldIn, colorMap[biome], x, i, z, this.boundingBox);
				this.placeBlock(worldIn, colorMap[biome], blockPos.getX(), i, blockPos.getZ(), this.boundingBox);
			}
			
			return false;
		}
	}
}
