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
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 */
public class PatchRenderItem implements IClassTransformer {

	@Override
	public byte[] transform(String s, String s1, byte[] bytes) {
		LogUtil.startClass("RenderItem (%s)", Names.renderItem.getName());

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
			if (Names.renderItemPerson.equals(name, desc)) {
				LogUtil.startMethod(Names.renderItemPerson.getName() + "(%s)", Names.renderItemPerson.getDesc());
				return new PatchRenderPerson(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			if (Names.renderItemInventory.equals(name, desc)) {
				LogUtil.startMethod(Names.renderItemInventory.getName() + "(%s)", Names.renderItemInventory.getDesc());
				return new PatchRenderInventory(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			LogUtil.endMethod();
			return super.visitMethod(access, name, desc, signature, exceptions);
		}

	}

	public class PatchRenderPerson extends MethodVisitor {

		public PatchRenderPerson(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			LogUtil.log("renderItemPerson");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onRenderItemPerson", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z", false);
			Label label = new Label();
			mv.visitJumpInsn(IFEQ, label);
			mv.visitInsn(RETURN);
			mv.visitLabel(label);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}

	}

	public class PatchRenderInventory extends MethodVisitor {

		public PatchRenderInventory(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			LogUtil.log("renderItemInventory");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onRenderItemInventory", "(Ljava/lang/Object;Ljava/lang/Object;II)Z", false);
			Label label = new Label();
			mv.visitJumpInsn(IFEQ, label);
			mv.visitInsn(RETURN);
			mv.visitLabel(label);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		}

	}

}
