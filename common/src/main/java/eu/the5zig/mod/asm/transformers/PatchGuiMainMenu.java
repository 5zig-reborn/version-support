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
import eu.the5zig.mod.asm.Transformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class PatchGuiMainMenu implements IClassTransformer {

	public byte[] transform(String s, String arg, byte[] bytes) {
		LogUtil.startClass("GuiMainMenu (%s)", "ayb");

		ClassReader reader = new ClassReader(bytes);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
		ClassPatcher visitor = new ClassPatcher(writer);
		reader.accept(visitor, 0);
		LogUtil.endClass();
		return writer.toByteArray();
	}

	public class ClassPatcher extends ClassVisitor {

		public ClassPatcher(ClassVisitor visitor) {
			super(Opcodes.ASM5, visitor);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (Names.insertSingleMultiplayerButton.equals(name, desc)) {
				LogUtil.startMethod(Names.insertSingleMultiplayerButton.getName() + "(%s)", Names.insertSingleMultiplayerButton.getDesc());
				return new PatchInsertSingleMultiplayer(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			if (Names.actionPerformed.equals(name, desc)) {
				LogUtil.startMethod(Names.actionPerformed.getName() + "(%s)", Names.actionPerformed.getDesc());
				return new PatchActionPerformed(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			if (Names._static.equals(name, desc) && access == ACC_STATIC) {
				LogUtil.startMethod(Names._static.getName() + "(%s)", Names._static.getDesc());
				return new PatchStatic(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			if (Names.drawScreen.equals(name, desc)) {
				LogUtil.startMethod(Names.drawScreen.getName() + "(%s)", Names.drawScreen.getDesc());
				return new PatchDrawScreen(cv.visitMethod(access, name, desc, signature, exceptions));
			}
			LogUtil.endMethod();
			return super.visitMethod(access, name, desc, signature, exceptions);
		}

	}

	public class PatchInsertSingleMultiplayer extends MethodVisitor {

		public PatchInsertSingleMultiplayer(MethodVisitor visitor) {
			super(ASM5, visitor);
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode == RETURN) {
				LogUtil.log("Adding 'Last Server' Button... ");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ILOAD, 1);
				mv.visitVarInsn(ILOAD, 2);
				mv.visitInsn(Transformer.FORGE ? ICONST_1 : ICONST_0);
				mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onInsertSingleMultiplayerButton", "(Ljava/lang/Object;IIZ)V", false);
			}
			super.visitInsn(opcode);
		}

	}

	public class PatchActionPerformed extends MethodVisitor {

		public PatchActionPerformed(MethodVisitor visitor) {
			super(ASM5, visitor);
		}

		@Override
		public void visitCode() {
			LogUtil.log("Adding Proxy access");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onMainActionPerformed", "(Ljava/lang/Object;)V", false);
			super.visitCode();
		}

	}

	public class PatchStatic extends MethodVisitor {

		public PatchStatic(MethodVisitor visitor) {
			super(ASM5, visitor);
		}

		@Override
		public void visitCode() {
			LogUtil.log("Adding The 5zig Mod");
			mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onMainStatic", "()V", false);
			super.visitCode();
		}
	}

	public class PatchDrawScreen extends MethodVisitor {

		public PatchDrawScreen(MethodVisitor visitor) {
			super(ASM5, visitor);
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode == RETURN) {
				LogUtil.log("Adding version");
				mv.visitMethodInsn(INVOKESTATIC, "BytecodeHook", "onMainDraw", "()V", false);
			}
			super.visitInsn(opcode);
		}

	}

}
