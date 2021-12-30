package com.iosenberg.polisproject.structure.city;

import java.util.List;
import java.util.Random;

import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.init.PPStructures;
import com.iosenberg.polisproject.structure.RoadJunctionStructurePiece;

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
	static final ResourceLocation BLOCK = new ResourceLocation(PolisProject.MODID, "block");
	static final ResourceLocation ROADJUNCTION = new ResourceLocation(PolisProject.MODID, "road_junction");
	static byte[][] map;
	static int height;

	public static byte[][] generateMap(ChunkGenerator generator, int chunkX, int chunkZ) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void map(byte[][] mapIn) {
		map = mapIn;
	}
	
	public static void height(int H) {
		height = H;
	}

	public static void generatePieces(TemplateManager templateManager, BlockPos pos, Rotation rotation,
			List<StructurePiece> pieceList, Random random) {

		
		int size = 5;
		for(int i = 0-size; i < size+1; i++) {
			for(int j = 0-size; j < size+1; j++) {
				int chunki = i * 16;
				int chunkj = j * 16;
				pieceList.add(new Piece(templateManager, BLOCK, new BlockPos(pos.getX() + chunki, height, pos.getZ() + chunkj), rotation));
			}
		}
		
	}

	//quick fix. need to put this somewhere else
	public static class Piece extends TemplateStructurePiece {
		private ResourceLocation resourceLocation;
		private Rotation rotation;
		
		public Piece(TemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos, Rotation rotationIn) {
			super(PPStructures.BLOCKPIECE, 0);
			this.resourceLocation = resourceLocationIn;
			this.templatePosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
			this.rotation = rotationIn;
			this.setupPiece(templateManagerIn);
		}
		
		public Piece(TemplateManager templateManagerIn, CompoundNBT tagCompound) {
			super(PPStructures.BLOCKPIECE, tagCompound);
			this.resourceLocation = new ResourceLocation(tagCompound.getString("Template"));
			this.rotation = Rotation.NONE;//Rotation.valueOf(tagCompound.getString("Rot"));
			this.setupPiece(templateManagerIn);
		}
		
		private void setupPiece(TemplateManager templateManager) {
			Template template = templateManager.getOrCreate(this.resourceLocation);
			PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE);
			this.setup(template, this.templatePosition, placementsettings);
		}
		
		protected void readAdditionalSaveData(CompoundNBT tagCompound) {
			super.addAdditionalSaveData(tagCompound);
			tagCompound.putString("Template", this.resourceLocation.toString());;
			tagCompound.putString("Rot", this.rotation.name());
		}

		@Override
		protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand,
				MutableBoundingBox sbb) {
			// TODO Auto-generated method stub
			
		}
	}
	
}
