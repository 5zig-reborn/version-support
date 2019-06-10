/*
 * Copyright (c) 2019 5zig Reborn
 *
 * This file is part of The 5zig Mod
 * The 5zig Mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The 5zig Mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The 5zig Mod.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.the5zig.mod.mixin;

import eu.the5zig.mod.util.CombatRangeUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GameRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(method = "getMouseOver", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "FIELD", target =
            "net/minecraft/client/Minecraft.pointedEntity:Lnet/minecraft/entity/Entity;",
            ordinal = 1,
            opcode = Opcodes.PUTFIELD))
    public void getMouseOver(float pTicks, CallbackInfo ci, Entity p1, double p2, Vec3d p3, boolean p4,
                             int p5, double p6, Vec3d p7, Vec3d p8, Vec3d p9, float p10, List p11, double p12) {
        CombatRangeUtil.lastPoint = p12;
        CombatRangeUtil.maxRange = p2;
    }

}
