package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import com.iosenberg.polisproject.PolisProject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class CityBuildings {
	public enum DESERT_BUILDING {
		//x >= z for every entry
		heehee	("f",4,4);
		
		final ResourceLocation resourceLocation;
		final int xSize;
		final int zSize;
		
		DESERT_BUILDING(String rl, int x, int z) {
			this.resourceLocation = new ResourceLocation(PolisProject.MODID, rl);
			this.xSize = x;
			this.zSize = z;
		}
		
		static ArrayList<Point> fillWithBuildings(TemplateManager templateManager, ArrayList<BlockPos> cornerList) {
			return fillWithBuildingsRecurse(templateManager, cornerList).getSecond();
		}
		
		@SuppressWarnings("unchecked")
		static Pair<Integer, ArrayList<Point>>fillWithBuildingsRecurse(TemplateManager templateManager, ArrayList<BlockPos> cornerList) {
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
}
