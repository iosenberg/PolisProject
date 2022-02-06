package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.iosenberg.polisproject.PolisProject;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class DesertCityPieces extends AbstractCityPieces {
	private static final ResourceLocation BLOCK = new ResourceLocation(PolisProject.MODID, "block");
	private static final ResourceLocation STREET = new ResourceLocation(PolisProject.MODID, "street");
	
	private static final ResourceLocation[] ANCHORS = 
		{
				new ResourceLocation(PolisProject.MODID, "desert_city/fountain"),
				new ResourceLocation(PolisProject.MODID, "desert_city/garden"),
				new ResourceLocation(PolisProject.MODID, "desert_city/house_of_wisdom"),
				new ResourceLocation(PolisProject.MODID, "desert_city/obelisk"),
				new ResourceLocation(PolisProject.MODID, "desert_city/well")
		};
	
	private static final BlockPos[] OFFSET = 
		{
				new BlockPos(-6, 0, -6),
				new BlockPos(-10, 0, -10),
				new BlockPos(-19, 0, -20),
				new BlockPos(-8, 0, -8),
				new BlockPos(-4, 0, -4)
		};
	
	
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
					pieceList.add(new AbstractCityPieces.Piece(templateManager, BLOCK, new BlockPos(x, height, z), Rotation.NONE));
				}
			}
		}
		
		Point[] anchorsPoints = new Point[anchors.length];
		//Place anchors
		for(int i = 0; i < anchors.length; i++) {
			BlockPos anchorPos = BlockPos.of(anchors[i]);
			int anchor = random.nextInt(ANCHORS.length);
			pieceList.add(new AbstractCityPieces.Piece(templateManager, ANCHORS[anchor], anchorPos.offset(OFFSET[anchor]), Rotation.NONE));
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
				
				//TODO Mnyeh offsets don't work. Figure it out later
				//Add offsets
//				if(x < destX) {
//					x-=OFFSET[i].getX();
//					destZ+=OFFSET[j].getX();
//				}
//				else if(x > destX) {
//					x+=OFFSET[i].getX();
//					destX-=OFFSET[j].getX();
//				}				
//				if(z < destZ) {
//					z-=OFFSET[i].getZ();
//					destZ+=OFFSET[j].getZ();
//				}
//				else if(z > destZ) {
//					z+=OFFSET[i].getZ();
//					destZ-=OFFSET[j].getZ();
//				}
//				
				//Generate roads
				if(x < destX) {
					x++;
				}
				if(x > destX) {
					x--;
				}
				
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
						pieceList.add(new AbstractCityPieces.Piece(templateManager, STREET, new BlockPos(p.x - offset + pos.getX(), height, p.y - offset + pos.getZ()), Rotation.NONE));
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
						pieceList.add(new AbstractCityPieces.Piece(templateManager, STREET, new BlockPos(p.x - offset + pos.getX(), height, p.y - offset + pos.getZ()), Rotation.NONE));
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
}
