package com.iosenberg.polisproject;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.iosenberg.polisproject.init.PPConfiguredStructures;
import com.iosenberg.polisproject.init.PPStructures;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod("polisproject")
public class PolisProject {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "polisproject";
	
	public PolisProject() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addGenericListener(Structure.class, this::onRegisterStructures);
		
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addListener(EventPriority.NORMAL, this::addDimensionalSpacing);
		
		forgeBus.addListener(EventPriority.HIGH, this::biomeModification);
	}
	
	public void onRegisterStructures(final RegistryEvent.Register<Structure<?>> event) {
		PPStructures.registerStructures(event);
		PPConfiguredStructures.registerConfiguredStructures();
	}
	
	public void biomeModification(final BiomeLoadingEvent event) {
		event.getGeneration().getStructures().add(() -> PPConfiguredStructures.CONFIGURED_ROAD_JUNCTION);
		event.getGeneration().getStructures().add(() -> PPConfiguredStructures.CONFIGURED_CITY);
		
    	event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> PPConfiguredStructures.CONFIGURED_ROAD_FEATURE);

	}
	
	public void addDimensionalSpacing(final WorldEvent.Load event) {
		if(event.getWorld() instanceof ServerWorld) {
    		ServerWorld serverWorld = (ServerWorld)event.getWorld();
    		
    		if(serverWorld.getChunkSource().getGenerator() instanceof FlatChunkGenerator &&
    				serverWorld.dimension().equals(World.OVERWORLD)) {
    			return;
    		}
    		
    		Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(serverWorld.getChunkSource().generator.getSettings().structureConfig());
    		tempMap.put(PPStructures.ROAD_JUNCTION, DimensionStructuresSettings.DEFAULTS.get(PPStructures.ROAD_JUNCTION));
    		tempMap.put(PPStructures.CITY, DimensionStructuresSettings.DEFAULTS.get(PPStructures.CITY));
    		
            serverWorld.getChunkSource().generator.getSettings().structureConfig = tempMap;
		}
	}
	
	public static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, T entry, String registryKey) {
		entry.setRegistryName(new ResourceLocation(PolisProject.MODID, registryKey));
		registry.register(entry);
		return entry;
	}
}
