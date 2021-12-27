package com.iosenberg.polisproject.structure;

import java.util.List;
import java.util.Random;

import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.init.PPStructures;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class RoadJunctionStructurePiece {
	private static final ResourceLocation ROAD_JUNCTION = new ResourceLocation(PolisProject.MODID, "road_junction");
	
	public static void start(TemplateManager templateManager, BlockPos pos, Rotation rotation, List<StructurePiece> pieceList, Random random) {
		pieceList.add(new RoadJunctionStructurePiece.Piece(templateManager, ROAD_JUNCTION, pos, rotation));
	}
	
	public static class Piece extends TemplateStructurePiece {
		private ResourceLocation resourceLocation;
		private Rotation rotation;
		
		public Piece(TemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos, Rotation rotationIn) {
			super(PPStructures.ROAD_JUNCTION_PIECE, 0);
			this.resourceLocation = resourceLocationIn;
			this.templatePosition = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
			this.rotation = rotationIn;
			this.setupPiece(templateManagerIn);
		}
		
		public Piece(TemplateManager templateManagerIn, CompoundNBT tagCompound) {
			super(PPStructures.ROAD_JUNCTION_PIECE, tagCompound);
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
