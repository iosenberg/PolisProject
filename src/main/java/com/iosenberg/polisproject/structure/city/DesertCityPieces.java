package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.ibm.icu.impl.UResource.Array;
import com.iosenberg.polisproject.PolisProject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class DesertCityPieces extends AbstractCityPieces {	
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
		
		System.out.println("HELLOOOOOOOO???");
		
		//Take world saved data and save it as local variables
		int height = (int)(city.getByte("height") - Byte.MIN_VALUE);
		byte[] byteMap = city.getByteArray("map");
		long[] anchors = city.getLongArray("anchors");
		BlockPos centerPos = new BlockPos(pos.getX(), height, pos.getZ());
		
		//translate 1d byte map into 2d citymap
		byte[][] cityMap = new byte[176][176];
		for(int i = 0; i < 176; i++) {
			for(int j = 0; j < 176; j++)  {
				cityMap[i][j] = byteMap[(i/4)*44 + (j/4)];
//				System.out.print(cityMap[i][j]);
			}
//			System.out.println();
		}
		System.out.println();

		
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
		
		ArrayList<BlockPos> gateList = new ArrayList<>();
		//Generate walls and roads. Adds them to pieceList and updates cityMap
		cityMap = generateWallsAndRoads(cityMap, centerPos, pieceList, gateList, templateManager, 4);
		
		System.out.println("WALL TYME");
		for(int i = 0; i < 176; i++) {
			for(int j = 0; j < 176; j++) {
				System.out.print(/*asciiMap[*/cityMap[i][j]/*]*/);
//				if(cityMap[i][j] == 3) pieceList.add(new AbstractCityPieces.Piece(templateManager, TEMPSTREET, new BlockPos(i - offset + pos.getX(), height, j - offset + pos.getZ()), Rotation.NONE));
			}
			System.out.println();
		}
		
		ArrayList</*List<*/BlockPos>/*>*/ roadPoints = new ArrayList</*List<*/BlockPos>/*>*/();
		roadPoints.addAll(gateList);
		
		//Place anchors
		for(long anchorAsLong : anchors) {
			BlockPos anchorPos = BlockPos.of(anchorAsLong);
			int anchorX = anchorPos.getX() + offset - pos.getX();
			int anchorZ = anchorPos.getZ() + offset - pos.getZ();
			
			CityAnchors.DESERT_ANCHOR anchor = CityAnchors.DESERT_ANCHOR.values()[random.nextInt(CityAnchors.DESERT_ANCHOR.values().length)];
			Rotation rotation = Rotation.getRandom(random);
			pieceList.add(new AbstractCityPieces.Piece(templateManager, anchor.resourceLocation(), anchor.offset(anchorPos, rotation), rotation));
			roadPoints.addAll(Arrays.asList(anchor.roadStarts(new BlockPos(anchorX, anchorPos.getY(), anchorZ), rotation)));
			
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
		
		generateAllStreets(cityMap, centerPos, roadPoints.toArray(new BlockPos[0]),
				pieceList, templateManager, 4);

		System.out.println("ROAD TIME");
		for(int i = 0; i < 176; i++) {
			for(int j = 0; j < 176; j++) {
				System.out.print(/*asciiMap[*/cityMap[i][j]/*]*/);
//				if(cityMap[i][j] == 3) pieceList.add(new AbstractCityPieces.Piece(templateManager, TEMPSTREET, new BlockPos(i - offset + pos.getX(), height, j - offset + pos.getZ()), Rotation.NONE));
			}
			System.out.println();
		}
		
		/**
		//Generate buildings
		for(int i = 0; i < 176; i++) {
			for(int j = 0; j < 176; j++) {
				//Find the corners of every section of 1s surrounded by not 1s
				if(cityMap[i][j] == 1) {
					ArrayList<BlockPos> corners = new ArrayList<BlockPos>(4);
					
					boolean[][] marked = new boolean[176][176];
					LinkedList<BlockPos> queue = new LinkedList<BlockPos>();
					queue.add(new BlockPos(i, height, j));
					marked[i][j] = true;
					cityMap[i][j] = 4;
					
					//Breadth first search, adding corners to list of corners and setting all cells == 1 to 4
					//We don't need to worry about out of bounds because the outer 4 blocks cannot equal 1 and cannot be queued
					while(!queue.isEmpty()) {
						BlockPos queuePos = queue.poll();
						
						//check -x neighbor
						int x = queuePos.getX() - 1;
						int z = queuePos.getZ();
						if(!marked[x][z]) {
							marked[x][z] = true;
							if(cityMap[x][z] == 1) {
								cityMap[x][z] = 4;
								queue.add(new BlockPos(x, height, z));
							}
						}
						
						//check +x neighbor
						x = queuePos.getX() + 1;
//						z is already set
						if(!marked[x][z]) {
							marked[x][z] = true;
							if(cityMap[x][z] == 1) {
								cityMap[x][z] = 4;
								queue.add(new BlockPos(x, height, z));
							}
						}
						
						//check -z neighbor
						x = queuePos.getX();
						z = queuePos.getZ() - 1;
						if(!marked[x][z]) {
							marked[x][z] = true;
							if(cityMap[x][z] == 1) {
								cityMap[x][z] = 4;
								queue.add(new BlockPos(x, height, z));
							}
						}
						
						//check +z neighbor
						//x is already set
						z = queuePos.getZ() + 1;
						if(!marked[x][z]) {
							marked[x][z] = true;
							if(cityMap[x][z] == 1) {
								cityMap[x][z] = 4;
								queue.add(new BlockPos(x, height, z));
							}
						}
						
						//check 8-way corners to determine whether cell is a corner
						int neighbors = 0;
						if(i != 0 && j != 0 && cityMap[i-1][j-1] != 1 && cityMap[i-1][j-1] != 4) neighbors++;
						if(i == 0 && j == 0) neighbors++;
						if(i != 0 && j != 175 && cityMap[i-1][j+1] != 1 && cityMap[i-1][j+1] != 4) neighbors++;
						if(i == 0 && j == 175) neighbors++;
						if(i != 175 && j != 0 && cityMap[i+1][j-1] != 1 && cityMap[i+1][j-1] != 4) neighbors++;
						if(i == 175 && j == 0) neighbors++;
						if(i != 175 && j != 175 && cityMap[i+1][j+1] != 1 && cityMap[i+1][j+1] != 4) neighbors++;
						if(i == 175 && j == 175) neighbors++;
						
						if(neighbors == 1 || neighbors == 3) cityMap[i][j] = corners.add(queuePos);
					}
					
//					ArrayList<Point> buildingsList = BUILDING.fillWithBuildings(templateManager, corners);
//					
//					System.out.println("Building size for " + i + "," + j);
//					for(Point p : buildingsList) {
//						System.out.println(p.x + " , " + p.y);
//					}
					
//					//Set all affected cells to 4
//					for(int k = 0; k < 176; k++) {
//						for(int l = 0; l < 176; l++) {
//							cityMap[k][l] = 4;
//						}
//					}
				}
			}
		}
		**/
		
		System.out.println("DONE!");
		//Print map
		char[] asciiMap = {'.',',','~','+',',','%'};
		for(int i = 0; i < 176; i++) {
			for(int j = 0; j < 176; j++) {
//				System.out.print(/*asciiMap[*/cityMap[i][j]/*]*/);
//				if(cityMap[i][j] == 3) pieceList.add(new AbstractCityPieces.Piece(templateManager, TEMPSTREET, new BlockPos(i - offset + pos.getX(), height, j - offset + pos.getZ()), Rotation.NONE));
			}
//			System.out.println();
		}
	}
	
//	private static void generateWalls(byte[][] map, List<StructurePiece> pieceList, BlockPos pos) {
//		
//	}
}
