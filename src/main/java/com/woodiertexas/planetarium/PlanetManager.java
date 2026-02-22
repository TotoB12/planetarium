package com.woodiertexas.planetarium;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class PlanetManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
	private static final Gson GSON = new GsonBuilder().create();
	private Map<ResourceLocation, PlanetInfo> planets = Map.of();

	public PlanetManager() {
		super(GSON, Planetarium.MOD_ID + "/planets");
	}

	public Map<ResourceLocation, PlanetInfo> getPlanets() {
		return planets;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> cache, ResourceManager manager, ProfilerFiller profiler) {
		Map<ResourceLocation, PlanetInfo> planets = new HashMap<>();

		profiler.push("Load Planets");
		for (Map.Entry<ResourceLocation, JsonElement> resourceEntry : cache.entrySet()) {
			ResourceLocation id = resourceEntry.getKey();
			DataResult<Pair<PlanetInfo, JsonElement>> result = PlanetInfo.CODEC.decode(JsonOps.INSTANCE, resourceEntry.getValue());

			if (result.error().isPresent()) {
				Planetarium.LOGGER.error(String.format("Could not parse planet file %s.\nReason: %s", id, result.error().get().message()));
				continue;
			}

			PlanetInfo planetInfo = result.result().get().getFirst();

			if (manager.getResource(planetInfo.getTexture(id)).isEmpty()) {
				Planetarium.LOGGER.error("No texture found for planet {}, skipping.", id);
				continue;
			}

			Planetarium.LOGGER.debug("Adding Planet {}: {}", id, planetInfo);
			planets.put(id, planetInfo);
		}

		profiler.pop();

		this.planets = Map.copyOf(planets);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ResourceLocation.fromNamespaceAndPath(Planetarium.MOD_ID, "planet_reloader");
	}
}
