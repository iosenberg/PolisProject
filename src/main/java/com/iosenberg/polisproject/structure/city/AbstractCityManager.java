package com.iosenberg.polisproject.structure.city;

import java.util.Random;

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
			Blocks.BLACK_STAINED_GLASS.defaultBlockState(),
			Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState(),
			Blocks.GRAY_STAINED_GLASS.defaultBlockState(),
			Blocks.GREEN_STAINED_GLASS.defaultBlockState(),
			Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),
			Blocks.LIME_STAINED_GLASS.defaultBlockState(),
			Blocks.BROWN_STAINED_GLASS.defaultBlockState(),
			Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),
			Blocks.BLACK_STAINED_GLASS.defaultBlockState(),
			Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
			Blocks.GREEN_STAINED_GLASS.defaultBlockState(),
			Blocks.CYAN_STAINED_GLASS.defaultBlockState(),
			Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
			Blocks.BLUE_STAINED_GLASS.defaultBlockState(),
			Blocks.GREEN_STAINED_GLASS.defaultBlockState(),
			Blocks.RED_STAINED_GLASS.defaultBlockState(),
			Blocks.BLACK_STAINED_GLASS.defaultBlockState()
		};
	
		public Piece(TemplateManager templateManagerIn, CompoundNBT tagCompound) {
			super(PPStructures.CITY_PIECE, tagCompound);
		}
		
		//Temporary for debug purposes
		public Piece(TemplateManager templateManagerIn, int x, int y, int z) {
			super(PPStructures.CITY_PIECE, 0);
			this.boundingBox = new MutableBoundingBox(x-90, y-30, z-90,
					x + 90, y+30, z + 90);
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
			int y = 70;
			
			this.placeBlock(worldIn, colorMap[3], x, y, z, this.boundingBox);
			this.placeBlock(worldIn, colorMap[4], blockPos.getX(), y, blockPos.getZ(), this.boundingBox);
			
			return false;
		}
	}
}
