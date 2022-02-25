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
	private enum ANCHOR {
		FOUNTAIN	("desert_city/fountain", 6, 6, 0),
		GARDEN	("desert_city/garden", 11, 11, 1),
		HOUSE_OF_WISDOM ("desert_city/house_of_wisdom", 17, 18, 2),
		MARKET	("desert_city/market", 11, 11, 0),
		OBELISK	("desert_city/obelisk", 8, 8, 0),
		PLAZA	("desert_city/plaza", 16, 16, 1),
		WELL	("desert_city/well", 4, 4, 0);
		
		private final ResourceLocation resourceLocation;
		private final int xOffset;
		private final int zOffset;
		//0 = starts at 4 corners, 1 = starts at middle of 4 edges, 2 = starts at middle of 2 edges
		private final int roadStarts;
		
		ANCHOR(String rl, int x, int z, int rs) {
			this.resourceLocation = new ResourceLocation(PolisProject.MODID, rl);
			this.xOffset = x;
			this.zOffset = z;
			this.roadStarts = rs;
		}
		private ResourceLocation resourceLocation() {return resourceLocation;}
		private BlockPos offset(BlockPos pos, Rotation rot) {
			return pos.offset(
							rot.equals(Rotation.NONE) || rot.equals(Rotation.COUNTERCLOCKWISE_90) ? xOffset * -1 : xOffset,
							0,
							rot.equals(Rotation.NONE) || rot.equals(Rotation.CLOCKWISE_90) ? zOffset * -1 : zOffset);
			}
		private BlockPos[] roadStarts(BlockPos pos, Rotation rotation) {
			int x = this.xOffset;
			int z = this.zOffset;

			if(rotation.equals(Rotation.CLOCKWISE_90) || rotation.equals(Rotation.COUNTERCLOCKWISE_90)) {
				int temp = x;
				x = z;
				z = temp;
			}
			
			if(roadStarts == 0) return new BlockPos[] {pos.offset(x + 1, 0, z + 1), pos.offset(x + 1, 0, z * -1 - 1), pos.offset(x * -1 - 1, 0, z + 1), pos.offset(x * -1 - 1, 0, z * -1 - 1)};
			if(roadStarts == 1) return new BlockPos[] {pos.offset(x + 1, 0, 0), pos.offset(x * -1 - 1, 0, 0), pos.offset(0, 0, z + 1), pos.offset(0, 0, z * -1 - 1)};
			//if (roadStarts == 2)
			return new BlockPos[] {pos.offset(0, 0, z + 1), pos.offset(0, 0, z * -1 - 1)};
		}
	}
	
	private enum BUILDING {
		//x >= z for every entry
		heehee	("f",4,4);
		
		private final ResourceLocation resourceLocation;
		private final int xSize;
		private final int zSize;
		
		BUILDING(String rl, int x, int z) {
			this.resourceLocation = new ResourceLocation(PolisProject.MODID, rl);
			this.xSize = x;
			this.zSize = z;
		}
		
		private static ArrayList<Point> fillWithBuildings(TemplateManager templateManager, ArrayList<BlockPos> cornerList) {
			return fillWithBuildingsRecurse(templateManager, cornerList).getSecond();
		}
		
		@SuppressWarnings("unchecked")
		private static Pair<Integer, ArrayList<Point>>fillWithBuildingsRecurse(TemplateManager templateManager, ArrayList<BlockPos> cornerList) {
			for(BlockPos pos : cornerList) {
				System.out.println(pos.toShortString() + " : ");
			}
			System.out.println();
			
			
			//If the polygon is not a rectangle, shave off a rectangle in the x and z directions and return the best solution of each of those
			//If polygon is not a rectangle, split it into smaller polygons, and run fillWithBuildingsRecurse()
			if(cornerList.size() > 4) {
				
				//Find min and max x and z value of all corners
				int xMin = Integer.MAX_VALUE;
				int xMax = 0;
				int zMin = Integer.MAX_VALUE;
				int zMax = 0;
				for(int i = 0; i < cornerList.size(); i++) {
					BlockPos corner = cornerList.get(i);
					if(corner.getX() < xMin) xMin = corner.getX();
					if(corner.getX() > xMax) xMax = corner.getX();
					if(corner.getZ() < zMin) zMin = corner.getZ();
					if(corner.getZ() > zMax) zMax = corner.getZ();
				}
				
				//List of positions of every corner on the outermost edges
				ArrayList<BlockPos> xMinPoses = new ArrayList<BlockPos>();
				ArrayList<BlockPos> xMaxPoses = new ArrayList<BlockPos>();
				ArrayList<BlockPos> zMinPoses = new ArrayList<BlockPos>();
				ArrayList<BlockPos> zMaxPoses = new ArrayList<BlockPos>();
				for(int i = 0; i < cornerList.size(); i++) {
					BlockPos corner = cornerList.get(i);
					if(corner.getX() == xMin) xMinPoses.add(corner);
					if(corner.getX() == xMax) xMaxPoses.add(corner);
					if(corner.getZ() == zMin) zMinPoses.add(corner);
					if(corner.getZ() == zMax) zMaxPoses.add(corner);
				}
				
				//GET MIN X SOLUTION
				xMinPoses.sort((a, b) -> a.getZ() - b.getZ());
				while(xMinPoses.size() > 2) xMinPoses.remove(2);
				
				Pair<Integer, ArrayList<Point>> xMinSolution;
				//If the corners are already the outer corners of the square, you cannot break it down into a new rectangle, so make the cost infinite
				if(xMinPoses.get(0).getZ() == zMin && xMinPoses.get(1).getZ() == zMax)  xMinSolution = new Pair<Integer, ArrayList<Point>>(Integer.MAX_VALUE, new ArrayList<Point>());
				//Otherwise, find the other corners and run the solution
				else {
					BlockPos thirdCorner = new BlockPos(xMinPoses.get(0).getX(), 0, Integer.MAX_VALUE);
					for(int i = 0; i < cornerList.size(); i++) {
						if((cornerList.get(i).getX() == xMinPoses.get(0).getX() || cornerList.get(i).getX() == xMinPoses.get(1).getX()) 
							&& cornerList.get(i).getX() != xMinPoses.get(0).getX()
							&& cornerList.get(i).getZ() < thirdCorner.getZ())
							thirdCorner = cornerList.get(i);
					}
					
					//xMinPoses is the corners of the removed rectangle
					xMinPoses.add(thirdCorner);
					//Fourth corner (third corner's z, with other x)
					xMinPoses.add(new BlockPos(thirdCorner.getX() == cornerList.get(0).getX() ? cornerList.get(1).getX() : cornerList.get(0).getX(),
												thirdCorner.getY(), thirdCorner.getZ()));
					
					//xMinPosesRemoved is the rest of the polygon
					ArrayList<BlockPos> xMinPosesRemoved = ((ArrayList<BlockPos>)cornerList.clone());
					//Remove rectangle made by xMinPoses from xMinPosesRemoved. Just trust me on this one, guys, idk how to explain it
					for(int i = 0; i < xMinPoses.size(); i++) {
						if(xMinPosesRemoved.contains(xMinPoses.get(i)))
							xMinPoses.remove(xMinPoses.get(i));
						else
							xMinPoses.add((xMinPoses.get(i)));
					}
					
					Pair<Integer, ArrayList<Point>> solution1 = fillWithBuildingsRecurse(templateManager, xMinPoses);
					Pair<Integer, ArrayList<Point>> solution2 = fillWithBuildingsRecurse(templateManager, xMinPosesRemoved);
					
					xMinSolution = new Pair<Integer, ArrayList<Point>>(solution1.getFirst() + solution2.getFirst(), solution1.getSecond());
					xMinSolution.getSecond().addAll(solution2.getSecond());
				}
				
				
				//GET MAX X SOLUTION
				xMaxPoses.sort((a, b) -> a.getZ() - b.getZ());
				while(xMaxPoses.size() > 2) xMaxPoses.remove(2);
				
				Pair<Integer, ArrayList<Point>> xMaxSolution;
				//If the corners are already the outer corners of the square, you cannot break it down into a new rectangle, so make the cost infinite
				if(xMaxPoses.get(0).getZ() == zMin && xMaxPoses.get(1).getZ() == zMax)  xMaxSolution = new Pair<Integer, ArrayList<Point>>(Integer.MAX_VALUE, new ArrayList<Point>());
				//Otherwise, find the other corners and run the solution
				else {
					BlockPos thirdCorner = new BlockPos(xMaxPoses.get(0).getX(), 0, Integer.MIN_VALUE);
					for(int i = 0; i < cornerList.size(); i++) {
						if((cornerList.get(i).getX() == xMaxPoses.get(0).getX() || cornerList.get(i).getX() == xMaxPoses.get(1).getX()) 
							&& cornerList.get(i).getX() != xMaxPoses.get(0).getX()
							&& cornerList.get(i).getZ() > thirdCorner.getZ())
							thirdCorner = cornerList.get(i);
					}
					
					//xMaxPoses is the corners of the removed rectangle
					xMaxPoses.add(thirdCorner);
					//Fourth corner (third corner's z, with other x)
					xMaxPoses.add(new BlockPos(thirdCorner.getX() == cornerList.get(0).getX() ? cornerList.get(1).getX() : cornerList.get(0).getX(),
												thirdCorner.getY(), thirdCorner.getZ()));
					
					//xMinPosesRemoved is the rest of the polygon
					ArrayList<BlockPos> xMaxPosesRemoved = ((ArrayList<BlockPos>)cornerList.clone());
					//Remove rectangle made by xMinPoses from xMinPosesRemoved. Just trust me on this one, guys, idk how to explain it
					for(int i = 0; i < xMaxPoses.size(); i++) {
						if(xMaxPosesRemoved.contains(xMaxPoses.get(i)))
							xMaxPoses.remove(xMaxPoses.get(i));
						else
							xMaxPoses.add((xMaxPoses.get(i)));
					}
					
					Pair<Integer, ArrayList<Point>> solution1 = fillWithBuildingsRecurse(templateManager, xMaxPoses);
					Pair<Integer, ArrayList<Point>> solution2 = fillWithBuildingsRecurse(templateManager, xMaxPosesRemoved);
					
					xMaxSolution = new Pair<Integer, ArrayList<Point>>(solution1.getFirst() + solution2.getFirst(), solution1.getSecond());
					xMaxSolution.getSecond().addAll(solution2.getSecond());
				}
				
				
				//GET MIN Z SOLUTION
				zMinPoses.sort((a, b) -> a.getX() - b.getX());
				while(zMinPoses.size() > 2) zMinPoses.remove(2);
				
				Pair<Integer, ArrayList<Point>> zMinSolution;
				//If the corners are already the outer corners of the square, you cannot break it down into a new rectangle, so make the cost infinite
				if(zMinPoses.get(0).getX() == xMin && zMinPoses.get(1).getX() == xMax)  zMinSolution = new Pair<Integer, ArrayList<Point>>(Integer.MAX_VALUE, new ArrayList<Point>());
				//Otherwise, find the other corners and run the solution
				else {
					BlockPos thirdCorner = new BlockPos(Integer.MAX_VALUE, 0, zMinPoses.get(0).getZ());
					for(int i = 0; i < cornerList.size(); i++) {
						if((cornerList.get(i).getZ() == zMinPoses.get(0).getZ() || cornerList.get(i).getZ() == zMinPoses.get(1).getZ()) 
							&& cornerList.get(i).getZ() != zMinPoses.get(0).getZ()
							&& cornerList.get(i).getX() < thirdCorner.getX())
							thirdCorner = cornerList.get(i);
					}
					
					//zMinPoses is the corners of the removed rectangle
					zMinPoses.add(thirdCorner);
					//Fourth corner (third corner's z, with other x)
					zMinPoses.add(new BlockPos(thirdCorner.getX(), thirdCorner.getY(),
												thirdCorner.getZ() == cornerList.get(0).getZ() ? cornerList.get(1).getZ() : cornerList.get(0).getZ()));
					
					//xMinPosesRemoved is the rest of the polygon
					ArrayList<BlockPos> zMinPosesRemoved = ((ArrayList<BlockPos>)cornerList.clone());
					//Remove rectangle made by xMinPoses from xMinPosesRemoved. Just trust me on this one, guys, idk how to explain it
					for(int i = 0; i < zMinPoses.size(); i++) {
						if(zMinPosesRemoved.contains(zMinPoses.get(i)))
							zMinPoses.remove(zMinPoses.get(i));
						else
							zMinPoses.add((zMinPoses.get(i)));
					}
					
					Pair<Integer, ArrayList<Point>> solution1 = fillWithBuildingsRecurse(templateManager, zMinPoses);
					Pair<Integer, ArrayList<Point>> solution2 = fillWithBuildingsRecurse(templateManager, zMinPosesRemoved);
					
					zMinSolution = new Pair<Integer, ArrayList<Point>>(solution1.getFirst() + solution2.getFirst(), solution1.getSecond());
					zMinSolution.getSecond().addAll(solution2.getSecond());
				}
				
				
				//GET MAX Z SOLUTION
				zMaxPoses.sort((a, b) -> a.getX() - b.getX());
				while(zMaxPoses.size() > 2) zMaxPoses.remove(2);
				
				Pair<Integer, ArrayList<Point>> zMaxSolution;
				//If the corners are already the outer corners of the square, you cannot break it down into a new rectangle, so make the cost infinite
				if(zMaxPoses.get(0).getX() == xMin && zMaxPoses.get(1).getX() == xMax)  zMaxSolution = new Pair<Integer, ArrayList<Point>>(Integer.MAX_VALUE, new ArrayList<Point>());
				//Otherwise, find the other corners and run the solution
				else {
					BlockPos thirdCorner = new BlockPos(Integer.MIN_VALUE, 0, zMaxPoses.get(0).getZ());
					for(int i = 0; i < cornerList.size(); i++) {
						if((cornerList.get(i).getZ() == zMaxPoses.get(0).getZ() || cornerList.get(i).getZ() == zMaxPoses.get(1).getZ()) 
							&& cornerList.get(i).getZ() != zMaxPoses.get(0).getZ()
							&& cornerList.get(i).getX() > thirdCorner.getX())
							thirdCorner = cornerList.get(i);
					}
					
					//zMaxPoses is the corners of the removed rectangle
					zMaxPoses.add(thirdCorner);
					//Fourth corner (third corner's z, with other x)
					zMaxPoses.add(new BlockPos(thirdCorner.getX(), thirdCorner.getY(),
												thirdCorner.getZ() == cornerList.get(0).getZ() ? cornerList.get(1).getZ() : cornerList.get(0).getZ()));
					
					//xMinPosesRemoved is the rest of the polygon
					ArrayList<BlockPos> zMaxPosesRemoved = ((ArrayList<BlockPos>)cornerList.clone());
					//Remove rectangle made by xMinPoses from xMinPosesRemoved. Just trust me on this one, guys, idk how to explain it
					for(int i = 0; i < zMaxPoses.size(); i++) {
						if(zMaxPosesRemoved.contains(zMaxPoses.get(i)))
							zMaxPoses.remove(zMaxPoses.get(i));
						else
							zMaxPoses.add((zMaxPoses.get(i)));
					}
					
					Pair<Integer, ArrayList<Point>> solution1 = fillWithBuildingsRecurse(templateManager, zMaxPoses);
					Pair<Integer, ArrayList<Point>> solution2 = fillWithBuildingsRecurse(templateManager, zMaxPosesRemoved);
					
					zMaxSolution = new Pair<Integer, ArrayList<Point>>(solution1.getFirst() + solution2.getFirst(), solution1.getSecond());
					zMaxSolution.getSecond().addAll(solution2.getSecond());
				}
				
				//Choose solution with lowest cost
				
				Pair<Integer, ArrayList<Point>>[] solutions = new Pair[] {
					xMinSolution,
					xMaxSolution,
					zMinSolution,
					zMaxSolution
				};
				
				//Find and return cheapest solution
				int cheapestIndex = 0;
				for(int i = 1; i < solutions.length; i++) {
					if(solutions[i].getFirst() < solutions[cheapestIndex].getFirst()) cheapestIndex = i;
				}
				
				return solutions[cheapestIndex];
			}
			//If polygon is a rectangle
			else {
				int xLength = Math.abs(cornerList.get(0).getX() - (cornerList.get(1).getX() == cornerList.get(0).getX() ? cornerList.get(2).getX() : cornerList.get(1).getX()));
				int zLength = Math.abs(cornerList.get(0).getZ() - (cornerList.get(1).getZ() == cornerList.get(0).getZ() ? cornerList.get(2).getZ() : cornerList.get(1).getZ()));
				
				int longLength;
				int shortLength;
				if(xLength > zLength) {
					longLength = xLength;
					shortLength = zLength;
				}
				else {
					longLength = zLength;
					shortLength = xLength;
				}
				
				return new Pair<Integer, ArrayList<Point>>(1,(ArrayList<Point>) Arrays.asList(new Point(longLength, shortLength)));
				
				//TODO uncomment after testing
//				int buildingIndex = 0;
//				while(BUILDING.values()[buildingIndex].xSize > longLength && BUILDING.values()[buildingIndex].zSize > shortLength) {
//					buildingIndex++;
//				}
//				return new Pair<Integer, ArrayList<Point>
			}
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
				int x;
				int z;
				//If z is the open direction, start generation with Z distance
				if(cityMap[sX][sZ-1] == 1 || cityMap[sX][sZ+1] == 1) {
					x = sX;
					z = sZ < dZ ? sZ + 1 : sZ - 1;
					while(z!=dZ && cityMap[x][z]==1) {
						streetList.add(new Point(x,z));
						if(z<dZ) z++;
						else z--;
					}
				}
				//Otherwise, start with X distance
				else {
					x = sX < dX ? sX + 1 : sX - 1;
					z = sZ;
				}
				while(x!=dX && cityMap[x][z]==1) {
					streetList.add(new Point(x,z));
					if(x<dX) x++;
					else x--;
				}
				//(In case Z isn't already done)
				while(z!=dZ && cityMap[x][z]==1) {
					streetList.add(new Point(x,z));
					if(z<dZ) z++;
					else z--;
				}
				if(cityMap[x][z]!=0) {
					while(!streetList.isEmpty()) {
						Point p = streetList.pop();
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
						
						if(neighbors == 1 || neighbors == 3) cityMap[i][j] = 5; //corners.add(queuePos);
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
		
		//Print map
		char[] asciiMap = {'.',',','~','+',',','%'};
		for(int i = 0; i < 176; i++) {
			for(int j = 0; j < 176; j++) {
				System.out.print(asciiMap[cityMap[i][j]]);
				if(cityMap[i][j] == 3) pieceList.add(new AbstractCityPieces.Piece(templateManager, TEMPSTREET, new BlockPos(i - offset + pos.getX(), height, j - offset + pos.getZ()), Rotation.NONE));
			}
			System.out.println();
		}
	}
}
