package com.woodiertexas.planetarium;

import java.util.Map;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class PlanetariumClient implements ClientModInitializer {
	private final PlanetManager planetManager = new PlanetManager();

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(planetManager);

		WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
			Minecraft mc = Minecraft.getInstance();
			ClientLevel world = mc.level;
			if (world == null) return;

			MultiBufferSource consumers = context.consumers();
			if (consumers == null) return;

			float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

			for (Map.Entry<ResourceLocation, PlanetInfo> entry : planetManager.getPlanets().entrySet()) {
				ResourceLocation texture = entry.getValue().getTexture(entry.getKey());
				Planetarium.renderPlanet(context.matrixStack(), consumers, texture, entry.getValue(), tickDelta, world);
			}
		});
	}
}
