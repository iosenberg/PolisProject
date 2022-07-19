package com.iosenberg.polisproject.structure.city;

import java.util.ArrayList;

import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.structure.city.AbstractCityPieces.Piece;
import com.mojang.datafixers.util.Pair;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class CityBuildings {
	public enum DESERT_BUILDING {
		// x >= z for every entry
		x9z9("x9z9", 9, 9), x11z9("x11z9", 11, 9), x11z11("x11z11", 11, 11), x13z9("x13z9", 13, 9),
		x13z11("x13z11", 13, 11), x13z13("x13z13", 13, 13), x15z9("x15z9", 15, 9), x15z11("x15z11", 15, 11),
		x15z13("x15z13", 15, 13), x17z9("x17z9", 17, 9), x17z11("x17z11", 17, 11), x17z13("x17z13", 17, 13),
		x19z9("x19z9", 19, 9), x19z11("x19z11", 19, 11), x19z13("x19z13", 19, 13), x21z9("x21z9", 21, 9),
		x21z11("x21z11", 21, 11), x21z13("x21z13", 21, 13), x23z9("x23z9", 23, 9), x23z11("x23z11", 23, 11),
		x23z13("x23z13", 23, 13), x25z9("x25z9", 25, 9), x25z11("x25z11", 25, 11), x25z13("x25z13", 25, 13),
		x27z9("x27z9", 27, 9), x27z11("x27z11", 27, 11), x27z13("x27z13", 27, 13), x29z9("x29z9", 29, 9),
		x29z11("x29z11", 29, 11), x29z13("x29z13", 29, 13),

		;

		static final int largestX = 29;
		static final int largestZ = 13;
		final ResourceLocation resourceLocation;
		final int xSize;
		final int zSize;

		DESERT_BUILDING(String rl, int x, int z) {
			this.resourceLocation = new ResourceLocation(PolisProject.MODID, "desert_city/" + rl);
			this.xSize = x;
			this.zSize = z;
		}

		ResourceLocation resourceLocation() {
			return resourceLocation;
		}

		BlockPos offset(BlockPos pos, Rotation rot) {
			int xOffset = (xSize / 2) + 1;
			int zOffset = (zSize / 2) + 1;
			return pos.offset(
					rot.equals(Rotation.NONE) || rot.equals(Rotation.COUNTERCLOCKWISE_90) ? xSize * -1 : xSize, 0,
					rot.equals(Rotation.NONE) || rot.equals(Rotation.CLOCKWISE_90) ? zSize * -1 : zSize);
		}

		/*
		 * The helper function that can be publicly called
		 * 
		 * @returns ArrayList of building pieces TODO for now, they're just points
		 * holding the x and y coords
		 */
		static ArrayList<StructurePiece> fillWithBuildings(TemplateManager templateManager,
				ArrayList<BlockPos> cornerList, BlockPos pos) {
			return fillWithBuildingsRecurse(templateManager, cornerList, pos).getSecond();
		}

		/*
		 * The recursive building placement function
		 * 
		 * @returns
		 */
		@SuppressWarnings("unchecked")
		private static Pair<Integer, ArrayList<StructurePiece>> fillWithBuildingsRecurse(
				TemplateManager templateManager, ArrayList<BlockPos> cornerList, BlockPos pos) {
//			System.out.println("new bitch!");

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

				Pair<Integer, ArrayList<StructurePiece>> xMinSolution;
				// If the corners are already the outer corners of the square, you cannot break
				// it down into a new rectangle, so make the cost infinite
				if (xMinPoses.get(0).getZ() == zMin && xMinPoses.get(1).getZ() == zMax)
					xMinSolution = new Pair<Integer, ArrayList<StructurePiece>>(Integer.MAX_VALUE,
							new ArrayList<StructurePiece>());
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

					Pair<Integer, ArrayList<StructurePiece>> solution1 = fillWithBuildingsRecurse(templateManager,
							xMinPoses, pos);
					Pair<Integer, ArrayList<StructurePiece>> solution2 = fillWithBuildingsRecurse(templateManager,
							xMinPosesRemoved, pos);

					xMinSolution = new Pair<Integer, ArrayList<StructurePiece>>(
							solution1.getFirst() + solution2.getFirst(), solution1.getSecond());
					xMinSolution.getSecond().addAll(solution2.getSecond());
				}

				// GET MAX X SOLUTION
				xMaxPoses.sort((a, b) -> a.getZ() - b.getZ());
				while (xMaxPoses.size() > 2)
					xMaxPoses.remove(2);

				Pair<Integer, ArrayList<StructurePiece>> xMaxSolution;
				// If the corners are already the outer corners of the square, you cannot break
				// it down into a new rectangle, so make the cost infinite
				if (xMaxPoses.get(0).getZ() == zMin && xMaxPoses.get(1).getZ() == zMax)
					xMaxSolution = new Pair<Integer, ArrayList<StructurePiece>>(Integer.MAX_VALUE,
							new ArrayList<StructurePiece>());
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

					Pair<Integer, ArrayList<StructurePiece>> solution1 = fillWithBuildingsRecurse(templateManager,
							xMaxPoses, pos);
					Pair<Integer, ArrayList<StructurePiece>> solution2 = fillWithBuildingsRecurse(templateManager,
							xMaxPosesRemoved, pos);

					xMaxSolution = new Pair<Integer, ArrayList<StructurePiece>>(
							solution1.getFirst() + solution2.getFirst(), solution1.getSecond());
					xMaxSolution.getSecond().addAll(solution2.getSecond());
				}

				// GET MIN Z SOLUTION
				zMinPoses.sort((a, b) -> a.getX() - b.getX());
				while (zMinPoses.size() > 2)
					zMinPoses.remove(2);

				Pair<Integer, ArrayList<StructurePiece>> zMinSolution;
				// If the corners are already the outer corners of the square, you cannot break
				// it down into a new rectangle, so make the cost infinite
				if (zMinPoses.get(0).getX() == xMin && zMinPoses.get(1).getX() == xMax)
					zMinSolution = new Pair<Integer, ArrayList<StructurePiece>>(Integer.MAX_VALUE,
							new ArrayList<StructurePiece>());
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

					Pair<Integer, ArrayList<StructurePiece>> solution1 = fillWithBuildingsRecurse(templateManager,
							zMinPoses, pos);
					Pair<Integer, ArrayList<StructurePiece>> solution2 = fillWithBuildingsRecurse(templateManager,
							zMinPosesRemoved, pos);

					zMinSolution = new Pair<Integer, ArrayList<StructurePiece>>(
							solution1.getFirst() + solution2.getFirst(), solution1.getSecond());
					zMinSolution.getSecond().addAll(solution2.getSecond());
				}

				// GET MAX Z SOLUTION
				zMaxPoses.sort((a, b) -> a.getX() - b.getX());
				while (zMaxPoses.size() > 2)
					zMaxPoses.remove(2);

				Pair<Integer, ArrayList<StructurePiece>> zMaxSolution;
				// If the corners are already the outer corners of the square, you cannot break
				// it down into a new rectangle, so make the cost infinite
				if (zMaxPoses.get(0).getX() == xMin && zMaxPoses.get(1).getX() == xMax)
					zMaxSolution = new Pair<Integer, ArrayList<StructurePiece>>(Integer.MAX_VALUE,
							new ArrayList<StructurePiece>());
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

					Pair<Integer, ArrayList<StructurePiece>> solution1 = fillWithBuildingsRecurse(templateManager,
							zMaxPoses, pos);
					Pair<Integer, ArrayList<StructurePiece>> solution2 = fillWithBuildingsRecurse(templateManager,
							zMaxPosesRemoved, pos);

					zMaxSolution = new Pair<Integer, ArrayList<StructurePiece>>(
							solution1.getFirst() + solution2.getFirst(), solution1.getSecond());
					zMaxSolution.getSecond().addAll(solution2.getSecond());
				}

				// Choose solution with lowest cost

				Pair<Integer, ArrayList<StructurePiece>>[] solutions = new Pair[] { xMinSolution, xMaxSolution,
						zMinSolution, zMaxSolution };

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
				// TODO Temporary because this shouldn't be happening.
//				if(cornerList.size() < 4) {
//					System.out.println("Cringe! : " + cornerList.size());
//					return new Pair<Integer, ArrayList<StructurePiece>>(Integer.MAX_VALUE, null);
//				}

				int lowX = 176;
				int highX = 0;
				int lowZ = 176;
				int highZ = 0;
				for (BlockPos c : cornerList) {
					if (c.getX() < lowX)
						lowX = c.getX();
					if (c.getX() > highX)
						highX = c.getX();
					if (c.getZ() < lowZ)
						lowZ = c.getZ();
					if (c.getZ() > highZ)
						highZ = c.getZ();
				}

				int xLength = highX - lowX;
				int zLength = highZ - lowZ;

				int longLength;
				int shortLength;
				boolean flip = false;
				if (xLength > zLength) {
					longLength = xLength;
					shortLength = zLength;
				} else {
					longLength = zLength;
					shortLength = xLength;
					flip = true;
				}
				
//				System.out.println("square! of size " + xLength + "," + zLength);

				//To avoid wasting calculation time
				if(longLength < 9 || shortLength < 9)
					return new Pair<Integer, ArrayList<StructurePiece>>(10, new ArrayList<StructurePiece>());
				
				// TODO maybe clean this up a bit later.
				// If one of the sides of the rectangle is too long, split it into two and three
				// parts, then return the lowest cost
				// If x is longer and larger than largestX or x is smaller and larger than
				// largestZ
				if (longLength > DESERT_BUILDING.largestX || shortLength > DESERT_BUILDING.largestZ) {
//					System.out.println("TOO LONG!!!" + longLength + "," + shortLength);
					ArrayList<BlockPos> twoSplitCornersList1 = new ArrayList<>();
					ArrayList<BlockPos> twoSplitCornersList2 = new ArrayList<>();

					ArrayList<BlockPos> threeSplitCornersList1 = new ArrayList<>();
					ArrayList<BlockPos> threeSplitCornersList2 = new ArrayList<>();
					ArrayList<BlockPos> threeSplitCornersList3 = new ArrayList<>();

					if ((longLength > DESERT_BUILDING.largestX && longLength == xLength)
							|| (shortLength > DESERT_BUILDING.largestZ && shortLength == xLength)) {
						int xSplit = (lowX + highX) / 2;

						twoSplitCornersList1.add(new BlockPos(lowX, pos.getY(), lowZ));
						twoSplitCornersList1.add(new BlockPos(lowX, pos.getY(), highZ));
						twoSplitCornersList1.add(new BlockPos(xSplit, pos.getY(), lowZ));
						twoSplitCornersList1.add(new BlockPos(xSplit, pos.getY(), highZ));

						twoSplitCornersList2.add(new BlockPos(highX, pos.getY(), lowZ));
						twoSplitCornersList2.add(new BlockPos(highX, pos.getY(), highZ));
						twoSplitCornersList2.add(new BlockPos(xSplit + 1, pos.getY(), lowZ));
						twoSplitCornersList2.add(new BlockPos(xSplit + 1, pos.getY(), highZ));

						xSplit = lowX + xLength / 3;
						int xSplit2 = lowX + 2 * xLength / 3;

						threeSplitCornersList1.add(new BlockPos(lowX, pos.getY(), lowZ));
						threeSplitCornersList1.add(new BlockPos(lowX, pos.getY(), highZ));
						threeSplitCornersList1.add(new BlockPos(xSplit, pos.getY(), lowZ));
						threeSplitCornersList1.add(new BlockPos(xSplit, pos.getY(), highZ));

						threeSplitCornersList2.add(new BlockPos(xSplit + 1, pos.getY(), lowZ));
						threeSplitCornersList2.add(new BlockPos(xSplit + 1, pos.getY(), highZ));
						threeSplitCornersList2.add(new BlockPos(xSplit2, pos.getY(), lowZ));
						threeSplitCornersList2.add(new BlockPos(xSplit2, pos.getY(), highZ));

						threeSplitCornersList3.add(new BlockPos(xSplit2 + 1, pos.getY(), lowZ));
						threeSplitCornersList3.add(new BlockPos(xSplit2 + 1, pos.getY(), highZ));
						threeSplitCornersList3.add(new BlockPos(highX, pos.getY(), lowZ));
						threeSplitCornersList3.add(new BlockPos(highX, pos.getY(), highZ));
					}
					if ((longLength > DESERT_BUILDING.largestX && longLength == zLength)
							|| (shortLength > DESERT_BUILDING.largestZ && shortLength == zLength)) {
						int zSplit = (lowZ + highZ) / 2;

						twoSplitCornersList1.add(new BlockPos(lowX, pos.getY(), lowZ));
						twoSplitCornersList1.add(new BlockPos(highX, pos.getY(), lowZ));
						twoSplitCornersList1.add(new BlockPos(lowX, pos.getY(), zSplit));
						twoSplitCornersList1.add(new BlockPos(highX, pos.getY(), zSplit));

						twoSplitCornersList2.add(new BlockPos(lowX, pos.getY(), highZ));
						twoSplitCornersList2.add(new BlockPos(highX, pos.getY(), highZ));
						twoSplitCornersList2.add(new BlockPos(lowX, pos.getY(), zSplit + 1));
						twoSplitCornersList2.add(new BlockPos(highX, pos.getY(), zSplit + 1));

						zSplit = lowZ + zLength / 3;
						int zSplit2 = lowZ + 2 * zLength / 3;

						threeSplitCornersList1.add(new BlockPos(lowX, pos.getY(), lowZ));
						threeSplitCornersList1.add(new BlockPos(highX, pos.getY(), lowZ));
						threeSplitCornersList1.add(new BlockPos(lowX, pos.getY(), zSplit));
						threeSplitCornersList1.add(new BlockPos(highX, pos.getY(), zSplit));

						threeSplitCornersList2.add(new BlockPos(lowX, pos.getY(), zSplit + 1));
						threeSplitCornersList2.add(new BlockPos(highX, pos.getY(), zSplit + 1));
						threeSplitCornersList2.add(new BlockPos(lowX, pos.getY(), zSplit2));
						threeSplitCornersList2.add(new BlockPos(highX, pos.getY(), zSplit2));

						threeSplitCornersList3.add(new BlockPos(lowX, pos.getY(), zSplit2 + 1));
						threeSplitCornersList3.add(new BlockPos(highX, pos.getY(), zSplit2 + 1));
						threeSplitCornersList3.add(new BlockPos(lowX, pos.getY(), highZ));
						threeSplitCornersList3.add(new BlockPos(highX, pos.getY(), highZ));
					}

					Pair<Integer, ArrayList<StructurePiece>> twoSplitSolution1 = fillWithBuildingsRecurse(
							templateManager, twoSplitCornersList1, pos);
					Pair<Integer, ArrayList<StructurePiece>> twoSplitSolution2 = fillWithBuildingsRecurse(
							templateManager, twoSplitCornersList2, pos);
					int twoSplitCost = twoSplitSolution1.getFirst() + twoSplitSolution2.getFirst();

//					Pair<Integer, ArrayList<StructurePiece>> threeSplitSolution1 = fillWithBuildingsRecurse(
//							templateManager, threeSplitCornersList1, pos);
//					Pair<Integer, ArrayList<StructurePiece>> threeSplitSolution2 = fillWithBuildingsRecurse(
//							templateManager, threeSplitCornersList2, pos);
//					Pair<Integer, ArrayList<StructurePiece>> threeSplitSolution3 = fillWithBuildingsRecurse(
//							templateManager, threeSplitCornersList3, pos);
//					int threeSplitCost = threeSplitSolution1.getFirst() + threeSplitSolution2.getFirst()
//							+ threeSplitSolution3.getFirst();

//					if (twoSplitCost <= threeSplitCost) {
						ArrayList<StructurePiece> tempPieceList = twoSplitSolution1.getSecond();
						tempPieceList.addAll(twoSplitSolution2.getSecond());
						return new Pair<Integer, ArrayList<StructurePiece>>(twoSplitCost, tempPieceList);
//					}
//					else {
//						ArrayList<StructurePiece> tempPieceList = threeSplitSolution1.getSecond();
//						tempPieceList.addAll(threeSplitSolution2.getSecond());
//						tempPieceList.addAll(threeSplitSolution3.getSecond());
//						return new Pair<Integer, ArrayList<StructurePiece>>(threeSplitCost, tempPieceList);
//					}
				}
				
				// Fill Square with a building
				int buildingIndex = 0;
//				System.out.println("Hello boy time\n" + longLength + "," + shortLength);
				while (buildingIndex < DESERT_BUILDING.values().length - 1
						&& (DESERT_BUILDING.values()[buildingIndex].xSize < longLength
						|| DESERT_BUILDING.values()[buildingIndex].zSize < shortLength)) {
//					System.out.println(DESERT_BUILDING.values()[buildingIndex].xSize + "," + DESERT_BUILDING.values()[buildingIndex].zSize);
					buildingIndex++;
				}
				DESERT_BUILDING building = DESERT_BUILDING.values()[buildingIndex];
//				System.out.println("I've settled on " + buildingIndex);
				int cost = longLength - building.xSize + shortLength - building.zSize + 1;

				//TODO fix so as not to use Math.random
				Rotation rot = (flip ? (Math.random() < 0.5 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90)
						: (Math.random() < 0.5 ? Rotation.NONE : Rotation.CLOCKWISE_180));
				BlockPos buildingPos = new BlockPos(
						pos.getX() - 80 + (rot.equals(Rotation.NONE) || rot.equals(Rotation.COUNTERCLOCKWISE_90) ? lowX : highX), 
						pos.getY() + 1, 
						pos.getZ() - 80 + (rot.equals(Rotation.NONE) || rot.equals(Rotation.CLOCKWISE_90) ? lowZ : highZ));

				StructurePiece piece = new AbstractCityPieces.Piece(templateManager, building.resourceLocation(),
						buildingPos, rot)/*building.offset(buildingPos, rot), rot)*/;
				ArrayList<StructurePiece> tempPieceList = new ArrayList<>();
				tempPieceList.add(piece);
				for(int i = 1; i < longLength-8; i++) {
					tempPieceList.add(new AbstractCityPieces.Piece(templateManager, building.resourceLocation(),
							buildingPos.above(i), rot));
				}
				for(BlockPos c: cornerList) {
					for(int i = pos.getY(); i < pos.getY() + 4; i++)
						tempPieceList.add(new AbstractCityPieces.Piece(templateManager, DesertCityPieces.WOOL[((building.xSize + building.zSize) % 32)/2],
								new BlockPos(c.getX() + pos.getX() - 80, i, c.getZ() + pos.getZ() - 80), Rotation.NONE));
				}
				return new Pair<Integer, ArrayList<StructurePiece>>(cost, tempPieceList);
			}
		}

//		private static Pair<Integer, ArrayList<StructurePiece>> fillWithBuildingsSquare() {
//			
//		}
	}
}

/*
 * ArrayList<StructurePiece> f = new ArrayList<>(); for(BlockPos h : cornerList)
 * { f.add(new Point(h.getX(), h.getZ())); }
 * 
 * return new Pair<Integer, ArrayList<StructurePiece>>(1, f);
 */
