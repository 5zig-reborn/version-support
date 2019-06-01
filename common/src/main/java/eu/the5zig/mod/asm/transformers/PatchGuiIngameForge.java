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

package eu.the5zig.mod.asm.transformers;

import eu.the5zig.mod.asm.Names;
import eu.the5zig.mod.asm.LogUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class PatchGuiIngameForge implements IClassTransformer {

	@Override
	public byte[] transform(String s, String s1, byte[] bytes) {
		LogUtil.startClass("GuiIngameForge (%s)", Names.guiIngameForge.getName());

		ClassReader reader = new ClassReader(bytes);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
		ClassPatcher visitor = new ClassPatcher(writer);
		reader.accept(visitor, 0);
		LogUtil.endClass();
		return writer.toByteArray();
	}

	public class ClassPatcher extends ClassVisitor {

		public ClassPatcher(ClassVisitor visitor) {
			super(ASM5, visitor);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (Names.renderGameOverlayForge.equals(name, desc)) {
				LogUtil.startMethod(Names.renderGameOverlayForge.getName() + " " + Names.renderGameOverlayForge.getDesc());
				return new PatchRenderGameOverlay(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			if (Names.ingameTick.equals(name, desc)) {
				LogUtil.startMethod(Names.ingameTick.getName() + " " + Names.ingameTick.getDesc());
				return new PatchTick(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			if (Names.renderChatForge.equals(name, desc)) {
				LogUtil.startMethod(Names.renderChatForge.getName() + " " + Names.renderChatForge.getDesc());
				return new PatchChat(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			LogUtil.endMethod();
			return super.visitMethod(access, name, desc, signature, exceptions);
		}

	}

	public class PatchRenderGameOverlay extends MethodVisitor {

		public PatchRenderGameOverlay(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			LogUtil.log("rendering mod");
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onRenderGameOverlay", "()V", false);
		}
	}

	public class PatchTick extends MethodVisitor {

		public PatchTick(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			LogUtil.log("ingame tick");
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onIngameTick", "()V", false);
		}
	}

	public class PatchChat extends MethodVisitor {

		public PatchChat(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			LogUtil.log("rendering chat");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, Names.guiIngame.getName(), "n", "I");
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onDrawChat", "(I)V", false);
		}

	}

}
