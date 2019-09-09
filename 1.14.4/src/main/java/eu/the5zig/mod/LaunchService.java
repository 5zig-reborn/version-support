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

package eu.the5zig.mod;

import cpw.mods.modlauncher.TransformingClassLoader;
import eu.the5zig.mod.asm.ReflectionNames;
import eu.the5zig.mod.asm.Transformer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;

@Mod("examplemod")
public class LaunchService {

    public LaunchService() {
        System.out.println("Constructed");
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preinit);
        try {

            Enumeration<URL> urls = TransformingClassLoader.getSystemClassLoader().getResources("META-INF/mods.toml");
            while(urls.hasMoreElements()) {
                System.out.println(urls.nextElement().toString());
            }

            System.out.println(LaunchService.class.getClassLoader().getClass().getName());
            System.out.println(The5zigMod.class.getClassLoader().getClass().getName());
            System.out.println(ClassLoader.getSystemClassLoader().getClass().getName());
            System.out.println(Thread.currentThread().getContextClassLoader().getClass().getName());

            // Load Mixin services
            System.out.println(LaunchService.class.getClassLoader().getResource("META-INF/services").toString());
            System.out.println(The5zigMod.class.getClassLoader().getResources("META-INF/services").nextElement().toString());
            System.out.println(ClassLoader.getSystemClassLoader().getResources("META-INF/services").nextElement().toString());
            System.out.println(Thread.currentThread().getContextClassLoader().getResources("META-INF/services").nextElement().toString());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void preinit(FMLCommonSetupEvent evt)
    {
        System.out.println("Preinit!");
        try {
            // Load Mixin services
            System.out.println(LaunchService.class.getClassLoader().getResource("META-INF/services").toString());
            System.out.println(LaunchService.class.getClassLoader().getResources("META-INF/services").nextElement().toString());
            System.out.println(The5zigMod.class.getClassLoader().getResources("META-INF/services").nextElement().toString());
            System.out.println(ClassLoader.getSystemClassLoader().getResources("META-INF/services").nextElement().toString());
            System.out.println(Thread.currentThread().getContextClassLoader().getResources("META-INF/services").nextElement().toString());
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static void load5zig() {

        String reflName = null;

        try {
            Transformer.REFLECTION = (ReflectionNames) Class.forName("eu.the5zig.mod.asm." + reflName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            IMixinService service = new MixinServiceLaunchWrapper();
            Field serviceField = MixinService.class.getDeclaredField("service");
            serviceField.setAccessible(true);

            Field instanceField = MixinService.class.getDeclaredField("instance");
            instanceField.setAccessible(true);

            MixinService instance = (MixinService) instanceField.get(null);
            serviceField.set(instance, service);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Initializing Mixins...");
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("mixins.json");

        Transformer.FORGE = true;

        env.setObfuscationContext("searge");
        env.setSide(MixinEnvironment.Side.CLIENT);

        System.out.println("Obfuscation context: " + env.getObfuscationContext());

    }
}
