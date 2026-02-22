package com.woodiertexas.planetarium;

import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class Planetarium {
	public static final Logger LOGGER = LoggerFactory.getLogger("Planetarium");
	public static final String MOD_ID = "planetarium";
	private static final int FULL_BRIGHT = 15728880;

	/**
	 * Computes the sky angle from world time, replacing the removed {@code ClientLevel.getSkyAngle}.
	 */
	public static float getSkyAngle(ClientLevel level) {
		long dayTime = level.getDayTime();
		double frac = Mth.frac(dayTime / 24000.0 - 0.25);
		double mul = 0.5 - Math.cos(frac * Math.PI) / 2.0;
		return (float)(frac * 2.0 + mul) / 3.0F;
	}

	/**
	 * @param matrices     The pose stack for rendering.
	 * @param bufferSource The buffer source for vertex data.
	 * @param texture      The resolved texture location for the planet.
	 * @param planetInfo   The planet data.
	 * @param tickDelta    Time between ticks.
	 * @param world        The client world to render in.
	 */
	public static void renderPlanet(PoseStack matrices, MultiBufferSource bufferSource, ResourceLocation texture, PlanetInfo planetInfo, float tickDelta, ClientLevel world) {
		matrices.pushPose();
		
		// First, line planet up where the sun is in the sky
		matrices.mulPose(Axis.YP.rotationDegrees(90.0F));
		
		// Second, change the orbital tilt of the planet
		matrices.mulPose(Axis.YP.rotationDegrees(planetInfo.tilt()));
		
		// Third, set the angle of the planet in the sky and offset it.
		matrices.mulPose(Axis.XP.rotationDegrees(-getSkyAngle(world) * 360.0F + planetInfo.procession()));
		
		// Fourth, set the inclination of the planet.
		matrices.mulPose(Axis.ZP.rotationDegrees(planetInfo.inclination()));
		
		// Finally, change the rotation of the planet texture.
		matrices.mulPose(Axis.YP.rotationDegrees(planetInfo.texture_rotation()));
		
		if (world.getDayTime() % 24000L >= 11800) {
			float clearFactor = 1.0f - world.getRainLevel(tickDelta);
			float transparency = 2 * world.getStarBrightness(tickDelta) * clearFactor;
			
			if (transparency > 0.0f) {
				Matrix4f matrix4f = matrices.last().pose();
				int color = ARGB.colorFromFloat(transparency, transparency, transparency, transparency);
				VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(texture));
				float size = planetInfo.size();
				
				buffer.addVertex(matrix4f, -size, 99.0F, -size).setColor(color).setUv(0.0F, 0.0F).setLight(FULL_BRIGHT);
				buffer.addVertex(matrix4f, size, 99.0F, -size).setColor(color).setUv(1.0F, 0.0F).setLight(FULL_BRIGHT);
				buffer.addVertex(matrix4f, size, 99.0F, size).setColor(color).setUv(1.0F, 1.0F).setLight(FULL_BRIGHT);
				buffer.addVertex(matrix4f, -size, 99.0F, size).setColor(color).setUv(0.0F, 1.0F).setLight(FULL_BRIGHT);
			}
		}
		
		matrices.popPose();
	}
}
