package com.iosenberg.polisproject.structure.city;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.init.PPStructures;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class AbstractCityPieces {

	static final int OFFSET = 80;

	/*
	 * generates walls, fills in appropriate spaces on the map, then populates
	 * wallsList with a list of BlockPoses at which to place walls
	 */
	static byte[][] generateWalls(byte[][] mapIn, List<StructurePiece> pieceList, BlockPos pos,
			TemplateManager templateManager, ArrayList<BlockPos> wallsList) {
		// Reduce wall map to 35x35 (~176 block array in 5 block chunks)
		final int WALL_SIZE = 5;
		final int WALL_MAX = 176 / WALL_SIZE;

		boolean[][] map = new boolean[WALL_MAX][WALL_MAX];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				int x = i * WALL_SIZE;
				int z = j * WALL_SIZE;
				boolean zero = false;
				for (int k = x; k < x + WALL_SIZE; k++) {
					for (int l = z; l < z + WALL_SIZE; l++) {
						if (mapIn[k][l] == 0)
							zero = true;
					}
				}
				map[i][j] = !zero;
			}
		}

		// Algorithm for wall placement
		for (int i = 0; i < WALL_MAX / 2 + 1; i++) {

			// Checks row i in x
			int oneCounter = 0;
			for (int j = 0; j < WALL_MAX; j++) {
				int x = i;
				int z = j;
				if (map[x][z])
					oneCounter++;
				else {
					if (oneCounter > 0 && oneCounter < 4) {
						for (int k = 1; k <= oneCounter; k++) {
							map[x][z - k] = false;
						}

					}
					oneCounter = 0;
				}
			}

			// Checks row WALLMAX - i in x
			oneCounter = 0;
			for (int j = 0; j < WALL_MAX; j++) {
				int x = WALL_MAX - i - 1;
				int z = j;
				if (map[x][z])
					oneCounter++;
				else {
					if (oneCounter > 0 && oneCounter < 4) {

						for (int k = 1; k <= oneCounter; k++) {
							map[x][z - k] = false;
						}
					}
					oneCounter = 0;
				}
			}

			// Checks row i in z
			oneCounter = 0;
			for (int j = 0; j < WALL_MAX; j++) {
				int x = j;
				int z = i;
				if (map[x][z])
					oneCounter++;
				else {
					if (oneCounter > 0 && oneCounter < 4) {

						for (int k = 1; k <= oneCounter; k++) {
							map[x - k][z] = false;
						}
					}
					oneCounter = 0;
				}
			}

			// Checks row WALLMAX - i in z
			oneCounter = 0;
			for (int j = 0; j < WALL_MAX; j++) {
				int x = j;
				int z = WALL_MAX - i - 1;
				if (map[x][z])
					oneCounter++;
				else {
					if (oneCounter > 0 && oneCounter < 4) {

						for (int k = 1; k <= oneCounter; k++) {
							map[x - k][z] = false;
						}

					}
					oneCounter = 0;
				}
			}
		}
		// END algorithm

		// Begin placement of walls in hashmap?
		// TODO FIND A BETTER WAY TO STORE/CHECK??
		ArrayList<BlockPos> walls = new ArrayList<>();
		byte[][] newMap = new byte[176 / WALL_SIZE][176 / WALL_SIZE];
		for (int i = 1; i < WALL_MAX - 1; i++) {
			for (int j = 1; j < WALL_MAX - 1; j++) {
				if (map[i][j]) {
					newMap[i][j] = 1;
					for (int k = -1; k < 2; k++) {
						for (int l = -1; l < 2; l++) {
							if (!map[i + k][j + l]) {
								newMap[i][j] = 2;
								break;
							}
						}
						if (newMap[i][j] == 2)
							break;
					}
					if (newMap[i][j] == 2) {
						walls.add(new BlockPos(i,0,j));
					}
				}
			}
		}

		// Go through again and either place corner in piece list or add wall to
		// wallsList (to determine wall or gate)
		for(BlockPos wall : walls) {
			 int i = wall.getX();
			 int j = wall.getZ();
				if (newMap[i][j] == 2) {
					int x = pos.getX() + i * WALL_SIZE + WALL_SIZE / 2 - OFFSET;
					
					int z = pos.getZ() + j * WALL_SIZE + WALL_SIZE / 2 - OFFSET;
					System.out.println(x + "," + z);
					// If it's not a corner
					if (newMap[i - 1][j] == newMap[i + 1][j] || newMap[i][j - 1] == newMap[i][j + 1]) {

					}
					// If it's a corner
					else {
						Rotation rot = Rotation.NONE;
						if (newMap[i + 1][j] == 2 && newMap[i][j + 1] == 2)
							rot = Rotation.CLOCKWISE_90;
						if (newMap[i - 1][j] == 2 && newMap[i][j + 1] == 2)
							rot = Rotation.CLOCKWISE_180;
						if (newMap[i - 1][j] == 2 && newMap[i][j - 1] == 2)
							rot = Rotation.COUNTERCLOCKWISE_90;
						pieceList.add(new AbstractCityPieces.Piece(templateManager,
								new ResourceLocation(PolisProject.MODID, "desert_city/wall_corner_1"),
								new BlockPos(x, pos.getY() - 1, z), rot));
					}
				
			}
		}

		// Move walls into larger map
		byte[][] byteMap = new byte[176][176];
		for (int i = WALL_SIZE; i < 176 - WALL_SIZE; i++) {
			for (int j = WALL_SIZE; j < 176 - WALL_SIZE; j++) {
				byteMap[i][j] = newMap[i / WALL_SIZE][j / WALL_SIZE];
//				if(walls.containsKey(i + "," + j)) System.out.print(9);
//				else System.out.print(byteMap[i][j]);
			}
//			System.out.println();
		}

		return byteMap;
	}

	public static class Piece extends TemplateStructurePiece {
		private ResourceLocation resourceLocation;
		private Rotation rotation;

		/* Generic StructurePiece methods */

		public Piece(TemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos,
				Rotation rotationIn) {
			super(PPStructures.CITY_PIECE, 0);
			this.resourceLocation = resourceLocationIn;
			BlockPos blockpos = new BlockPos(0, -1, 0);// UndergroundVillagePieces.OFFSET.get(resourceLocation);
			this.templatePosition = pos.offset(blockpos.getX(), blockpos.getY(), blockpos.getZ());
			this.rotation = rotationIn;
			this.setupPiece(templateManagerIn);
		}

		public Piece(TemplateManager templateManagerIn, CompoundNBT tagCompound) {
			super(PPStructures.CITY_PIECE, tagCompound);
			this.resourceLocation = new ResourceLocation(tagCompound.getString("Template"));
			this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
			this.setupPiece(templateManagerIn);
		}

		private void setupPiece(TemplateManager templateManager) {
			Template template = templateManager.getOrCreate(this.resourceLocation);
			PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation)
					.setMirror(Mirror.NONE);
			this.setup(template, this.templatePosition, placementsettings);
		}

		@Override
		protected void addAdditionalSaveData(CompoundNBT tagCompound) {
			super.addAdditionalSaveData(tagCompound);
			tagCompound.putString("Template", this.resourceLocation.toString());
			;
			tagCompound.putString("Rot", this.rotation.name());
		}

		@Override
		protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand,
				MutableBoundingBox sbb) {
//          if ("chest".equals(function)) {
//          worldIn.setBlockState(pos, Blocks.CHEST.getDefaultState(), 2);
//          TileEntity tileentity = worldIn.getTileEntity(pos);
//
//          // Just another check to make sure everything is going well before we try to set the chest.
//          if (tileentity instanceof ChestTileEntity) {
//              // ((ChestTileEntity) tileentity).setLootTable(<resource_location_to_loottable>, rand.nextLong());
//          }
//      }
		}
	}
}
