/*
 * Copyright (c) 2019-2020 5zig Reborn
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

package eu.the5zig.mod.asm;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;

public class FabricHandle {
    public static void run() {
        Transformer.FABRIC = true;
        Field vf;
        String reflName = null;
        String version;
        try {
            vf = null; // ZIG116

            version = (String) vf.get(null);

            switch (version) {
                case "1.8.9":
                    reflName = "ReflectionNames189";
                    break;
                case "1.12.2":
                    reflName = "ReflectionNames1122";
                    break;
                case "1.13.2":
                    reflName = "ReflectionNames1132";
                    break;
            }
        } catch (Exception e) {
            version = "1.14.4";
            reflName = "ReflectionNames1144";
        }
        System.out.println("F Minecraft Version: " + version);

        try {
            Transformer.REFLECTION = (ReflectionNames) Class.forName("eu.the5zig.mod.asm." + reflName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Checking for Forge");
            Class.forName("net.minecraftforge.client.GuiIngameForge");
            System.out.println("Forge detected!");
            Transformer.FORGE = true;
        } catch (Exception ignored) {
            System.out.println("Forge not found!");
        }

        System.out.println("Initializing Mixins...");
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("mixins.the5zigmod.optional.json");
        Mixins.addConfiguration("mixins.json");

        System.out.println("Forge: " + Transformer.FORGE);

        if (Transformer.FORGE) {
            Mixins.addConfiguration("mixins_forge.json");
        }

        env.setObfuscationContext(Transformer.FORGE ? "searge" : "notch");
        env.setSide(MixinEnvironment.Side.CLIENT);

        System.out.println("Obfuscation context: " + env.getObfuscationContext());
    }
}
