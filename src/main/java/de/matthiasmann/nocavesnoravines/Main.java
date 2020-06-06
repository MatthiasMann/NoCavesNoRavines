package de.matthiasmann.nocavesnoravines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.CanyonWorldCarver;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.carver.UnderwaterCanyonWorldCarver;
import net.minecraft.world.gen.carver.UnderwaterCaveWorldCarver;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.LakesFeature;
import net.minecraft.world.gen.feature.LiquidsConfig;
import net.minecraft.world.gen.feature.SpringFeature;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("nocavesnoravines")
public class Main
{
	public static final String MODID = "nocavesnoravines";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

    private final CommonConfiguration commonConfig;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        final Pair<CommonConfiguration, ForgeConfigSpec> com = new ForgeConfigSpec.Builder().configure(builder -> new CommonConfiguration(builder));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, com.getRight());
        commonConfig = com.getLeft();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
		DeferredWorkQueue.runLater(this::modifyBiomes);
    }

    private void modifyBiomes() {
        LOGGER.debug("modifying biomes");

        ForgeRegistries.BIOMES.forEach(b -> {
            // Exclude Nether and End biomes
            if (b.getCategory() == Biome.Category.NETHER || b.getCategory() == Biome.Category.THEEND || b.getCategory() == Biome.Category.NONE)
                return;

            final BlockState LAVA = Blocks.LAVA.getDefaultState();
            final BlockState WATER = Blocks.WATER.getDefaultState();

            for(GenerationStage.Carving stage : GenerationStage.Carving.values()) {
			    b.getCarvers(stage).removeIf(carver ->
                    (commonConfig.removeRavines.get() && carver.carver instanceof CanyonWorldCarver) ||
                    (commonConfig.removeCaves.get() && carver.carver instanceof CaveWorldCarver) ||
                    (commonConfig.removeUnderwaterCaves.get() && carver.carver instanceof UnderwaterCanyonWorldCarver) ||
                    (commonConfig.removeUnderwaterRavines.get() && carver.carver instanceof UnderwaterCaveWorldCarver));
			}

            for(GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
			    b.getFeatures(stage).removeIf(maybe_decorated -> {
				    ConfiguredFeature<?,?> feature = maybe_decorated;
                    if(feature.config instanceof DecoratedFeatureConfig) {
                        DecoratedFeatureConfig decorated = (DecoratedFeatureConfig)feature.config;
                        //LOGGER.debug("Found decorated feature {} with decorator {}", decorated.feature.getClass(), decorated.decorator.getClass());
                        feature = decorated.feature;
                    }
                    LOGGER.debug("Found feature {} with config {}", feature.feature.getClass(), feature.config.getClass());
                    if(feature.feature instanceof LakesFeature && feature.config instanceof BlockStateFeatureConfig) {
                        BlockStateFeatureConfig config = (BlockStateFeatureConfig)feature.config;
                        LOGGER.debug("Found lake with block {}", config.state.getBlock().getTranslationKey());
                        return (commonConfig.removeLakes.get() && config.state == WATER) ||
                               (commonConfig.removeLavaLakes.get() && config.state == LAVA);
                    }
                    if(feature.feature instanceof SpringFeature && feature.config instanceof LiquidsConfig) {
                        LiquidsConfig config = (LiquidsConfig)feature.config;
                        LOGGER.debug("Found spring with fluid {}", config.state.getFluid().getRegistryName());
                        return (commonConfig.removeSprings.get() && config.state.getFluid().isEquivalentTo(Fluids.WATER)) ||
                               (commonConfig.removeLavaSprings.get() && config.state.getFluid().isEquivalentTo(Fluids.LAVA));
                    }
                    return false;
				});
			}
        });
    }

    public static class CommonConfiguration {
        public final ForgeConfigSpec.BooleanValue removeUnderwaterCaves;
        public final ForgeConfigSpec.BooleanValue removeUnderwaterRavines;
        public final ForgeConfigSpec.BooleanValue removeCaves;
        public final ForgeConfigSpec.BooleanValue removeRavines;
        public final ForgeConfigSpec.BooleanValue removeLakes;
        public final ForgeConfigSpec.BooleanValue removeLavaLakes;
        public final ForgeConfigSpec.BooleanValue removeSprings;
        public final ForgeConfigSpec.BooleanValue removeLavaSprings;

        public CommonConfiguration(final ForgeConfigSpec.Builder builder) {
            builder.push("carvers");
            removeUnderwaterCaves = builder.define("removeUnderwaterCaves", true);
            removeUnderwaterRavines = builder.define("removeUnderwaterRavines", true);
            removeCaves = builder.define("removeCaves", true);
            removeRavines = builder.define("removeRavines", true);
            builder.pop();
            builder.push("features");
            removeLakes = builder.define("removeLakes", false);
            removeLavaLakes = builder.define("removeLavaLakes", false);
            removeSprings = builder.define("removeSprings", false);
            removeLavaSprings = builder.define("removeLavaSprings", false);
            builder.pop();
        }
    }
}

