package com.iosenberg.polisproject.init;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.iosenberg.polisproject.PolisProject;
import com.iosenberg.polisproject.structure.RoadFeature;
import com.iosenberg.polisproject.structure.RoadJunctionStructure;
import com.iosenberg.polisproject.structure.RoadJunctionStructurePiece;
import com.iosenberg.polisproject.structure.city.AbstractCityPieces;
import com.iosenberg.polisproject.structure.city.CityStructure;
import com.iosenberg.polisproject.structure.city.DebugCityPieces;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class PPStructures {
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES,  PolisProject.MODID);
	
	public static Feature<NoFeatureConfig> ROAD_FEATURE = new RoadFeature(NoFeatureConfig.CODEC);
	public static final RegistryObject<Feature<NoFeatureConfig>> ROADFEATUREREGISTRY = createFeature("road_feature",() -> ROAD_FEATURE);
	
	public static Structure<NoFeatureConfig> ROAD_JUNCTION = new RoadJunctionStructure(NoFeatureConfig.CODEC);
	public static Structure<NoFeatureConfig> CITY = new CityStructure(NoFeatureConfig.CODEC);
	public static IStructurePieceType ROAD_JUNCTION_PIECE = RoadJunctionStructurePiece.Piece::new;
	public static IStructurePieceType CITY_PIECE = AbstractCityPieces.Piece::new;
	
	public static void registerStructures(Register<Structure<?>> event) {
		PolisProject.register(event.getRegistry(), ROAD_JUNCTION, "road_junction");
		registerStructure(ROAD_JUNCTION, new StructureSeparationSettings(15, 7, 70031124), true);
		PolisProject.register(event.getRegistry(), CITY, "city");
		registerStructure(CITY, new StructureSeparationSettings(60, 40, 2352352), true); //fix to false for no terrain generation
		
		PPStructures.registerAllPieces();
	}
	
	public static <F extends Structure<?>> void registerStructure(F structure, StructureSeparationSettings structureSeparationSettings, boolean transformSurroundingLand) {
		Structure.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);
		
		if(transformSurroundingLand) {
			Structure.NOISE_AFFECTING_FEATURES = ImmutableList.<Structure<?>> builder()
					.addAll(Structure.NOISE_AFFECTING_FEATURES)
					.add(structure)
					.build();
		}
		
		DimensionStructuresSettings.DEFAULTS = 
				ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
					.putAll(DimensionStructuresSettings.DEFAULTS)
					.put(structure, structureSeparationSettings)
					.build();
	}
	
	public static void registerAllPieces() {
		registerStructurePiece(ROAD_JUNCTION_PIECE, new ResourceLocation(PolisProject.MODID, "road_junction_piece"));
		registerStructurePiece(CITY_PIECE, new ResourceLocation(PolisProject.MODID, "city_piece"));
	}
	
	static void registerStructurePiece(IStructurePieceType structurePiece, ResourceLocation rl) {
		Registry.register(Registry.STRUCTURE_PIECE,  rl,  structurePiece);
	}
	
	private static <F extends Feature<?>> RegistryObject<F> createFeature(String name, Supplier<F> feature) {
		return FEATURES.register(name, feature);
	}
	
//	private static <F extends Feature<?>> RegistryObject<F> createFeature(String name, Supplier<F> feature) {
//		return FEATURES.register(name, feature);
//	}
}
