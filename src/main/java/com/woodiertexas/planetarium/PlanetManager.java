package com.woodiertexas.planetarium;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;

public class PlanetManager extends SimpleJsonResourceReloadListener<JsonElement> {
	private static final FileToIdConverter CONVERTER = FileToIdConverter.json(Planetarium.MOD_ID + "/planets");
	private Map<Identifier, PlanetInfo> planets = Map.of();

	public PlanetManager() {
		super(ExtraCodecs.JSON, CONVERTER);
	}

	public Map<Identifier, PlanetInfo> getPlanets() {
		return planets;
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> cache, ResourceManager manager, ProfilerFiller profiler) {
		Map<Identifier, PlanetInfo> planets = new HashMap<>();

		profiler.push("Load Planets");
		for (Map.Entry<Identifier, JsonElement> resourceEntry : cache.entrySet()) {
			Identifier id = resourceEntry.getKey();
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
}
