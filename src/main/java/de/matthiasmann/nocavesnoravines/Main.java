package de.matthiasmann.nocavesnoravines;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.CanyonWorldCarver;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.UnderwaterCaveWorldCarver;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("nocavesnoravines")
public class Main
{
    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    	ForgeRegistries.BIOMES.forEach(new Consumer<Biome>() {
			@Override
			public void accept(Biome b)
			{
				// Exclude Nether and End biomes
				if (b.getCategory() == Biome.Category.NETHER || b.getCategory() == Biome.Category.THEEND || b.getCategory() == Biome.Category.NONE)
					return;
				
				//Remove vanilla cave / ravine carver
                for(GenerationStage.Carving stage : GenerationStage.Carving.values()) {
				    List<ConfiguredCarver<?>> carvers = b.getCarvers(stage);
				    Iterator<ConfiguredCarver<?>> iter = carvers.iterator();
				    while(iter.hasNext()) {
					    ConfiguredCarver<?> carver = iter.next();
					    if(carver.carver instanceof CanyonWorldCarver ||
                           carver.carver instanceof CaveWorldCarver ||
                           carver.carver instanceof UnderwaterCaveWorldCarver)
						    iter.remove();
					}
				}
            }
        });
    }
}

