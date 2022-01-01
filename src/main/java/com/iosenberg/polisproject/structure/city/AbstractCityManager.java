package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;

import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.init.PPStructures;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

public abstract class AbstractCityManager {
	
//	public abstract byte[][] generateMap(ChunkGenerator generator, int chunkX, int chunkZ);
	
//	public abstract void generatePieces(TemplateManager templateManager, BlockPos pos, Rotation rotation, List<StructurePiece> pieceList, Random random);
	
	//CityPiece, which is inherited by all city managers
	public static class Piece extends StructurePiece {
		//Map of colored blocks which corresponds to CityStructure.biomeMap. For debug purposes
		private static final BlockState[] biomeColorMap= {
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
		
		//Map of colored blocks corrosponding to whatever
		private static final BlockState[] indexColorMap= {
			Blocks.WHITE_STAINED_GLASS.defaultBlockState(),
			Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),
			Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(),
			Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),
			Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
			Blocks.LIME_STAINED_GLASS.defaultBlockState(),
			Blocks.PINK_STAINED_GLASS.defaultBlockState(),
			Blocks.GRAY_STAINED_GLASS.defaultBlockState(),
			Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState(),
			Blocks.CYAN_STAINED_GLASS.defaultBlockState(),
			Blocks.PURPLE_STAINED_GLASS.defaultBlockState(),
			Blocks.BLUE_STAINED_GLASS.defaultBlockState(),
			Blocks.BROWN_STAINED_GLASS.defaultBlockState(),
			Blocks.GREEN_STAINED_GLASS.defaultBlockState(),
			Blocks.RED_STAINED_GLASS.defaultBlockState(),
			Blocks.BLACK_STAINED_GLASS.defaultBlockState()
		};
		
		//all of these can be static for now, but I will NEED to store them elsewhere before seriously building this mod
		public static int biome;
		public static int height;
		private static boolean[][][][] map = new boolean[11][11][16][16]; //maybe use a custom class instead of point?
		private static int chunkX;
		private static int chunkZ;
		
		
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
		
		//Constructor for map
		public Piece(BlockPos blockPos, int chunkXin, int chunkZin, boolean[][] mapIn, int heightIn) {
			super(PPStructures.CITY_PIECE, 0);
			this.boundingBox = new MutableBoundingBox(blockPos.getX()-90, blockPos.getY()-30, blockPos.getZ()-90,
					blockPos.getX() + 90, blockPos.getY()+30, blockPos.getZ() + 90);
			
			chunkX = chunkXin;
			chunkZ = chunkZin;
			
			for(int i=0;i<176;i++) {
				for (int j=0;j<176;j++) {
					map[i/16][j/16][i%16][j%16] = mapIn[i/8][j/8];
					map[i/16][j/16][i%16][j%16] = mapIn[i][j];
				}
			}
			
			height = heightIn;
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
			
			//Iterates through chunks in map, centered around chunkX and chunkZ
			for(int i=0;i<11;i++) {
				for(int j=0;j<11;j++) {
					//If chunkX+i-5, chunkZ+j-5 is the input chunk, it is the Selected Chunk
					if((chunkPos.x == chunkX+i-5) && (chunkPos.z == chunkZ+j-5)) {
						//Once selected chunk is found, iterates through blocks in chunk, applying info from map
						for(int k=0;k<16;k++) {
							for(int l=0;l<16;l++) {
//								int y = map[i][j][k][l].x + 5;
//								BlockState color = biomeColorMap[map[i][j][k][l].y];
								if(map[i][j][k][l]) {
									int y = chunkGenerator.getFirstFreeHeight(x+k, z+l, Heightmap.Type.OCEAN_FLOOR);//map[i][j][k][l];
									BlockState color = indexColorMap[y != 5 ? 14 : 0];
								
									for(int m = y; m < height; m++) 
										this.placeBlock(worldIn, Blocks.SAND.defaultBlockState(), x+k, m, z+l, this.boundingBox);
									for(int m = height; m < y; m++) 
										this.placeBlock(worldIn, Blocks.AIR.defaultBlockState(), x+k, m, z+l, this.boundingBox);
									
//									this.placeBlock(worldIn, color, x+k, y-1, z+l, this.boundingBox);
//									if(y>50)
//										for(int m=0;m<worldIn.getHeight(Heightmap.Type.MOTION_BLOCKING, x+k, z+l);m++) 
//											this.placeBlock(worldIn, Blocks.AIR.defaultBlockState(), x+k, y+m, z+l, this.boundingBox);
								}
							}
						}
						
						//Returns true that selected chunk was changed
						return true;
					}
				}
			}
			
			return true; //I think I can save this to false
		}
	}
}
