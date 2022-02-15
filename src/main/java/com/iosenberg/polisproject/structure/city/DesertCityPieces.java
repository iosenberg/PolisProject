package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
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
	private enum ANCHOR {
		FOUNTAIN	("desert_city/fountain", 6, 6, new BlockPos[] {}),
		GARDEN	("desert_city/garden", 10, 10, new BlockPos[0]),
		HOUSE_OF_WISDOM ("desert_city/house_of_wisdom", 16, 17, new BlockPos[0]),
		MARKET	("desert_city/market", 10, 10, new BlockPos[0]),
		OBELISK	("desert_city/obelisk", 8, 8, new BlockPos[0]),
		PLAZA	("desert_city/plaza", 15, 15, new BlockPos[0]),
		WELL	("desert_city/well", 4, 4, new BlockPos[0]);
		
		private final ResourceLocation resourceLocation;
		private final int xOffset;
		private final int zOffset;
		private final BlockPos[] legalRoadBlocks;
		
		ANCHOR(String rl, int x, int z, BlockPos[] lrb) {
			this.resourceLocation = new ResourceLocation(PolisProject.MODID, rl);
			this.xOffset = x;
			this.zOffset = z;
			this.legalRoadBlocks = lrb;
		}
		private ResourceLocation resourceLocation() {return resourceLocation;}
		private BlockPos offset(BlockPos pos, Rotation rot) {
			return pos.offset(
							rot.equals(Rotation.NONE) || rot.equals(Rotation.COUNTERCLOCKWISE_90) ? xOffset * -1 : xOffset,
							0,
							rot.equals(Rotation.NONE) || rot.equals(Rotation.CLOCKWISE_90) ? zOffset * -1 : zOffset);
			}
		private BlockPos[] legalRoadBlocks() {return legalRoadBlocks;}
		private BlockPos[] roadStarts(BlockPos pos, Rotation rotation) {
			int xOffset = this.xOffset;
			int zOffset = this.zOffset;

			if(rotation.equals(Rotation.CLOCKWISE_90) || rotation.equals(Rotation.COUNTERCLOCKWISE_90)) {
				int tempOffset = xOffset;
				xOffset = zOffset;
				zOffset = tempOffset;
			}
			BlockPos[] starts = {pos.offset(xOffset + 1, 0, 0), pos.offset(xOffset * -1 - 1, 0, 0), pos.offset(0, 0, zOffset + 1), pos.offset(0, 0, zOffset * -1 - 1)};
			return starts;
		}
	}
	
	private static final ResourceLocation BLOCK = new ResourceLocation(PolisProject.MODID, "block");
	private static final ResourceLocation[] STREETS = 
			{
					new ResourceLocation(PolisProject.MODID, "desert_city/street_1"),
					new ResourceLocation(PolisProject.MODID, "desert_city/street_2"),
					new ResourceLocation(PolisProject.MODID, "desert_city/street_3"),
					new ResourceLocation(PolisProject.MODID, "desert_city/street_4")
			};
	private static final ResourceLocation TEMPSTREET = new ResourceLocation(PolisProject.MODID, "street");
	
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
		
		//Add city "blocks" TODO once all buildings are placed, this shouldn't be necessary
		//KEEP: Also checks whether chunks are found in WorldStructureData as roads
		for (int i = 0; i < 176; i+=4) {
			for (int j = 0; j < 176; j+=4) {
				int x = i - offset + pos.getX();
				int z = j - offset + pos.getZ();
				if(cityMap[i][j] == 1) {
					pieceList.add(new AbstractCityPieces.Piece(templateManager, BLOCK, new BlockPos(x, height, z), Rotation.NONE));
				}
			}
		}
		
		ArrayList<List<BlockPos>> roadPoints = new ArrayList<List<BlockPos>>();

		//Place anchors
		for(long anchorAsLong : anchors) {
			BlockPos anchorPos = BlockPos.of(anchorAsLong);
			int anchorX = anchorPos.getX() + offset - pos.getX();
			int anchorZ = anchorPos.getZ() + offset - pos.getZ();
			
			ANCHOR anchor = ANCHOR.values()[random.nextInt(ANCHOR.values().length)];
			Rotation rotation = Rotation.getRandom(random);
			pieceList.add(new AbstractCityPieces.Piece(templateManager, anchor.resourceLocation(), anchor.offset(anchorPos, rotation), rotation));
			roadPoints.add(Arrays.asList(anchor.roadStarts(new BlockPos(anchorX, anchorPos.getY(), anchorZ), rotation)));
			
			//Set cells in cityMap			
			int xLeftCorner = anchor.xOffset * -1 - 1;
			int xRightCorner = anchor.xOffset + 1;
			int zLeftCorner = anchor.zOffset * -1 - 1;
			int zRightCorner = anchor.zOffset + 1;
			
			//If x and z were flipped, flip the offsets
			if(rotation.equals(Rotation.CLOCKWISE_90) || rotation.equals(Rotation.COUNTERCLOCKWISE_90)) {
				int tempCorner = xLeftCorner;
				xLeftCorner = zLeftCorner;
				zLeftCorner = tempCorner;
				tempCorner = xRightCorner;
				xRightCorner = zRightCorner;
				zRightCorner = tempCorner;
			}
			
			//Add anchor pos to place it in the map
			
			xLeftCorner += anchorX;
			xRightCorner += anchorX;
			zLeftCorner += anchorZ;
			zRightCorner += anchorZ;
			
			for(int i = xLeftCorner; i <= xRightCorner; i++) {
				for(int j = zLeftCorner; j <= zRightCorner; j++) {
					if(i == xLeftCorner || i == xRightCorner || j == zLeftCorner || j == zRightCorner) cityMap[i][j] = 3;
					else cityMap[i][j] = 2;
				}
			}
		}
		
		//Place streets
		for(int i = 0; i < roadPoints.size()-1; i++) {
			for(int j = 1; j < roadPoints.size(); j++) {
				List<BlockPos> list1 = roadPoints.get(i);
				List<BlockPos> list2 = roadPoints.get(j);
				//Source point
				int sX = list1.get(0).getX();
				int sZ = list1.get(0).getZ();
				//Destination point
				int dX = list2.get(0).getX();
				int dZ = list2.get(0).getZ();
				int minDist = Math.abs(sX - dX) + Math.abs(sZ - dZ);
				System.out.println(minDist);
				
				//Find two closest points in lists 1 and 2
				for(int k = 0; k < list1.size(); k++) {
					for(int l = 0; l < list2.size(); l++) {
						int tempSX = list1.get(k).getX();
						int tempSZ = list1.get(k).getZ();
						int tempDX = list2.get(l).getX();
						int tempDZ = list2.get(l).getZ();
						int tempDist = Math.abs(tempSX - tempDX) + Math.abs(tempSZ - tempDZ);
						if(tempDist < minDist) {
							sX = tempSX;
							sZ = tempSZ;
							dX = tempDX;
							dZ = tempDZ;
							minDist = tempDist;
							System.out.println(minDist);
						}
					}
				}
				
				//Now (x,z) and (destX,destZ) are the closest start points in each structure
				
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
				
				//TODO Clean up this implementation
				//Generate roads				
				LinkedList<Point> streetList = new LinkedList<Point>(); //The list of points at which to generate roads
				System.out.println(sX + "," + sZ + " to " + dX + "," + dZ);
				int x;
				int z;
				//If z is the open direction, start generation with Z distance
				System.out.println(cityMap[sX][sZ+1] + "," + cityMap[sX][sZ-1] + "," + cityMap[sX+1][sZ] + "," + cityMap[sX-1][sZ]);
				if(cityMap[sX][sZ-1] == 1 || cityMap[sX][sZ+1] == 1) {
					x = sX;
					z = sZ < dZ ? sZ + 1 : sZ - 1;
					System.out.println(x + "," + z + " : " + cityMap[x][z]);
					while(z!=dZ && cityMap[x][z]==1) {
						System.out.println(x + "," + z + " : " + cityMap[x][z]);
						streetList.add(new Point(x,z));
						if(z<dZ) z++;
						else z--;
					}
				}
				//Otherwise, start with X distance
				else {
					x = sX < dX ? sX + 1 : sX - 1;
					z = sZ;
					System.out.println(x + "," + z + " : " + cityMap[x][z]);
				}
				while(x!=dX && cityMap[x][z]==1) {
					System.out.println(x + "," + z + " : " + cityMap[x][z]);
					streetList.add(new Point(x,z));
					if(x<dX) x++;
					else x--;
				}
				//(In case Z isn't already done)
				while(z!=dZ && cityMap[x][z]==1) {
					System.out.println(x + "," + z + " : " + cityMap[x][z]);
					streetList.add(new Point(x,z));
					if(z<dZ) z++;
					else z--;
				}
				System.out.println(streetList.toString());
				if(cityMap[x][z]!=0) {
					while(!streetList.isEmpty()) {
						Point p = streetList.pop();
						System.out.println(p.x + "," + p.y + " : " + cityMap[p.x][p.y]);
						cityMap[p.x][p.y] = 3;
						pieceList.add(new AbstractCityPieces.Piece(templateManager, STREETS[random.nextInt(4)], new BlockPos(p.x - offset + pos.getX() - 1, height, p.y - offset + pos.getZ() - 1), Rotation.NONE));
					}
				} else {
					streetList.clear();
				}
				
//				//then in z direction
//				x = roadPoints.get(i).getX();
//				z = roadPoints.get(i).getZ();
//				if(z < destZ) z++;
//				if(z > destZ) z--;
//				while(z!=destZ && cityMap[x][z]==1) {
//					streetList.add(new Point(x,z));
//					if(z<destZ) z++;
//					else z--;
//				}
//				while(x!=destX && cityMap[x][z]==1) {
//					streetList.add(new Point(x,z));
//					if(x<destX) x++;
//					else x--;
//				}
//				if(cityMap[x][z]!=0) {
//					while(!streetList.isEmpty()) {
//						Point p = streetList.pop();
//						cityMap[p.x][p.y] = 3;
//						pieceList.add(new AbstractCityPieces.Piece(templateManager, STREETS[random.nextInt(4)], new BlockPos(p.x - offset + pos.getX() - 1, height, p.y - offset + pos.getZ() - 1), Rotation.NONE));
//					}
//				} else {
//					streetList.clear();
//				}
			}
		}
		
		//Print map
		char[] asciiMap = {'.',',','~','+'};
		for(int i = 0; i < 176; i++) {
			for(int j = 0; j < 176; j++) {
				System.out.print(asciiMap[cityMap[i][j]]);
				if(cityMap[i][j] == 3) pieceList.add(new AbstractCityPieces.Piece(templateManager, TEMPSTREET, new BlockPos(i - offset + pos.getX(), height, j - offset + pos.getZ()), Rotation.NONE));
			}
			System.out.println();
		}
	}
}
