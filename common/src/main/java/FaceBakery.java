/*
 * Original: Copyright (c) 2015-2019 5zig [MIT]
 * Current: Copyright (c) 2019 5zig Reborn [GPLv3+]
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

import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class FaceBakery {
	private static final float fieldA = 1.0F / (float) Math.cos(0.39269909262657166D) - 1.0F;
	private static final float fieldB = 1.0F / (float) Math.cos(0.7853981852531433D) - 1.0F;

	public static final class Constants {
		public static final int SOUTH = EnumFacing.SOUTH.getIndex();
		public static final int UP = EnumFacing.UP.getIndex();
		public static final int EAST = EnumFacing.EAST.getIndex();
		public static final int NORTH = EnumFacing.NORTH.getIndex();
		public static final int DOWN = EnumFacing.DOWN.getIndex();
		public static final int WEST = EnumFacing.WEST.getIndex();
	}

	public FaceBakery() {
	}

	public BakedQuad a(Vector3f vector1, Vector3f vector2, BlockPartFace blockPartFace, EnumFacing enumFacing, ModelRotation modelRotation,
					   BlockPartRotation blockPartRotation, boolean b1, boolean b2) {
		int[] data = this.a(blockPartFace, enumFacing, this.a(vector1, vector2), modelRotation, blockPartRotation, b1, b2);
		EnumFacing facing = a(data);
		if (b1) {
			this.a(data, facing, blockPartFace.blockFaceUV);
		}

		if (blockPartRotation == null) {
			this.a(data, facing);
		}

		fillNormal(data);

		return new BakedQuad(data, blockPartFace.tintIndex, facing);
	}

	private void fillNormal(int[] faceData) {
		Vector3f v1 = new Vector3f(faceData[3 * 7 + 0], faceData[3 * 7 + 1], faceData[3 * 7 + 2]);
		Vector3f t = new Vector3f(faceData[1 * 7 + 0], faceData[1 * 7 + 1], faceData[1 * 7 + 2]);
		Vector3f v2 = new Vector3f(faceData[2 * 7 + 0], faceData[2 * 7 + 1], faceData[2 * 7 + 2]);
		Vector3f result1 = new Vector3f();
		Vector3f result2 = new Vector3f();
		Vector3f result3 = new Vector3f();
		Vector3f.sub(v1, t, result1);
		t.set(faceData[0 * 7 + 0], faceData[0 * 7 + 1], faceData[0 * 7 + 2]);
		Vector3f.sub(v2, t, result2);
		Vector3f.cross(result2, result1, result3);
		result3.normalise();

		int x = ((byte) (result3.x * 127)) & 0xFF;
		int y = ((byte) (result3.y * 127)) & 0xFF;
		int z = ((byte) (result3.z * 127)) & 0xFF;
		for (int i = 0; i < 4; i++) {
			faceData[i * 7 + 6] = x | (y << 0x08) | (z << 0x10);
		}
	}

	private int[] a(BlockPartFace blockPartFace, EnumFacing enumFacing, float[] vec, ModelRotation modelRotation,
					BlockPartRotation blockPartRotation, boolean b1, boolean b2) {
		int[] data = new int[28];

		for (int i = 0; i < 4; ++i) {
			this.a(data, i, enumFacing, blockPartFace, vec, modelRotation, blockPartRotation, b1, b2);
		}

		return data;
	}

	private int a(EnumFacing var) {
		float var1 = this.b(var);
		int var2 = MathHelper.clamp_int((int) (var1 * 255.0F), 0, 255);
		return -16777216 | var2 << 16 | var2 << 8 | var2;
	}

	private float b(EnumFacing enumFacing) {
		switch (enumFacing) {
			case DOWN:
				return 0.5F;
			case UP:
				return 1.0F;
			case NORTH:
			case SOUTH:
				return 0.8F;
			case WEST:
			case EAST:
				return 0.6F;
			default:
				return 1.0F;
		}
	}

	private float[] a(Vector3f var, Vector3f var1) {
		float[] var2 = new float[EnumFacing.values().length];
		var2[Constants.WEST] = var.x / 16.0F;
		var2[Constants.DOWN] = var.y / 16.0F;
		var2[Constants.NORTH] = var.z / 16.0F;
		var2[Constants.EAST] = var1.x / 16.0F;
		var2[Constants.UP] = var1.y / 16.0F;
		var2[Constants.SOUTH] = var1.z / 16.0F;
		return var2;
	}

	private void a(int[] dataIn, int count, EnumFacing enumFacing, BlockPartFace blockPartFace, float[] vec,
				   ModelRotation modelRotation, BlockPartRotation blockPartRotation, boolean b1, boolean b2) {
		EnumFacing var10 = modelRotation.rotateFace(enumFacing);
		int var11 = b2 ? this.a(var10) : -1;
		EnumFaceDirection.VertexInformation var12 = EnumFaceDirection.getFacing(enumFacing).getVertexInformation(count);
		Vector3f var13 = new Vector3f(vec[var12.xIndex], vec[var12.yIndex], vec[var12.zIndex]);
		this.a(var13, blockPartRotation);
		int var14 = this.a(var13, enumFacing, count, modelRotation, b1);
		this.a(dataIn, var14, count, var13, var11, blockPartFace.blockFaceUV);
	}

	private void a(int[] dataIn, int var1, int count, Vector3f var3, int var4, BlockFaceUV blockPartFace) {
		int var7 = var1 * 7;
		dataIn[var7] = Float.floatToRawIntBits(var3.x);
		dataIn[var7 + 1] = Float.floatToRawIntBits(var3.y);
		dataIn[var7 + 2] = Float.floatToRawIntBits(var3.z);
		dataIn[var7 + 3] = var4;
		dataIn[var7 + 4] = Float.floatToRawIntBits(blockPartFace.func_178348_a(count) / 16f);
		dataIn[var7 + 4 + 1] = Float.floatToRawIntBits(blockPartFace.func_178346_b(count) / 16f);
	}

	private void a(Vector3f vector, BlockPartRotation var1) {
		if (var1 != null) {
			Matrix4f var2 = this.a();
			Vector3f var3 = new Vector3f(0.0F, 0.0F, 0.0F);
			switch (var1.axis) {
				case X:
					Matrix4f.rotate(var1.angle * 0.017453292F, new Vector3f(1.0F, 0.0F, 0.0F), var2, var2);
					var3.set(0.0F, 1.0F, 1.0F);
					break;
				case Y:
					Matrix4f.rotate(var1.angle * 0.017453292F, new Vector3f(0.0F, 1.0F, 0.0F), var2, var2);
					var3.set(1.0F, 0.0F, 1.0F);
					break;
				case Z:
					Matrix4f.rotate(var1.angle * 0.017453292F, new Vector3f(0.0F, 0.0F, 1.0F), var2, var2);
					var3.set(1.0F, 1.0F, 0.0F);
			}

			if (var1.rescale) {
				if (Math.abs(var1.angle) == 22.5F) {
					var3.scale(fieldA);
				} else {
					var3.scale(fieldB);
				}

				Vector3f.add(var3, new Vector3f(1.0F, 1.0F, 1.0F), var3);
			} else {
				var3.set(1.0F, 1.0F, 1.0F);
			}

			this.a(vector, new Vector3f(var1.origin), var2, var3);
		}
	}

	public int a(Vector3f var, EnumFacing var1, int var2, ModelRotation var3, boolean var4) {
		if (var3 == ModelRotation.X0_Y0) {
			return var2;
		} else {
			this.a(var, new Vector3f(0.5F, 0.5F, 0.5F), var3.getMatrix4d(), new Vector3f(1.0F, 1.0F, 1.0F));
			return var3.rotateVertex(var1, var2);
		}
	}

	private void a(Vector3f var, Vector3f var1, Matrix4f var2, Vector3f var3) {
		Vector4f var4 = new Vector4f(var.x - var1.x, var.y - var1.y, var.z - var1.z, 1.0F);
		Matrix4f.transform(var2, var4, var4);
		var4.x *= var3.x;
		var4.y *= var3.y;
		var4.z *= var3.z;
		var.set(var4.x + var1.x, var4.y + var1.y, var4.z + var1.z);
	}

	private Matrix4f a() {
		Matrix4f var = new Matrix4f();
		var.setIdentity();
		return var;
	}

	public static EnumFacing a(int[] var) {
		Vector3f var1 = new Vector3f(Float.intBitsToFloat(var[0]), Float.intBitsToFloat(var[1]), Float.intBitsToFloat(var[2]));
		Vector3f var2 = new Vector3f(Float.intBitsToFloat(var[7]), Float.intBitsToFloat(var[8]), Float.intBitsToFloat(var[9]));
		Vector3f var3 = new Vector3f(Float.intBitsToFloat(var[14]), Float.intBitsToFloat(var[15]), Float.intBitsToFloat(var[16]));
		Vector3f var4 = new Vector3f();
		Vector3f var5 = new Vector3f();
		Vector3f var6 = new Vector3f();
		Vector3f.sub(var1, var2, var4);
		Vector3f.sub(var3, var2, var5);
		Vector3f.cross(var5, var4, var6);
		float var7 = (float) Math.sqrt((double) (var6.x * var6.x + var6.y * var6.y + var6.z * var6.z));
		var6.x /= var7;
		var6.y /= var7;
		var6.z /= var7;
		EnumFacing var8 = null;
		float var9 = 0.0F;
		EnumFacing[] var10 = EnumFacing.values();
		int var11 = var10.length;

		for (int var12 = 0; var12 < var11; ++var12) {
			EnumFacing var13 = var10[var12];
			Vec3i var14 = var13.getDirectionVec();
			Vector3f var15 = new Vector3f((float) var14.getX(), (float) var14.getY(), (float) var14.getZ());
			float var16 = Vector3f.dot(var6, var15);
			if (var16 >= 0.0F && var16 > var9) {
				var9 = var16;
				var8 = var13;
			}
		}

		if (var8 == null) {
			return EnumFacing.UP;
		} else {
			return var8;
		}
	}

	public void a(int[] data, EnumFacing facing, BlockFaceUV blockPartFace) {
		for (int i = 0; i < 4; ++i) {
			this.a(i, data, facing, blockPartFace);
		}

	}

	private void a(int[] var, EnumFacing var1) {
		int[] var2 = new int[var.length];
		System.arraycopy(var, 0, var2, 0, var.length);
		float[] var3 = new float[EnumFacing.values().length];
		var3[Constants.WEST] = 999.0F;
		var3[Constants.DOWN] = 999.0F;
		var3[Constants.NORTH] = 999.0F;
		var3[Constants.EAST] = -999.0F;
		var3[Constants.UP] = -999.0F;
		var3[Constants.SOUTH] = -999.0F;

		int var5;
		float var8;
		for (int var4 = 0; var4 < 4; ++var4) {
			var5 = 7 * var4;
			float var6 = Float.intBitsToFloat(var2[var5]);
			float var7 = Float.intBitsToFloat(var2[var5 + 1]);
			var8 = Float.intBitsToFloat(var2[var5 + 2]);
			if (var6 < var3[Constants.WEST]) {
				var3[Constants.WEST] = var6;
			}

			if (var7 < var3[Constants.DOWN]) {
				var3[Constants.DOWN] = var7;
			}

			if (var8 < var3[Constants.NORTH]) {
				var3[Constants.NORTH] = var8;
			}

			if (var6 > var3[Constants.EAST]) {
				var3[Constants.EAST] = var6;
			}

			if (var7 > var3[Constants.UP]) {
				var3[Constants.UP] = var7;
			}

			if (var8 > var3[Constants.SOUTH]) {
				var3[Constants.SOUTH] = var8;
			}
		}

		EnumFaceDirection var17 = EnumFaceDirection.getFacing(var1);

		for (var5 = 0; var5 < 4; ++var5) {
			int var18 = 7 * var5;
			EnumFaceDirection.VertexInformation var19 = var17.getVertexInformation(var5);
			var8 = var3[var19.xIndex];
			float var9 = var3[var19.yIndex];
			float var10 = var3[var19.zIndex];
			var[var18] = Float.floatToRawIntBits(var8);
			var[var18 + 1] = Float.floatToRawIntBits(var9);
			var[var18 + 2] = Float.floatToRawIntBits(var10);

			for (int var11 = 0; var11 < 4; ++var11) {
				int var12 = 7 * var11;
				float var13 = Float.intBitsToFloat(var2[var12]);
				float var14 = Float.intBitsToFloat(var2[var12 + 1]);
				float var15 = Float.intBitsToFloat(var2[var12 + 2]);
				if (MathHelper.epsilonEquals(var8, var13) && MathHelper.epsilonEquals(var9, var14) && MathHelper.epsilonEquals(var10, var15)) {
					var[var18 + 4] = var2[var12 + 4];
					var[var18 + 4 + 1] = var2[var12 + 4 + 1];
				}
			}
		}

	}

	private void a(int count, int[] data, EnumFacing enumFacing, BlockFaceUV blockPartFace) {
		int var5 = 7 * count;
		float var6 = Float.intBitsToFloat(data[var5]);
		float var7 = Float.intBitsToFloat(data[var5 + 1]);
		float var8 = Float.intBitsToFloat(data[var5 + 2]);
		if (var6 < -0.1F || var6 >= 1.1F) {
			var6 -= (float) MathHelper.floor_double_long(var6);
		}

		if (var7 < -0.1F || var7 >= 1.1F) {
			var7 -= (float) MathHelper.floor_double_long(var7);
		}

		if (var8 < -0.1F || var8 >= 1.1F) {
			var8 -= (float) MathHelper.floor_double_long(var8);
		}

		float var9 = 0.0F;
		float var10 = 0.0F;
		switch (enumFacing) {
			case DOWN:
				var9 = var6 * 16.0F;
				var10 = (1.0F - var8) * 16.0F;
				break;
			case UP:
				var9 = var6 * 16.0F;
				var10 = var8 * 16.0F;
				break;
			case NORTH:
				var9 = (1.0F - var6) * 16.0F;
				var10 = (1.0F - var7) * 16.0F;
				break;
			case SOUTH:
				var9 = var6 * 16.0F;
				var10 = (1.0F - var7) * 16.0F;
				break;
			case WEST:
				var9 = var8 * 16.0F;
				var10 = (1.0F - var7) * 16.0F;
				break;
			case EAST:
				var9 = (1.0F - var8) * 16.0F;
				var10 = (1.0F - var7) * 16.0F;
		}

		int var11 = blockPartFace.func_178345_c(count) * 7;
		data[var11 + 4] = Float.floatToRawIntBits(var9 / 16f);
		data[var11 + 4 + 1] = Float.floatToRawIntBits(var10 / 16f);
	}
}