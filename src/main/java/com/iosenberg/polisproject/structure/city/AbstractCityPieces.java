package com.iosenberg.polisproject.structure.city;

import java.util.Random;

import com.iosenberg.polisproject.init.PPStructures;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class AbstractCityPieces {

	
	public static class Piece extends TemplateStructurePiece {
		private ResourceLocation resourceLocation;
		private Rotation rotation;
		
		public Piece(TemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos, Rotation rotationIn) {
			super(PPStructures.CITY_PIECE, 0);
			this.resourceLocation = resourceLocationIn;
			BlockPos blockpos = new BlockPos(0,-1,0);//UndergroundVillagePieces.OFFSET.get(resourceLocation);
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
			PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE);
			this.setup(template, this.templatePosition, placementsettings);
		}
		
		@Override
		protected void addAdditionalSaveData(CompoundNBT tagCompound) {
			super.addAdditionalSaveData(tagCompound);
			tagCompound.putString("Template",  this.resourceLocation.toString());;
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
