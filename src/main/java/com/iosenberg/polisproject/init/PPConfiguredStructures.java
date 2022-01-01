package com.iosenberg.polisproject.init;

import com.iosenberg.polisproject.PolisProject;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class PPConfiguredStructures {
	public static StructureFeature<?,?> CONFIGURED_ROAD_JUNCTION = PPStructures.ROAD_JUNCTION.configured(IFeatureConfig.NONE);
	public static StructureFeature<?,?> CONFIGURED_CITY = PPStructures.CITY.configured(IFeatureConfig.NONE);
//	public static ConfiguredFeature<?,?> CONFIGURED_ROAD_FEATURE = PolisStructures.ROAD_FEATURE.configured(IFeatureConfig.NONE);
	
	public static void registerConfiguredStructures() {		
		Registry<StructureFeature<?,?>> registry = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE;
		
		Registry.register(registry, new ResourceLocation(PolisProject.MODID, "configured_road_junction"),  CONFIGURED_ROAD_JUNCTION);
		Registry.register(registry, new ResourceLocation(PolisProject.MODID, "configured_city"), CONFIGURED_CITY);
		
		FlatGenerationSettings.STRUCTURE_FEATURES.put(PPStructures.ROAD_JUNCTION, CONFIGURED_ROAD_JUNCTION);
		FlatGenerationSettings.STRUCTURE_FEATURES.put(PPStructures.CITY, CONFIGURED_CITY);
	}
}
