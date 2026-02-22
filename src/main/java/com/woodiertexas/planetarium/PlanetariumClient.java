package com.woodiertexas.planetarium;

import java.util.Map;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class PlanetariumClient implements ClientModInitializer {
	private final PlanetManager planetManager = new PlanetManager();

	@Override
	public void onInitializeClient() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(
			Identifier.parse(Planetarium.MOD_ID + ":planet_reloader"),
			planetManager
		);

		WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
			Minecraft mc = Minecraft.getInstance();
			ClientLevel world = mc.level;
			if (world == null) return;

			MultiBufferSource consumers = context.consumers();
			if (consumers == null) return;

			float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

			for (Map.Entry<Identifier, PlanetInfo> entry : planetManager.getPlanets().entrySet()) {
				Identifier texture = entry.getValue().getTexture(entry.getKey());
				Planetarium.renderPlanet(context.matrices(), consumers, texture, entry.getValue(), tickDelta, world);
			}
		});
	}
}
