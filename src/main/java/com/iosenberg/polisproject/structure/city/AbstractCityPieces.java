package com.iosenberg.polisproject.structure.city;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.dimension.PPWorldSavedData;
import com.iosenberg.polisproject.init.PPStructures;
import com.iosenberg.polisproject.structure.city.CityBuildings.DESERT_BUILDING;
import com.iosenberg.polisproject.utils.GeneralUtils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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
	static byte[][] generateWallsAndRoads(byte[][] mapIn, BlockPos pos, List<StructurePiece> pieceList,
			List<BlockPos> gateList, TemplateManager templateManager, int biome) {
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
		System.out.println("beep beep");
		ArrayList<BlockPos> wallsAndCorners = new ArrayList<>();
		byte[][] newMap = new byte[176 / WALL_SIZE][176 / WALL_SIZE];

		for (int i = 1; i < WALL_MAX - 1; i++) {
			for (int j = 1; j < WALL_MAX - 1; j++) {
				if (map[i][j]) {
					newMap[i][j] = 1;
					for (int k = -1; k < 2 && newMap[i][j] != 2; k++) {
						for (int l = -1; l < 2 && newMap[i][j] != 2; l++) {
							if (!map[i + k][j + l]) {
								wallsAndCorners.add(new BlockPos(i, 0, j));
								newMap[i][j] = 2;
//								break;
							}
						}
//						if (newMap[i][j] == 2)
//							break;
					}
//					if (newMap[i][j] == 2) 
//						break;
				}
			}
		}

		ArrayList<BlockPos> walls = new ArrayList<>();
		ArrayList<BlockPos> wallsForRoadGeneration = new ArrayList<>();

		// Go through again and either place corner in piece list or add wall to
		// wallsList (to determine wall or gate)
		for (BlockPos wall : wallsAndCorners) {
			int i = wall.getX();
			int j = wall.getZ();
			BlockPos wallPos = new BlockPos(pos.getX() + i * WALL_SIZE + WALL_SIZE / 2 - OFFSET, pos.getY() + 1,
					pos.getZ() + j * WALL_SIZE + WALL_SIZE / 2 - OFFSET);
			// If it's not a corner
			if (newMap[i - 1][j] == newMap[i + 1][j] || newMap[i][j - 1] == newMap[i][j + 1]) {
				walls.add(wall);
				wallsForRoadGeneration.add(new BlockPos(wall.getX() * WALL_SIZE + WALL_SIZE / 2, pos.getY() + 1,
						wall.getZ() * WALL_SIZE + WALL_SIZE / 2));
			}
			// If it's a corner
			else {
				Rotation rot = Rotation.NONE;
				if (newMap[i + 1][j] == 2 && newMap[i][j + 1] == 2) {
					rot = Rotation.CLOCKWISE_90;
					wallPos = wallPos.offset(2, 0, -2);
				}
				if (newMap[i - 1][j] == 2 && newMap[i][j + 1] == 2) {
					rot = Rotation.CLOCKWISE_180;
					wallPos = wallPos.offset(2, 0, 2);
				}
				if (newMap[i - 1][j] == 2 && newMap[i][j - 1] == 2) {
					rot = Rotation.COUNTERCLOCKWISE_90;
					wallPos = wallPos.offset(-2, 0, 2);
				}
				// If no change/if still none rotation
				if (rot.equals(Rotation.NONE))
					wallPos = wallPos.offset(-2, 0, -2);
				pieceList.add(new AbstractCityPieces.Piece(templateManager,
						new ResourceLocation(PolisProject.MODID, "desert_city/wall_corner_1"), wallPos, rot));
			}

		}

		// Generate roads and gates using wall data
		ArrayList<BlockPos> roads = new ArrayList<BlockPos>();
		HashMap<String, BlockPos> gates = new HashMap<>();
		generateCityRoads(pos, wallsForRoadGeneration, roads, gates);

		// Go through again and either place corner in piece list or add wall to
		// wallsList (to determine wall or gate)
		for (BlockPos wall : walls) {
			int i = wall.getX();
			int j = wall.getZ();
			BlockPos wallPos = new BlockPos(pos.getX() + i * WALL_SIZE + WALL_SIZE / 2 - OFFSET, pos.getY() + 1,
					pos.getZ() + j * WALL_SIZE + WALL_SIZE / 2 - OFFSET);
			// TODO FIX THIS IT'S A REALLY MESSY SYSTEM
			// TODO ALSO HOW DOES THIS WORK????? WITH ADDING THE OFFSET???
			BlockPos gateLocation = new BlockPos(wallPos.getX() - pos.getX() + OFFSET, wallPos.getY(),
					wallPos.getZ() - pos.getZ() + OFFSET);
			// If it's not a corner
			Rotation rot = Rotation.NONE;
			if (newMap[i][j - 1] == 0) {
				rot = Rotation.CLOCKWISE_90;
				wallPos = wallPos.offset(2, 0, -1);
			}
			if (newMap[i + 1][j] == 0) {
				rot = Rotation.CLOCKWISE_180;
				wallPos = wallPos.offset(1, 0, 2);
			}
			if (newMap[i][j + 1] == 0) {
				rot = Rotation.COUNTERCLOCKWISE_90;
				wallPos = wallPos.offset(-2, 0, 1);
			}
			// If no change/if still none rotation
			if (rot.equals(Rotation.NONE))
				wallPos = wallPos.offset(-1, 0, -2);
//			System.out.println(gateLocation.toShortString());
			if (gates.containsKey(gateLocation.toShortString())) {
				gateList.add(gateLocation);
				pieceList.add(new AbstractCityPieces.Piece(templateManager,
						new ResourceLocation(PolisProject.MODID, "desert_city/wall_gate_1"), wallPos, rot));
			} else
				pieceList.add(new AbstractCityPieces.Piece(templateManager,
						new ResourceLocation(PolisProject.MODID, "desert_city/wall_1"), wallPos, rot));
		}

		for (BlockPos road : roads) {
			System.out.println((pos.getX() + road.getX() - OFFSET) + "," + (pos.getZ() + road.getZ() - OFFSET));
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					BlockPos newPos = new BlockPos(pos.getX() + road.getX() - OFFSET + i, pos.getY(),
							pos.getZ() + road.getZ() - OFFSET + j);
					pieceList.add(new AbstractCityPieces.Piece(templateManager,
							new ResourceLocation(PolisProject.MODID, "street"), newPos, Rotation.NONE));
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

	private static void generateCityRoads(BlockPos pos, ArrayList<BlockPos> walls, ArrayList<BlockPos> roads,
			HashMap<String, BlockPos> gates) {

		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);

		// Checks outermost row of chunks in each direction
		for (int i = 0; i < 12; i++) {

			// North (-z)
			ChunkPos chunk = new ChunkPos(chunkPos.x - 5 + i, chunkPos.z - 5);
			CompoundNBT road = PPWorldSavedData.getRoad(chunk);
			// If there is a roadchunk there, find the nearest non-corner wall, add it to
			// gates, then
			if (road != null) {
				int minX = 0 + i * 16;
				int maxX = minX + 15;
				BlockPos chunkAsBlockPos = new BlockPos(minX + 8, 0, 8); // z = middle block of smallest chunk
				roads.add(chunkAsBlockPos);
				System.out.println("NORTH SNEE SNEE: " + chunkAsBlockPos.toString());

				// Closest is the closest wall to the road
				// If a wall is found that is within the x range of the chunk, withinChunk is
				// set to true, end
				// then only walls within that range are considered
				BlockPos closest = new BlockPos(-200, 0, -200);
				boolean withinChunk = false;
				for (BlockPos wall : walls) {
					if (withinChunk) {
						if (wall.getX() > minX && wall.getX() < maxX
								&& wall.distManhattan(chunkAsBlockPos) < closest.distManhattan(chunkAsBlockPos)) {
							closest = wall;
						}
					} else {
						if (wall.getX() > minX && wall.getX() < maxX) {
							withinChunk = true;
							closest = wall;
						} else if (wall.distManhattan(chunkAsBlockPos) < closest.distManhattan(chunkAsBlockPos)) {
							closest = wall;
						}
					}
				}
				gates.put(closest.toShortString(), closest);

				// If the road is direct, connect the road
				if (withinChunk) {
					BlockPos roadPos = closest.south();
					while (roadPos.getZ() != 0) {
						roads.add(roadPos);
						roadPos = roadPos.north();
					}
					PPWorldSavedData.updateRoadStarts(chunk,
							new ChunkPos[] { new ChunkPos(roadPos.getX() % 16, roadPos.getZ()) });
				}
			}

			// South (+z)
			chunk = new ChunkPos(chunkPos.x - 5 + i, chunkPos.z + 6);
			road = PPWorldSavedData.getRoad(chunk);
			if (road != null) {
				int minX = 0 + i * 16;
				int maxX = minX + 15;
				BlockPos chunkAsBlockPos = new BlockPos(minX + 8, 0, 168); // z = middle block of largest chunk
				roads.add(chunkAsBlockPos);
				System.out.println("SOUTH SNEE SNEE: " + chunkAsBlockPos.toString());

				BlockPos closest = new BlockPos(-200, 0, -200);
				boolean withinChunk = false;
				for (BlockPos wall : walls) {
					if (withinChunk) {
						if (wall.getX() > minX && wall.getX() < maxX
								&& wall.distManhattan(chunkAsBlockPos) < closest.distManhattan(chunkAsBlockPos)) {
							closest = wall;
						}
					} else {
						if (wall.getX() > minX && wall.getX() < maxX) {
							withinChunk = true;
							closest = wall;
						} else if (wall.distManhattan(chunkAsBlockPos) < closest.distManhattan(chunkAsBlockPos)) {
							closest = wall;
						}
					}
				}
				gates.put(closest.toShortString(), closest);

				// If the road is direct, connect the road
				if (withinChunk) {
					BlockPos roadPos = closest.north();
					while (roadPos.getZ() != 175) {
						roads.add(roadPos);
						roadPos = roadPos.south();
					}
					PPWorldSavedData.updateRoadStarts(chunk,
							new ChunkPos[] { new ChunkPos(roadPos.getX() % 16, roadPos.getZ()) });
				}
			}

			// West (-x)
			chunk = new ChunkPos(chunkPos.x - 5, chunkPos.z - 5 + i);
			road = PPWorldSavedData.getRoad(chunk);
			if (road != null) {
				int minZ = 0 + i * 16;
				int maxZ = minZ + 15;
				BlockPos chunkAsBlockPos = new BlockPos(8, 0, minZ + 8); // z = middle block of largest chunk
				roads.add(chunkAsBlockPos);
				System.out.println("WEST SNEE SNEE: " + chunkAsBlockPos.toString());

				BlockPos closest = new BlockPos(-200, 0, -200);
				boolean withinChunk = false;
				for (BlockPos wall : walls) {
					if (withinChunk) {
						if (wall.getZ() > minZ && wall.getZ() < maxZ
								&& wall.distManhattan(chunkAsBlockPos) < closest.distManhattan(chunkAsBlockPos)) {
							closest = wall;
						}
					} else {
						if (wall.getZ() > minZ && wall.getZ() < maxZ) {
							withinChunk = true;
							closest = wall;
						} else if (wall.distManhattan(chunkAsBlockPos) < closest.distManhattan(chunkAsBlockPos)) {
							closest = wall;
						}
					}
				}
				gates.put(closest.toShortString(), closest);

				// If the road is direct, just connect the road
				if (withinChunk) {
					BlockPos roadPos = closest.east();
					while (roadPos.getX() != 0) {
						roads.add(roadPos);
						roadPos = roadPos.west();
					}
					PPWorldSavedData.updateRoadStarts(chunk,
							new ChunkPos[] { new ChunkPos(roadPos.getX(), roadPos.getZ() % 16) });
				}
			}

			// East (+x)
			chunk = new ChunkPos(chunkPos.x + 6, chunkPos.z - 5 + i);
			road = PPWorldSavedData.getRoad(chunk);
			if (road != null) {
				int minZ = 0 + i * 16;
				int maxZ = minZ + 15;
				BlockPos chunkAsBlockPos = new BlockPos(168, 0, minZ + 8); // z = middle block of largest chunk
				roads.add(chunkAsBlockPos);
				System.out.println("EAST SNEE SNEE: " + chunkAsBlockPos.toString());

				BlockPos closest = new BlockPos(-200, 0, -200);
				boolean withinChunk = false;
				for (BlockPos wall : walls) {
					if (withinChunk) {
						if (wall.getZ() > minZ && wall.getZ() < maxZ
								&& wall.distManhattan(chunkAsBlockPos) < closest.distManhattan(chunkAsBlockPos)) {
							closest = wall;
						}
					} else {
						if (wall.getZ() > minZ && wall.getZ() < maxZ) {
							withinChunk = true;
							closest = wall;
						} else if (wall.distManhattan(chunkAsBlockPos) < closest.distManhattan(chunkAsBlockPos)) {
							closest = wall;
						}
					}
				}
				gates.put(closest.toShortString(), closest);

				// If the road is direct, just connect the road
				if (withinChunk) {
					BlockPos roadPos = closest.west();
					while (roadPos.getX() != 175) {
						roads.add(roadPos);
						roadPos = roadPos.east();
					}
					PPWorldSavedData.updateRoadStarts(chunk,
							new ChunkPos[] { new ChunkPos(roadPos.getX(), roadPos.getZ() % 16) });
				}
			}

		}

	}

	static byte[][] generateAllStreets(byte[][] mapIn, BlockPos pos, BlockPos[] connectionPoints,
			List<StructurePiece> pieceList, TemplateManager templateManager, int biome) {
		byte[][] weightMap = new byte[176][176];
		for (int i = 0; i < 176; i++) {
			for (int j = 0; j < 176; j++) {
				if (mapIn[i][j] == 3)
					weightMap[i][j] = 0;
				else if (mapIn[i][j] == 1) {
					weightMap[i][j] = 2;
				} else
					weightMap[i][j] = Byte.MAX_VALUE;
			}
		}

		for (int i = 0; i < connectionPoints.length - 1; i++) {
			for (int j = i; j < connectionPoints.length; j++) {
				ChunkPos s = new ChunkPos(connectionPoints[i].getX(), connectionPoints[i].getZ());
				ChunkPos d = new ChunkPos(connectionPoints[j].getX(), connectionPoints[j].getZ());
				ChunkPos[] roads = generateStreet(weightMap, s, d);

				for (ChunkPos r : roads) {
					mapIn[r.x][r.z] = 3;
					weightMap[r.x][r.z] = 0;
					BlockPos streetPos = new BlockPos(r.x + pos.getX() - OFFSET, pos.getY(), r.z + pos.getZ() - OFFSET);
					pieceList.add(new AbstractCityPieces.Piece(templateManager,
							new ResourceLocation(PolisProject.MODID, "street"), streetPos, Rotation.NONE));
				}
			}
		}

		return mapIn;
	}

	// Another A* algorithm. Works with chunkposes instead of blockposes so there's
	// no extraneous y wasting data
	private static ChunkPos[] generateStreet(byte[][] weightMap, ChunkPos s, ChunkPos d) {
		int xMax = weightMap.length;
		int zMax = weightMap[0].length;

		boolean[][] visited = new boolean[xMax][zMax];
		int[][] distance = new int[xMax][zMax];
		ChunkPos[][] previousChunk = new ChunkPos[xMax][zMax];
		previousChunk[s.x][s.z] = s;
		for (int i = 0; i < xMax; i++) {
			for (int j = 0; j < zMax; j++) {
				distance[i][j] = Integer.MAX_VALUE;
			}
		}

		PriorityQueue<ChunkPos> queue = new PriorityQueue<ChunkPos>((a, b) -> distance[a.x][a.z]
				+ GeneralUtils.ManhattanDistance(a, d) - distance[b.x][b.z] - GeneralUtils.ManhattanDistance(b, d));
		distance[s.x][s.z] = 0;
		queue.offer(s);
		visited[s.x][s.z] = true;
		while (!visited[d.x][d.z]) {
			ChunkPos pos = queue.poll();
			visited[pos.x][pos.z] = true;
			if (pos.x > 0 && !visited[pos.x - 1][pos.z]) {
				int tx = pos.x - 1; // target x
				int tz = pos.z; // target z

				// The distance is the weight of the target pos plus an additional cost if
				// target is not in a straight line with pos and previous pos
				int tempDist = weightMap[tx][tz] + distance[pos.x][pos.z] + 1;
				if (tx != previousChunk[pos.x][pos.z].x && tz != previousChunk[pos.x][pos.z].z)
					tempDist += 2;
				if (tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx, tz);
					if (!queue.contains(chunk))
						queue.offer(chunk);
				}
			}
			if (pos.x < xMax - 1 && !visited[pos.x + 1][pos.z]) {
				int tx = pos.x + 1; // target x
				int tz = pos.z; // target z

				// The distance is the weight of the target pos plus an additional cost if
				// target is not in a straight line with pos and previous pos
				int tempDist = weightMap[tx][tz] + distance[pos.x][pos.z] + 1;
				if (tx != previousChunk[pos.x][pos.z].x && tz != previousChunk[pos.x][pos.z].z)
					tempDist += 2;
				if (tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx, tz);
					if (!queue.contains(chunk))
						queue.offer(chunk);
				}
			}
			if (pos.z > 0 && !visited[pos.x][pos.z - 1]) {
				int tx = pos.x; // target x
				int tz = pos.z - 1; // target z

				// The distance is the weight of the target pos plus an additional cost if
				// target is not in a straight line with pos and previous pos
				int tempDist = weightMap[tx][tz] + distance[pos.x][pos.z] + 1;
				if (tx != previousChunk[pos.x][pos.z].x && tz != previousChunk[pos.x][pos.z].z)
					tempDist += 2;
				if (tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx, tz);
					if (!queue.contains(chunk))
						queue.offer(chunk);
				}
			}
			if (pos.z < zMax - 1 && !visited[pos.x][pos.z + 1]) {
				int tx = pos.x; // target x
				int tz = pos.z + 1; // target z

				// The distance is the weight of the target pos plus an additional cost if
				// target is not in a straight line with pos and previous pos
				int tempDist = weightMap[tx][tz] + distance[pos.x][pos.z] + 1;
				if (tx != previousChunk[pos.x][pos.z].x && tz != previousChunk[pos.x][pos.z].z)
					tempDist += 2;
				if (tempDist < distance[tx][tz]) {
					distance[tx][tz] = tempDist;
					previousChunk[tx][tz] = pos;
					ChunkPos chunk = new ChunkPos(tx, tz);
					if (!queue.contains(chunk))
						queue.offer(chunk);
				}
			}

		}

		// Finished algorithm. The path is stored in previousChunk[], which will be read
		// into an ArrayList path
		ArrayList<ChunkPos> path = new ArrayList<ChunkPos>();
		ChunkPos chunk = d;
		while (!chunk.equals(s)) {
			path.add(chunk);
			chunk = previousChunk[chunk.x][chunk.z];
		}

		return path.toArray(new ChunkPos[0]);
	}

	static ArrayList<Point> generateBuildings(byte[][] cityMap) {
		final int height = 0;
		ArrayList<Point> buildingsList = new ArrayList<>();
		for (int i = 0; i < 176; i++) {
			for (int j = 0; j < 176; j++) {
				// Find the corners of every section of 1s surrounded by not 1s
				if (cityMap[i][j] == 1) {
					ArrayList<BlockPos> corners = new ArrayList<BlockPos>(4);

					LinkedList<BlockPos> queue = new LinkedList<BlockPos>();
					queue.add(new BlockPos(i, height, j));
					cityMap[i][j] = 4;

					// Breadth first search, adding corners to list of corners and setting all cells
					// == 1 to 4
					// We don't need to worry about out of bounds because the outer 4 blocks cannot
					// equal 1 and cannot be queued
					while (!queue.isEmpty()) {
						BlockPos queuePos = queue.poll();

						// check -x neighbor
						int x = queuePos.getX() - 1;
						int z = queuePos.getZ();
						if (cityMap[x][z] == 1) {
							cityMap[x][z] = 4;
							queue.add(new BlockPos(x, height, z));
						}

						// check +x neighbor
						x = queuePos.getX() + 1;
//						z is already set
						if (cityMap[x][z] == 1) {
							cityMap[x][z] = 4;
							queue.add(new BlockPos(x, height, z));
						}

						// check -z neighbor
						x = queuePos.getX();
						z = queuePos.getZ() - 1;
						if (cityMap[x][z] == 1) {
							cityMap[x][z] = 4;
							queue.add(new BlockPos(x, height, z));
						}

						// check +z neighbor
						// x is already set
						z = queuePos.getZ() + 1;
						if (cityMap[x][z] == 1) {
							cityMap[x][z] = 4;
							queue.add(new BlockPos(x, height, z));
						}

						// This took a lot of trial and error. TODO figure out how to explain later.
						x = queuePos.getX();
						z = queuePos.getZ();
						if (cityMap[x][z] == 4) {
							int threeCorners = 0; // number of size 3 corners where all cells in the corner == 1 or == 4
							// -x -z corner
							if (isOneorFour(cityMap[x - 1][z - 1]) && isOneorFour(cityMap[x][z - 1])
									&& isOneorFour(cityMap[x - 1][z]))
								threeCorners++;
							// +x -z corner
							if (isOneorFour(cityMap[x + 1][z - 1]) && isOneorFour(cityMap[x][z - 1])
									&& isOneorFour(cityMap[x + 1][z]))
								threeCorners++;
							// -x +z corner
							if (isOneorFour(cityMap[x - 1][z + 1]) && isOneorFour(cityMap[x][z + 1])
									&& isOneorFour(cityMap[x - 1][z]))
								threeCorners++;
							// +x +z corner
							if (isOneorFour(cityMap[x + 1][z + 1]) && isOneorFour(cityMap[x][z + 1])
									&& isOneorFour(cityMap[x + 1][z]))
								threeCorners++;

							if (threeCorners == 1 || threeCorners == 3)
								corners.add(queuePos);
						}
					}

					// If more than 20 corners, the buildings filler will run forever, so it splits
					// them in half then runs the corner search again, setting all the cells to 1,
					// and decrements j to run the whole step again.
					if (corners.size() > 15) {
						int xMax = 0;
						int xMin = 176;
						int zMax = 0;
						int zMin = 176;
						for (BlockPos c : corners) {
							if (c.getX() > xMax)
								xMax = c.getX();
							if (c.getX() < xMin)
								xMin = c.getX();
							if (c.getZ() > zMax)
								zMax = c.getZ();
							if (c.getZ() < zMin)
								zMin = c.getZ();
							cityMap[c.getX()][c.getZ()] = 1;
							queue.add(c);
						}

						// Wherever the polygon is widest (x or z), divide the polygon in half along
						// that axis
						if (xMax - xMin > zMax - zMin) {
							int middle = (xMax + xMin) / 2;
							for (int k = 0; k < 176; k++) {
								if (cityMap[middle][k] == 4)
									cityMap[middle][k] = 5;
							}
						} else {
							int middle = (zMax + zMin) / 2;
							for (int k = 0; k < 176; k++) {
								if (cityMap[k][middle] == 4)
									cityMap[k][middle] = 5;
							}
						}

						while (!queue.isEmpty()) {
							BlockPos queuePos = queue.poll();
							// check -x neighbor
							int x = queuePos.getX() - 1;
							int z = queuePos.getZ();
							if (cityMap[x][z] == 4) {
								cityMap[x][z] = 1;
								queue.add(new BlockPos(x, height, z));
							}
							// check +x neighbor
							x = queuePos.getX() + 1;
//							z is already set
							if (cityMap[x][z] == 4) {
								cityMap[x][z] = 1;
								queue.add(new BlockPos(x, height, z));
							}
							// check -z neighbor
							x = queuePos.getX();
							z = queuePos.getZ() - 1;
							if (cityMap[x][z] == 4) {
								cityMap[x][z] = 1;
								queue.add(new BlockPos(x, height, z));
							}
							// check +z neighbor
							// x is already set
							z = queuePos.getZ() + 1;
							if (cityMap[x][z] == 4) {
								cityMap[x][z] = 1;
								queue.add(new BlockPos(x, height, z));
							}
						}
						// Decrement j to restart corner search.
						j--;
					} else {
						// TODO Figure out a generic way to do this, instead of DESERT_BUILDING specific
						System.out.println("I'm a new set of buildings! at " + i + "," + j);
						buildingsList.addAll(DESERT_BUILDING.fillWithBuildings(/* templateManager */true, corners));
					}
				}
			}
		}
		return buildingsList;
	}

	/*
	 * I hate it here
	 */
	private static boolean isOneorFour(int n) {
		return n == 1 || n == 4;
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
