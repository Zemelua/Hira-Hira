package io.github.zemelua.hira_hira.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.CherryLeavesParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(CherryLeavesParticle.class)
public abstract class MixinCherryLeavesParticle extends SpriteBillboardParticle {
	@Shadow private float field_43369;
	@Shadow @Final private float field_43371;
	@Shadow @Final private float field_43370;

	/**
	 * @author Zemelua
	 * @reason Because there are many changes, the entire method is rewritten. Also rewrite inappropriate specifications
	 * such as {@code maxAge} being used like age.
	 */
	@Overwrite
	public void tick() {
		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;

		if (this.age++ >= this.maxAge) {
			this.markDead();

			return;
		}

		float g = Math.min(this.age / 300.0f, 1.0f);
		double d = Math.cos(Math.toRadians(this.field_43370 * 60.0f)) * 2.0 * Math.pow(g, 1.25);
		double e = Math.sin(Math.toRadians(this.field_43370 * 60.0f)) * 2.0 * Math.pow(g, 1.25);
		this.velocityX += d * (double)0.0025f;
		this.velocityZ += e * (double)0.0025f;
		this.velocityY -= this.gravityStrength;
		this.field_43369 += this.field_43371 / 20.0f;
		this.prevAngle = this.angle;
		if (!this.onGround) {
			this.angle += this.field_43369 / 20.0f;
		}
		this.move(this.velocityX, this.velocityY, this.velocityZ);
		if (this.dead) {
			return;
		}
		this.velocityX *= this.velocityMultiplier;
		this.velocityY *= this.velocityMultiplier;
		this.velocityZ *= this.velocityMultiplier;

		float a = Math.max(0.0F, this.maxAge - this.age - 1);
		this.alpha = Math.min(1.0F, a / 10.0F);
	}

	@Inject(method = "getType",
			at = @At(value = "RETURN"),
			cancellable = true)
	private void returnTranslucentType(CallbackInfoReturnable<ParticleTextureSheet> callback) {
		callback.setReturnValue(ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT);
	}

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		Quaternionf quaternionf;
		Vec3d vec3d = camera.getPos();
		float f = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
		float g = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
		float h = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());
		if (this.angle == 0.0f) {
			quaternionf = camera.getRotation();
		} else {
			quaternionf = new Quaternionf(camera.getRotation());
			quaternionf.rotateZ(MathHelper.lerp(tickDelta, this.prevAngle, this.angle));
		}

		Quaternionf quaternionf1 = new Quaternionf();
		if (!this.onGround) {
			quaternionf1.rotateY((float) Math.toRadians((this.age * 10) % 180 - 90));
			quaternionf1.rotateZ((float) Math.toRadians(55));
		}


		Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
		float i = this.getSize(tickDelta);
		for (int j = 0; j < 4; ++j) {
			Vector3f vector3f = vector3fs[j];
			vector3f.rotate(quaternionf1);
			vector3f.rotate(quaternionf);
			vector3f.mul(i);
			vector3f.add(f, g, h);
		}
		float k = this.getMinU();
		float l = this.getMaxU();
		float m = this.getMinV();
		float n = this.getMaxV();
		int o = this.getBrightness(tickDelta);
		vertexConsumer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).texture(l, n).color(this.red, this.green, this.blue, this.alpha).light(o).next();
		vertexConsumer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).texture(l, m).color(this.red, this.green, this.blue, this.alpha).light(o).next();
		vertexConsumer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).texture(k, m).color(this.red, this.green, this.blue, this.alpha).light(o).next();
		vertexConsumer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).texture(k, n).color(this.red, this.green, this.blue, this.alpha).light(o).next();
	}

	protected MixinCherryLeavesParticle(ClientWorld clientWorld, double x, double y, double z) {
		super(clientWorld, x, y, z);
	}
}
