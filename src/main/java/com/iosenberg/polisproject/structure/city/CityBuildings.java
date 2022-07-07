package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.iosenberg.polisproject.PolisProject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class CityBuildings {
	public enum DESERT_BUILDING {
		// x >= z for every entry
		x10z10("x10z10", 10, 10), x12z10("x12z10", 12, 10), x12z12("x12z12", 12, 12), x14z10("x14z10", 14, 10),
		x14z12("x14z12", 14, 12), x14z14("x14z14", 14, 14), x16z10("x16z10", 16, 10), x16z12("x16z12", 16, 12),
		x16z14("x16z14", 16, 14), x18z10("x18z10", 18, 10), x18z12("x18z12", 18, 12), x18z14("x18z14", 18, 14),
		x20z10("x20z10", 20, 10), x20z12("x20z12", 20, 12), x20z14("x20z14", 20, 14), x22z10("x22z10", 22, 10),
		x22z12("x22z12", 22, 12), x22z14("x22z14", 22, 14), x24z10("x24z10", 24, 10), x24z12("x24z12", 24, 12),
		x24z14("x24z14", 24, 14), x26z10("x26z10", 26, 10), x26z12("x26z12", 26, 12), x26z14("x26z14", 26, 14),
		x28z10("x28z10", 28, 10), x28z12("x28z12", 28, 12), x28z14("x28z14", 28, 14), x30z10("x30z10", 30, 10),
		x30z12("x30z12", 30, 12), x30z14("x30z14", 30, 14),

		heehee("f", 4, 4);

		final ResourceLocation resourceLocation;
		final int xSize;
		final int zSize;

		DESERT_BUILDING(String rl, int x, int z) {
			this.resourceLocation = new ResourceLocation(PolisProject.MODID, rl);
			this.xSize = x;
			this.zSize = z;
		}

		/*
		 * The helper function that can be publicly called
		 * 
		 * @returns ArrayList of building pieces TODO for now, they're just points
		 * holding the x and y coords
		 */
		static ArrayList<Point> fillWithBuildings(Boolean templateManager, ArrayList<BlockPos> cornerList) {
			return fillWithBuildingsRecurse(templateManager, cornerList).getSecond();
		}

		/*
		 * The recursive building placement function
		 * 
		 * @returns
		 */
		@SuppressWarnings("unchecked")
		static Pair<Integer, ArrayList<Point>> fillWithBuildingsRecurse(Boolean templateManager,
				ArrayList<BlockPos> cornerList) {

			// If the polygon is not a rectangle, shave off a rectangle in the x and z
			// directions and return the best solution of each of those
			// If polygon is not a rectangle, split it into smaller polygons, and run
			// fillWithBuildingsRecurse()
			if (cornerList.size() > 4) {
				// Find min and max x and z value of all corners
				int xMin = Integer.MAX_VALUE;
				int xMax = 0;
				int zMin = Integer.MAX_VALUE;
				int zMax = 0;
				for (int i = 0; i < cornerList.size(); i++) {
					BlockPos corner = cornerList.get(i);
					if (corner.getX() < xMin)
						xMin = corner.getX();
					if (corner.getX() > xMax)
						xMax = corner.getX();
					if (corner.getZ() < zMin)
						zMin = corner.getZ();
					if (corner.getZ() > zMax)
						zMax = corner.getZ();
				}

				// List of positions of every corner on the outermost edges
				ArrayList<BlockPos> xMinPoses = new ArrayList<BlockPos>();
				ArrayList<BlockPos> xMaxPoses = new ArrayList<BlockPos>();
				ArrayList<BlockPos> zMinPoses = new ArrayList<BlockPos>();
				ArrayList<BlockPos> zMaxPoses = new ArrayList<BlockPos>();
				for (int i = 0; i < cornerList.size(); i++) {
					BlockPos corner = cornerList.get(i);
					if (corner.getX() == xMin)
						xMinPoses.add(corner);
					if (corner.getX() == xMax)
						xMaxPoses.add(corner);
					if (corner.getZ() == zMin)
						zMinPoses.add(corner);
					if (corner.getZ() == zMax)
						zMaxPoses.add(corner);
				}

				// GET MIN X SOLUTION
				xMinPoses.sort((a, b) -> a.getZ() - b.getZ());
				while (xMinPoses.size() > 2)
					xMinPoses.remove(2);

				Pair<Integer, ArrayList<Point>> xMinSolution;
				// If the corners are already the outer corners of the square, you cannot break
				// it down into a new rectangle, so make the cost infinite
				if (xMinPoses.get(0).getZ() == zMin && xMinPoses.get(1).getZ() == zMax)
					xMinSolution = new Pair<Integer, ArrayList<Point>>(Integer.MAX_VALUE, new ArrayList<Point>());
				// Otherwise, find the other corners and run the solution
				else {
					BlockPos thirdCorner = new BlockPos(xMinPoses.get(0).getX(), 0, Integer.MAX_VALUE);
					for (int i = 0; i < cornerList.size(); i++) {
						if ((cornerList.get(i).getZ() == xMinPoses.get(0).getZ()
								|| cornerList.get(i).getZ() == xMinPoses.get(1).getZ())
								&& cornerList.get(i).getX() != xMinPoses.get(0).getX()
								&& cornerList.get(i).getZ() < thirdCorner.getZ())
							thirdCorner = cornerList.get(i);
					}

					// xMinPoses is the corners of the removed rectangle
					xMinPoses.add(thirdCorner);
					// Fourth corner (third corner's z, with other x)
					xMinPoses.add(new BlockPos(thirdCorner.getX(), thirdCorner.getY(),
							thirdCorner.getZ() == xMinPoses.get(0).getZ() ? xMinPoses.get(1).getZ()
									: xMinPoses.get(0).getZ()));

					// xMinPosesRemoved is the rest of the polygon
					ArrayList<BlockPos> xMinPosesRemoved = ((ArrayList<BlockPos>) cornerList.clone());
					// Remove rectangle made by xMinPoses from xMinPosesRemoved. If it is not
					// already a corner, adds the point as a new corner
					for (int i = 0; i < xMinPoses.size(); i++) {
						if (xMinPosesRemoved.contains(xMinPoses.get(i)))
							xMinPosesRemoved.remove(xMinPoses.get(i));
						else
							xMinPosesRemoved.add((xMinPoses.get(i)));
					}

					Pair<Integer, ArrayList<Point>> solution1 = fillWithBuildingsRecurse(templateManager, xMinPoses);
					Pair<Integer, ArrayList<Point>> solution2 = fillWithBuildingsRecurse(templateManager,
							xMinPosesRemoved);

					xMinSolution = new Pair<Integer, ArrayList<Point>>(solution1.getFirst() + solution2.getFirst(),
							solution1.getSecond());
					xMinSolution.getSecond().addAll(solution2.getSecond());
				}

				// GET MAX X SOLUTION
				xMaxPoses.sort((a, b) -> a.getZ() - b.getZ());
				while (xMaxPoses.size() > 2)
					xMaxPoses.remove(2);

				Pair<Integer, ArrayList<Point>> xMaxSolution;
				// If the corners are already the outer corners of the square, you cannot break
				// it down into a new rectangle, so make the cost infinite
				if (xMaxPoses.get(0).getZ() == zMin && xMaxPoses.get(1).getZ() == zMax)
					xMaxSolution = new Pair<Integer, ArrayList<Point>>(Integer.MAX_VALUE, new ArrayList<Point>());
				// Otherwise, find the other corners and run the solution
				else {
					BlockPos thirdCorner = new BlockPos(xMaxPoses.get(0).getX(), 0, Integer.MIN_VALUE);
					for (int i = 0; i < cornerList.size(); i++) {
						if ((cornerList.get(i).getZ() == xMaxPoses.get(0).getZ()
								|| cornerList.get(i).getZ() == xMaxPoses.get(1).getZ())
								&& cornerList.get(i).getX() != xMaxPoses.get(0).getX()
								&& cornerList.get(i).getZ() > thirdCorner.getZ())
							thirdCorner = cornerList.get(i);
					}

					// xMaxPoses is the corners of the removed rectangle
					xMaxPoses.add(thirdCorner);
					// Fourth corner (third corner's z, with other x)
					xMaxPoses.add(new BlockPos(thirdCorner.getX(), thirdCorner.getY(),
							thirdCorner.getZ() == xMaxPoses.get(0).getZ() ? xMaxPoses.get(1).getZ()
									: xMaxPoses.get(0).getZ()));

					// xMinPosesRemoved is the rest of the polygon
					ArrayList<BlockPos> xMaxPosesRemoved = ((ArrayList<BlockPos>) cornerList.clone());
					// Remove rectangle made by xMinPoses from xMinPosesRemoved. If it is not
					// already a corner, adds the point as a new corner
					for (int i = 0; i < xMaxPoses.size(); i++) {
						if (xMaxPosesRemoved.contains(xMaxPoses.get(i)))
							xMaxPosesRemoved.remove(xMaxPoses.get(i));
						else
							xMaxPosesRemoved.add((xMaxPoses.get(i)));
					}

					Pair<Integer, ArrayList<Point>> solution1 = fillWithBuildingsRecurse(templateManager, xMaxPoses);
					Pair<Integer, ArrayList<Point>> solution2 = fillWithBuildingsRecurse(templateManager,
							xMaxPosesRemoved);

					xMaxSolution = new Pair<Integer, ArrayList<Point>>(solution1.getFirst() + solution2.getFirst(),
							solution1.getSecond());
					xMaxSolution.getSecond().addAll(solution2.getSecond());
				}

				// GET MIN Z SOLUTION
				zMinPoses.sort((a, b) -> a.getX() - b.getX());
				while (zMinPoses.size() > 2)
					zMinPoses.remove(2);

				Pair<Integer, ArrayList<Point>> zMinSolution;
				// If the corners are already the outer corners of the square, you cannot break
				// it down into a new rectangle, so make the cost infinite
				if (zMinPoses.get(0).getX() == xMin && zMinPoses.get(1).getX() == xMax)
					zMinSolution = new Pair<Integer, ArrayList<Point>>(Integer.MAX_VALUE, new ArrayList<Point>());
				// Otherwise, find the other corners and run the solution
				else {
					BlockPos thirdCorner = new BlockPos(Integer.MAX_VALUE, 0, zMinPoses.get(0).getZ());
					for (int i = 0; i < cornerList.size(); i++) {
						if ((cornerList.get(i).getX() == zMinPoses.get(0).getX()
								|| cornerList.get(i).getX() == zMinPoses.get(1).getX())
								&& cornerList.get(i).getZ() != zMinPoses.get(0).getZ()
								&& cornerList.get(i).getX() < thirdCorner.getX())
							thirdCorner = cornerList.get(i);
					}

					// zMinPoses is the corners of the removed rectangle
					zMinPoses.add(thirdCorner);
					// Fourth corner (third corner's z, with other x)
					zMinPoses.add(new BlockPos(thirdCorner.getX() == zMinPoses.get(0).getX() ? zMinPoses.get(1).getX()
							: zMinPoses.get(0).getX(), thirdCorner.getY(), thirdCorner.getZ()));

					// xMinPosesRemoved is the rest of the polygon
					ArrayList<BlockPos> zMinPosesRemoved = ((ArrayList<BlockPos>) cornerList.clone());
					// Remove rectangle made by xMinPoses from xMinPosesRemoved. If it is not
					// already a corner, adds the point as a new corner
					for (int i = 0; i < zMinPoses.size(); i++) {
						if (zMinPosesRemoved.contains(zMinPoses.get(i)))
							zMinPosesRemoved.remove(zMinPoses.get(i));
						else
							zMinPosesRemoved.add((zMinPoses.get(i)));
					}

					Pair<Integer, ArrayList<Point>> solution1 = fillWithBuildingsRecurse(templateManager, zMinPoses);
					Pair<Integer, ArrayList<Point>> solution2 = fillWithBuildingsRecurse(templateManager,
							zMinPosesRemoved);

					zMinSolution = new Pair<Integer, ArrayList<Point>>(solution1.getFirst() + solution2.getFirst(),
							solution1.getSecond());
					zMinSolution.getSecond().addAll(solution2.getSecond());
				}

				// GET MAX Z SOLUTION
				zMaxPoses.sort((a, b) -> a.getX() - b.getX());
				while (zMaxPoses.size() > 2)
					zMaxPoses.remove(2);

				Pair<Integer, ArrayList<Point>> zMaxSolution;
				// If the corners are already the outer corners of the square, you cannot break
				// it down into a new rectangle, so make the cost infinite
				if (zMaxPoses.get(0).getX() == xMin && zMaxPoses.get(1).getX() == xMax)
					zMaxSolution = new Pair<Integer, ArrayList<Point>>(Integer.MAX_VALUE, new ArrayList<Point>());
				// Otherwise, find the other corners and run the solution
				else {
					BlockPos thirdCorner = new BlockPos(Integer.MIN_VALUE, 0, zMaxPoses.get(0).getZ());
					for (int i = 0; i < cornerList.size(); i++) {
						if ((cornerList.get(i).getX() == zMaxPoses.get(0).getX()
								|| cornerList.get(i).getX() == zMaxPoses.get(1).getX())
								&& cornerList.get(i).getZ() != zMaxPoses.get(0).getZ()
								&& cornerList.get(i).getX() > thirdCorner.getX())
							thirdCorner = cornerList.get(i);
					}

					// zMaxPoses is the corners of the removed rectangle
					zMaxPoses.add(thirdCorner);
					// Fourth corner (third corner's z, with other x)
					zMaxPoses.add(new BlockPos(thirdCorner.getX() == zMaxPoses.get(0).getX() ? zMaxPoses.get(1).getX()
							: zMaxPoses.get(0).getX(), thirdCorner.getY(), thirdCorner.getZ()));

					// xMinPosesRemoved is the rest of the polygon
					ArrayList<BlockPos> zMaxPosesRemoved = ((ArrayList<BlockPos>) cornerList.clone());
					// Remove rectangle made by xMinPoses from xMinPosesRemoved. If it is not
					// already a corner, adds the point as a new corner
					for (int i = 0; i < zMaxPoses.size(); i++) {
						if (zMaxPosesRemoved.contains(zMaxPoses.get(i)))
							zMaxPosesRemoved.remove(zMaxPoses.get(i));
						else
							zMaxPosesRemoved.add((zMaxPoses.get(i)));
					}

					Pair<Integer, ArrayList<Point>> solution1 = fillWithBuildingsRecurse(templateManager, zMaxPoses);
					Pair<Integer, ArrayList<Point>> solution2 = fillWithBuildingsRecurse(templateManager,
							zMaxPosesRemoved);

					zMaxSolution = new Pair<Integer, ArrayList<Point>>(solution1.getFirst() + solution2.getFirst(),
							solution1.getSecond());
					zMaxSolution.getSecond().addAll(solution2.getSecond());
				}

				// Choose solution with lowest cost

				Pair<Integer, ArrayList<Point>>[] solutions = new Pair[] { xMinSolution, xMaxSolution, zMinSolution,
						zMaxSolution };

				// Find and return cheapest solution
				int cheapestIndex = 0;
				for (int i = 1; i < solutions.length; i++) {
					if (solutions[i].getFirst() < solutions[cheapestIndex].getFirst())
						cheapestIndex = i;
				}

				return solutions[cheapestIndex];
			}
			// If corners == 4 (polygon is a rectangle)
			else {
				int xLength = Math.abs(cornerList.get(0).getX()
						- (cornerList.get(1).getX() == cornerList.get(0).getX() ? cornerList.get(2).getX()
								: cornerList.get(1).getX()));
				int zLength = Math.abs(cornerList.get(0).getZ()
						- (cornerList.get(1).getZ() == cornerList.get(0).getZ() ? cornerList.get(2).getZ()
								: cornerList.get(1).getZ()));

				int longLength;
				int shortLength;
				if (xLength > zLength) {
					longLength = xLength;
					shortLength = zLength;
				} else {
					longLength = zLength;
					shortLength = xLength;
				}
				return new Pair<Integer, ArrayList<Point>>(1,
						new ArrayList<Point>(List.of(new Point(longLength, shortLength))));

				// TODO uncomment after testing
//				int buildingIndex = 0;
//				while(BUILDING.values()[buildingIndex].xSize > longLength && BUILDING.values()[buildingIndex].zSize > shortLength) {
//					buildingIndex++;
//				}
//				return new Pair<Integer, ArrayList<Point>
			}
		}
	}
}

/*
 * ArrayList<Point> f = new ArrayList<>(); for(BlockPos h : cornerList) {
 * f.add(new Point(h.getX(), h.getZ())); }
 * 
 * return new Pair<Integer, ArrayList<Point>>(1, f);
 */
