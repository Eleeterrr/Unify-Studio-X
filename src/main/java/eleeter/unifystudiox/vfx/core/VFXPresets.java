package eleeter.unifystudiox.vfx.core;

import eleeter.unifystudiox.vfx.parameter.VFXCurve;
import eleeter.unifystudiox.vfx.parameter.VFXGradient;
import eleeter.unifystudiox.vfx.parameter.VFXRandom;

public class VFXPresets
{

    private VFXPresets()
    {
    }

    /* Fire */
    public static VFXEffect fire()
    {
        VFXEmitter core = new VFXEmitter();
        core.name = "Fire Core";
        core.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        core.spawnRate = 90.0F;
        core.maxParticles = 350;
        core.lifetime = new VFXRandom(0.25F, 0.55F);
        core.initialSpeed = new VFXRandom(1.8F, 3.5F);
        core.spreadAngle = 14.0F;
        core.dirX = 0.0F;
        core.dirY = 1.0F;
        core.dirZ = 0.0F;
        core.initialSize = new VFXRandom(0.08F, 0.22F);
        core.sizeOverLifetime = VFXCurve.fireCoreCurve();
        core.gravity = -0.15F;
        core.drag = 0.04F;
        core.colorOverLifetime = VFXGradient.fireCore();
        core.turbulenceEnabled = true;
        core.turbulenceStrength = 1.4F;
        core.turbulenceFreq = 2.5F;

        VFXEmitter body = new VFXEmitter();
        body.name = "Fire Body";
        body.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        body.spawnRate = 70.0F;
        body.maxParticles = 500;
        body.lifetime = new VFXRandom(0.45F, 1.0F);
        body.initialSpeed = new VFXRandom(1.2F, 3.2F);
        body.spreadAngle = 28.0F;
        body.dirX = 0.0F;
        body.dirY = 1.0F;
        body.dirZ = 0.0F;
        body.initialSize = new VFXRandom(0.18F, 0.45F);
        body.sizeOverLifetime = VFXCurve.fireBodyCurve();
        body.gravity = -0.08F;
        body.drag = 0.055F;
        body.colorOverLifetime = VFXGradient.fire();
        body.turbulenceEnabled = true;
        body.turbulenceStrength = 0.7F;
        body.turbulenceFreq = 1.5F;

        VFXEmitter outer = new VFXEmitter();
        outer.name = "Fire Outer";
        outer.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        outer.spawnRate = 50.0F;
        outer.maxParticles = 300;
        outer.lifetime = new VFXRandom(0.6F, 1.3F);
        outer.initialSpeed = new VFXRandom(0.9F, 2.4F);
        outer.spreadAngle = 38.0F;
        outer.dirX = 0.0F;
        outer.dirY = 1.0F;
        outer.dirZ = 0.0F;
        outer.initialSize = new VFXRandom(0.22F, 0.55F);
        outer.sizeOverLifetime = VFXCurve.fireBodyCurve();
        outer.gravity = -0.05F;
        outer.drag = 0.07F;
        outer.colorOverLifetime = VFXGradient.fireOuter();
        outer.turbulenceEnabled = true;
        outer.turbulenceStrength = 0.45F;
        outer.turbulenceFreq = 1.1F;

        VFXEmitter smokeCap = new VFXEmitter();
        smokeCap.name = "Fire Smoke";
        smokeCap.blendMode = VFXEmitter.BlendMode.NORMAL;
        smokeCap.spawnRate = 18.0F;
        smokeCap.maxParticles = 120;
        smokeCap.lifetime = new VFXRandom(1.8F, 3.2F);
        smokeCap.initialSpeed = new VFXRandom(0.35F, 1.0F);
        smokeCap.spreadAngle = 18.0F;
        smokeCap.dirX = 0.0F;
        smokeCap.dirY = 1.0F;
        smokeCap.dirZ = 0.0F;
        smokeCap.offsetY = new VFXRandom(1.2F, 2.0F);
        smokeCap.initialSize = new VFXRandom(0.35F, 0.7F);
        smokeCap.sizeOverLifetime = VFXCurve.fadeIn();
        smokeCap.gravity = -0.06F;
        smokeCap.drag = 0.09F;
        smokeCap.colorOverLifetime = VFXGradient.fireSmoke();
        smokeCap.turbulenceEnabled = true;
        smokeCap.turbulenceStrength = 0.25F;
        smokeCap.turbulenceFreq = 0.7F;

        return new VFXEffect(core, body, outer, smokeCap);
    }

    /* Lightning */
    public static VFXEffect lightning()
    {
        VFXEmitter e = new VFXEmitter();
        e.name = "Lightning";
        e.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        e.spawnRate = 200.0F;
        e.maxParticles = 500;
        e.lifetime = new VFXRandom(0.03F, 0.12F);
        e.initialSpeed = new VFXRandom(0.5F, 3.0F);
        e.spreadAngle = 360.0F;
        e.initialSize = new VFXRandom(0.05F, 0.15F);
        e.sizeOverLifetime = VFXCurve.fadeOut();
        e.gravity = 0.0F;
        e.drag = 0.1F;
        e.colorOverLifetime = VFXGradient.lightning();
        e.turbulenceEnabled = true;
        e.turbulenceStrength = 3.0F;
        e.turbulenceFreq = 3.0F;
        return new VFXEffect(e);
    }

    /* Smoke */

    public static VFXEffect smoke()
    {
        return new VFXEffect(smokeEmitter());
    }

    public static VFXEmitter smokeEmitter()
    {
        VFXEmitter e = new VFXEmitter();
        e.name = "Smoke";
        e.blendMode = VFXEmitter.BlendMode.NORMAL;
        e.spawnRate = 20.0F;
        e.maxParticles = 200;
        e.lifetime = new VFXRandom(2.0F, 4.0F);
        e.initialSpeed = new VFXRandom(0.3F, 1.0F);
        e.spreadAngle = 15.0F;
        e.dirX = 0.0F;
        e.dirY = 1.0F;
        e.dirZ = 0.0F;
        e.initialSize = new VFXRandom(0.3F, 0.6F);
        e.sizeOverLifetime = VFXCurve.fadeIn();
        e.gravity = -0.1F;
        e.drag = 0.08F;
        e.colorOverLifetime = VFXGradient.smoke();
        e.turbulenceEnabled = true;
        e.turbulenceStrength = 0.3F;
        return e;
    }

    /* Explosion */
    public static VFXEffect explosion()
    {
        VFXEmitter core = new VFXEmitter();
        core.name = "Explosion Core";
        core.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        core.burstCount = 60;
        core.spawnRate = 0.0F;
        core.maxParticles = 100;
        core.lifetime = new VFXRandom(0.3F, 0.7F);
        core.initialSpeed = new VFXRandom(5.0F, 12.0F);
        core.spreadAngle = 360.0F;
        core.initialSize = new VFXRandom(0.2F, 0.5F);
        core.sizeOverLifetime = VFXCurve.fadeOut();
        core.gravity = 2.0F;
        core.drag = 0.06F;
        core.colorOverLifetime = VFXGradient.explosion();

        VFXEmitter smokeTrail = smokeEmitter();
        smokeTrail.name = "Explosion Smoke";
        smokeTrail.startDelay = 0.1F;
        smokeTrail.duration = 1.5F;

        return new VFXEffect(core, smokeTrail);
    }


    /* Magic Aura 💀 */

    public static VFXEffect magicAura()
    {
        VFXEmitter e = new VFXEmitter();
        e.name = "Magic Aura";
        e.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        e.shape = VFXEmitter.EmitterShape.SPHERE;
        e.shapeRadius = 0.8F;
        e.spawnRate = 40.0F;
        e.maxParticles = 300;
        e.lifetime = new VFXRandom(0.8F, 1.5F);
        e.initialSpeed = new VFXRandom(0.2F, 1.0F);
        e.spreadAngle = 360.0F;
        e.initialSize = new VFXRandom(0.05F, 0.2F);
        e.sizeOverLifetime = VFXCurve.fadeInOut();
        e.gravity = -0.5F;
        e.drag = 0.1F;
        e.colorOverLifetime = VFXGradient.magic();
        e.turbulenceEnabled = true;
        e.turbulenceStrength = 1.0F;
        return new VFXEffect(e);
    }


    /* Sparks */
    public static VFXEffect sparks()
    {
        return new VFXEffect(sparksEmitter());
    }

    public static VFXEmitter sparksEmitter()
    {
        VFXEmitter e = new VFXEmitter();
        e.name = "Sparks";
        e.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        e.renderType = VFXEmitter.RenderType.RIBBON;
        e.ribbonHistory = 12;
        e.ribbonWidth = 0.03F;
        e.ribbonWidthCurve = VFXCurve.fadeOut();
        e.burstCount = 20;
        e.spawnRate = 0.0F;
        e.maxParticles = 30;
        e.lifetime = new VFXRandom(0.5F, 1.2F);
        e.initialSpeed = new VFXRandom(4.0F, 10.0F);
        e.spreadAngle = 360.0F;
        e.initialSize = new VFXRandom(0.02F, 0.06F);
        e.sizeOverLifetime = VFXCurve.fadeOut();
        e.gravity = 8.0F;
        e.drag = 0.02F;
        e.colorOverLifetime = VFXGradient.lightning();
        return e;
    }

    /* Lightning Strike */

    public static VFXEffect lightningStrike()
    {
        VFXEmitter bolt = new VFXEmitter();
        bolt.name = "Strike Bolt";
        bolt.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        bolt.renderType = VFXEmitter.RenderType.RIBBON;
        bolt.shape = VFXEmitter.EmitterShape.LIGHTNING_BOLT;
        bolt.shapeHeight = 30.0F;
        bolt.shapeWidth = 2.0F;
        bolt.ribbonHistory = 30;
        bolt.ribbonWidth = 0.6F;
        bolt.ribbonWidthCurve = VFXCurve.constant(1.0F);
        bolt.burstCount = 1;
        bolt.spawnRate = 0.0F;
        bolt.maxParticles = 5;
        bolt.lifetime = new VFXRandom(0.2F, 0.4F);
        bolt.initialSpeed = new VFXRandom(0.0F, 0.0F);
        bolt.spreadAngle = 0.0F;
        bolt.dirX = 0.0F;
        bolt.dirY = 1.0F;
        bolt.dirZ = 0.0F;
        bolt.offsetY = new VFXRandom(30.0F, 30.0F);
        bolt.collideFloor = true;
        bolt.floorY = 0.0F;
        bolt.bounciness = 0.0F;
        bolt.initialSize = new VFXRandom(0.1F, 0.2F);
        bolt.sizeOverLifetime = VFXCurve.fadeOut();
        bolt.gravity = 0.0F;
        bolt.drag = 0.0F;
        bolt.colorOverLifetime = VFXGradient.lightning();
        bolt.turbulenceEnabled = true;
        bolt.turbulenceStrength = 80.0F;
        bolt.turbulenceFreq = 0.3F;

        VFXEmitter flash = new VFXEmitter();
        flash.name = "Strike Flash";
        flash.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        flash.burstCount = 30;
        flash.spawnRate = 0.0F;
        flash.maxParticles = 40;
        flash.lifetime = new VFXRandom(0.1F, 0.3F);
        flash.initialSpeed = new VFXRandom(5.0F, 20.0F);
        flash.spreadAngle = 360.0F;
        flash.initialSize = new VFXRandom(0.5F, 1.5F);
        flash.sizeOverLifetime = VFXCurve.fadeOut();
        flash.gravity = 0.0F;
        flash.drag = 0.2F;
        flash.colorOverLifetime = VFXGradient.lightning();
        flash.startDelay = 0.0F;

        VFXEmitter sparkTrail = sparksEmitter();
        sparkTrail.name = "Strike Sparks";
        sparkTrail.burstCount = 40;
        sparkTrail.initialSpeed = new VFXRandom(10.0F, 25.0F);
        sparkTrail.startDelay = 0.0F;

        return new VFXEffect(bolt, flash, sparkTrail);
    }

    public static VFXEffect ringShockwave()
    {
        VFXEmitter e = new VFXEmitter();
        e.name = "Ring Shockwave";
        e.blendMode = VFXEmitter.BlendMode.ADDITIVE;
        e.renderType = VFXEmitter.RenderType.RING;
        e.burstCount = 1;
        e.spawnRate = 0.0F;
        e.maxParticles = 5;
        e.lifetime = new VFXRandom(0.5F, 0.8F);
        e.initialSize = new VFXRandom(0.5F, 0.5F);
        e.sizeOverLifetime = VFXCurve.fadeIn();
        e.initialSpeed = new VFXRandom(0.0F, 0.0F);
        e.gravity = 0.0F;
        e.drag = 0.0F;
        e.ringRadius = 2.0F;
        e.ringThickness = 0.15F;
        e.ringSegments = 48;
        e.colorOverLifetime = VFXGradient.explosion();
        return new VFXEffect(e);
    }
}
