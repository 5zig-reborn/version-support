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

package eu.the5zig.mod.asm;

import net.minecraft.client.main.Main;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;

public class ClassTweaker implements ITweaker {

    private List<String> args;
    private File gameDir;
    private File assetsDir;
    private String version;

    public ClassTweaker() {
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String version) {
        this.args = args;
        this.gameDir = gameDir;
        this.assetsDir = assetsDir;

        String reflName = null;

        try {
            Enumeration<URL> resources = ClassTweaker.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try {
                    URL url = resources.nextElement();
                    Manifest manifest = new Manifest(url.openStream());
                    if (manifest.getMainAttributes().getValue("5zig-Version") != null) {
                        this.version = manifest.getMainAttributes().getValue("Minecraft-Version");
                        switch (this.version) {
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
                        break;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        LogWrapper.info("Minecraft Version: " + this.version);

        try {
            Transformer.REFLECTION = (ReflectionNames) Class.forName("eu.the5zig.mod.asm." + reflName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            LogWrapper.finest("Checking for Forge");
            Class.forName("net.minecraftforge.client.GuiIngameForge");
            LogWrapper.info("Forge detected!");
            Transformer.FORGE = true;
        } catch (Exception ignored) {
            LogWrapper.info("Forge not found!");
        }

        LogWrapper.info("Initializing Mixins...");
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("mixins.json");

        LogWrapper.info("Forge: %b", Transformer.FORGE);

        if (Transformer.FORGE) {
            Mixins.addConfiguration("mixins_forge.json");
        }

        env.setObfuscationContext(Transformer.FORGE ? "searge" : "notch");
        env.setSide(MixinEnvironment.Side.CLIENT);

        LogWrapper.info("Obfuscation context: " + env.getObfuscationContext());
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        //classLoader.registerTransformer(Transformer.class.getName());
    }

    @Override
    public String getLaunchTarget() {
        return Main.class.getName();
    }

    @Override
    public String[] getLaunchArguments() {
        ArrayList<String> argumentList = (ArrayList<String>) Launch.blackboard.get("ArgumentList");
        if (argumentList.isEmpty()) {
            if (gameDir != null) {
                argumentList.add("--gameDir");
                argumentList.add(gameDir.getPath());
            }
            if (assetsDir != null) {
                argumentList.add("--assetsDir");
                argumentList.add(assetsDir.getPath());
            }
            argumentList.add("--version");
            argumentList.add(version);
            argumentList.addAll(args);
        }
        return new String[0];
    }

}
