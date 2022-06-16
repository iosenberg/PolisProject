package com.iosenberg.polisproject.structure.city;

import com.iosenberg.polisproject.PolisProject;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class CityAnchors {
	public enum DESERT_ANCHOR {
		FOUNTAIN	("desert_city/fountain", 6, 6, 0),
		GARDEN	("desert_city/garden", 11, 11, 1),
		HOUSE_OF_WISDOM ("desert_city/house_of_wisdom", 17, 18, 2),
		MARKET	("desert_city/market", 11, 11, 0),
		OBELISK	("desert_city/obelisk", 8, 8, 0),
		PLAZA	("desert_city/plaza", 16, 16, 1),
		WELL	("desert_city/well", 4, 4, 0);
		
		final ResourceLocation resourceLocation;
		final int xOffset;
		final int zOffset;
		//0 = starts at 4 corners, 1 = starts at middle of 4 edges, 2 = starts at middle of 2 edges
		final int roadStarts;
		
		DESERT_ANCHOR(String rl, int x, int z, int rs) {
			this.resourceLocation = new ResourceLocation(PolisProject.MODID, rl);
			this.xOffset = x;
			this.zOffset = z;
			this.roadStarts = rs;
		}
		ResourceLocation resourceLocation() {return resourceLocation;}
		BlockPos offset(BlockPos pos, Rotation rot) {
			return pos.offset(
							rot.equals(Rotation.NONE) || rot.equals(Rotation.COUNTERCLOCKWISE_90) ? xOffset * -1 : xOffset,
							0,
							rot.equals(Rotation.NONE) || rot.equals(Rotation.CLOCKWISE_90) ? zOffset * -1 : zOffset);
			}
		BlockPos[] roadStarts(BlockPos pos, Rotation rotation) {
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
}
