package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.LinkedList;
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
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class DebugCityManager extends AbstractCityManager{
	private static final ResourceLocation BLOCK = new ResourceLocation(PolisProject.MODID, "block");
	private static final ResourceLocation ROADJUNCTION = new ResourceLocation(PolisProject.MODID, "road_junction");
	private static final ResourceLocation STREET = new ResourceLocation(PolisProject.MODID, "street");

	public static byte[][] generateMap(ChunkGenerator generator, int chunkX, int chunkZ) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	public static void generatePieces(TemplateManager templateManager, BlockPos pos, Rotation rotation,
//			List<StructurePiece> pieceList, Random random) {
//
//		//int size = 5;
//		int offset = 80;
//		
//		for (int i = 0; i < 176; i+=4) {
//			for (int j = 0; j < 176; j+=4) {
//				int x = i - offset + pos.getX();
//				int z = j - offset + pos.getZ();
//				if(map[i][j]) pieceList.add(new DebugCityManager.Piece(templateManager, BLOCK, new BlockPos(x, height, z), rotation));
//
//			}
//		}		
//	}
	
	public static void start(TemplateManager templateManager, BlockPos pos, CompoundNBT city, List<StructurePiece> pieceList, Random random) {
		
		System.out.println("pp");
		int height = (int)(city.getByte("height") - Byte.MIN_VALUE);
		byte[] byteMap = city.getByteArray("map");
		long[] anchors = city.getLongArray("anchors");
		
		byte[][] cityMap = new byte[176][176];
		for(int i = 0; i < 176; i++) 
			for(int j = 0; j < 176; j++) 
				cityMap[i][j] = byteMap[(i/4)*44 + (j/4)];
		
		int offset = 80;
		
		for (int i = 0; i < 176; i+=4) {
			for (int j = 0; j < 176; j+=4) {
				int x = i - offset + pos.getX();
				int z = j - offset + pos.getZ();
				if(cityMap[i][j] == 1) {
					pieceList.add(new DebugCityManager.Piece(templateManager, BLOCK, new BlockPos(x, height, z), Rotation.NONE));
				}
			}
		}
		
		Point[] anchorsPoints = new Point[anchors.length];
		//Place anchors
		for(int i = 0; i < anchors.length; i++) {
			BlockPos anchorPos = BlockPos.of(anchors[i]);
			pieceList.add(new DebugCityManager.Piece(templateManager, ROADJUNCTION, anchorPos.offset(-1,0,-1), Rotation.NONE));
			anchorsPoints[i] = new Point(anchorPos.getX() - pos.getX() + offset, anchorPos.getZ() - pos.getZ() + offset);
			cityMap[anchorsPoints[i].x][anchorsPoints[i].y] = 2;
		}
		
		//Place streets
		for(int i = 0; i < anchorsPoints.length-1; i++) {
			for(int j = 1; j < anchorsPoints.length; j++) {
				//first in x direction
				int x = anchorsPoints[i].x;
				int z = anchorsPoints[i].y;
				int destX = anchorsPoints[j].x;
				int destZ = anchorsPoints[j].y;
				if(x < destX) x++;
				if(x > destX) x--;
				LinkedList<Point> streetList = new LinkedList<Point>();
				while(x!=destX && cityMap[x][z]!=3 && cityMap[x][z]!=0) {
					streetList.add(new Point(x,z));
					if(x<destX) x++;
					else x--;
				}
				while(z!=destZ && cityMap[x][z]!=3 && cityMap[x][z]!=0) {
					streetList.add(new Point(x,z));
					if(z<destZ) z++;
					else z--;
				}
				if(cityMap[x][z]!=0) {
					while(!streetList.isEmpty()) {
						Point p = streetList.pop();
						cityMap[p.x][p.y] = 3;
						pieceList.add(new DebugCityManager.Piece(templateManager, STREET, new BlockPos(p.x - offset + pos.getX(), height, p.y - offset + pos.getZ()), Rotation.NONE));
					}
				} else {
					streetList.clear();
				}
				
				//then in z direction
				x = anchorsPoints[i].x;
				z = anchorsPoints[i].y;
				if(z < destZ) z++;
				if(z > destZ) z--;
				while(z!=destZ && cityMap[x][z]!=3 && cityMap[x][z]!=0) {
					streetList.add(new Point(x,z));
					if(z<destZ) z++;
					else z--;
				}
				while(x!=destX && cityMap[x][z]!=3 && cityMap[x][z]!=0) {
					streetList.add(new Point(x,z));
					if(x<destX) x++;
					else x--;
				}
				if(cityMap[x][z]!=0) {
					while(!streetList.isEmpty()) {
						Point p = streetList.pop();
						cityMap[p.x][p.y] = 3;
						pieceList.add(new DebugCityManager.Piece(templateManager, STREET, new BlockPos(p.x - offset + pos.getX(), height, p.y - offset + pos.getZ()), Rotation.NONE));
					}
				} else {
					streetList.clear();
				}
			}
		}
		
		//Print map
		for(int i = 0; i < 176; i++) {
			for(int j = 0; j < 176; j++) {
				System.out.print(cityMap[i][j]);
			}
			System.out.println();
		}
	}

	//quick fix. need to put this somewhere else
	public static class Piece extends TemplateStructurePiece {
		private ResourceLocation resourceLocation;
		private Rotation rotation;
		
		public Piece(TemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos, Rotation rotationIn) {
			super(PPStructures.CITY_PIECE, 0);
			this.resourceLocation = resourceLocationIn;
			BlockPos blockpos = new BlockPos(0,-1,0);//UndergroundVillagePieces.OFFSET.get(resourceLocation);
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
			PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE);
			this.setup(template, this.templatePosition, placementsettings);
		}
		
		@Override
		protected void addAdditionalSaveData(CompoundNBT tagCompound) {
			super.addAdditionalSaveData(tagCompound);
			tagCompound.putString("Template",  this.resourceLocation.toString());;
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
