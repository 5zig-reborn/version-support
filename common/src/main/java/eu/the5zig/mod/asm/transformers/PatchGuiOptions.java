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

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class PatchGuiOptions implements IClassTransformer {

	@Override
	public byte[] transform(String s, String s1, byte[] bytes) {
		LogUtil.startClass("GuiOptions (%s)", Names.guiOptions.getName());

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
			if (Names.initGui.equals(name, desc)) {
				LogUtil.startMethod("InitGui " + Names.initGui.getName() + "(%s)", Names.initGui.getDesc());
				return new PatchInitGui(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			if (Names.actionPerformed.equals(name, desc)) {
				LogUtil.startMethod("actionPerformed " + Names.actionPerformed.getName() + "(%s)", Names.actionPerformed.getDesc());
				return new PatchActionPerformed(cv.visitMethod(access, name, desc, signature, exceptions));
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
		public void visitCode() {
			LogUtil.log("Adding button");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, Names.guiOptions.getName(), "n", "Ljava/util/List;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "get5zigOptionButton", "(Ljava/lang/Object;)Leu/the5zig/mod/gui/elements/IButton;", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
			mv.visitInsn(POP);
			super.visitCode();
		}
	}

	public class PatchActionPerformed extends MethodVisitor {

		public PatchActionPerformed(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitCode() {
			LogUtil.log("action performed proxy");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onOptionsActionPerformed", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
			super.visitCode();
		}
	}

}
