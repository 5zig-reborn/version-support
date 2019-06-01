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

import eu.the5zig.mod.asm.LogUtil;
import eu.the5zig.mod.asm.Names;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class PatchGuiDisconnected implements IClassTransformer {

	@Override
	public byte[] transform(String s, String s1, byte[] bytes) {
		LogUtil.startClass("GuiDisconnected (%s)", Names.guiDisconnected.getName());

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
			if (Names.guiDisconnectedInit.equals(name, desc)) {
				LogUtil.startMethod(Names.guiDisconnectedInit.getName() + " " + Names.guiDisconnectedInit.getDesc());
				return new PatchInitGui(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			if (Names.drawScreen.equals(name, desc)) {
				LogUtil.startMethod(Names.drawScreen.getName() + " " + Names.drawScreen.getDesc());
				return new PatchDrawGui(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			LogUtil.endMethod();
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}

	public class PatchInitGui extends MethodVisitor {

		public PatchInitGui(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode == RETURN) {
				LogUtil.log("draw");
				LogUtil.log("init");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, "axh", "h", "L" + Names.guiScreen.getName() + ";");
				mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onGuiDisconnectedInit", "(Ljava/lang/Object;)V", false);
			}
			super.visitInsn(opcode);
		}

	}

	public class PatchDrawGui extends MethodVisitor {

		public PatchDrawGui(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode == RETURN) {
				LogUtil.log("draw");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onGuiDisconnectedDraw", "(Ljava/lang/Object;)V", false);
			}
			super.visitInsn(opcode);
		}

	}
}
