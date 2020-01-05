package eu.the5zig.mod.asm;

import net.minecraft.realms.RealmsSharedConstants;
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
            vf = RealmsSharedConstants.class.getField("VERSION_STRING");

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
