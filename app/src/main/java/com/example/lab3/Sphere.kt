package com.example.lab3

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

fun getErrors(){
    var error = GLES20.glGetError()
    while (error != GLES20.GL_NO_ERROR){
        when(error){
            GLES20.GL_INVALID_ENUM -> println("ERROR: Invalid enum")
            GLES20.GL_INVALID_VALUE -> println("ERROR: Invalid value")
            GLES20.GL_INVALID_OPERATION -> println("ERROR: Invalid operation")
            GLES20.GL_INVALID_FRAMEBUFFER_OPERATION -> println("ERROR: Invalid framebuffer operation")
            GLES20.GL_OUT_OF_MEMORY -> println("ERROR: Out of memory")
        }
    }
}

class Sphere(cX: Float, cY: Float, cZ: Float, radius: Float, private val shaderProgram: Int, private val textureId: Int) {
    private val vertices: MutableList<Float> = mutableListOf()
    private val indices: MutableList<Short> = mutableListOf()
    private val texCoords: MutableList<Float> = mutableListOf()

    private val stackCount: Int = 26
    private val sectorCount: Int = 26

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer

    private val modelMatrix = FloatArray(16)
    private val transformationMatrix = FloatArray(16)
    private var originMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val vertexStride = 3 * Float.SIZE_BYTES
    private val textureStride = 2 * Float.SIZE_BYTES
    private val color = floatArrayOf(0.5607843f, 0.843137f, 0.858823f, 1.0f)

    private val positionHandle: Int
    private val textureHandle: Int
    private val textureUnitLocation: Int
    private val colorHandle: Int
    private val mvpMatrixLocation: Int

    init {
        val stackStep = Math.PI / stackCount
        val sectorStep = 2 * Math.PI / sectorCount
        for(i in 0 .. stackCount){
            val stackAngle = Math.PI / 2 - i * stackStep
            val xz = radius * cos(stackAngle)
            val y = radius * sin(stackAngle)
            for(j in 0 .. sectorCount){
                val sectorAngle = j * sectorStep
                val x = xz * sin(sectorAngle)
                val z = xz * cos(sectorAngle)
                vertices.add((x + cX).toFloat())
                vertices.add((y + cY).toFloat())
                vertices.add((z + cZ).toFloat())

                val s = j.toFloat() / sectorCount
                val t = i.toFloat() / stackCount
                texCoords.add(s)
                texCoords.add(t)
            }
        }

        for(i in 0 until stackCount){
            var k1 = i * (sectorCount + 1)
            var k2 = k1 + sectorCount + 1
            for(j in 0 until sectorCount){
                if(i != 0){
                    indices.add(k1.toShort())
                    indices.add(k2.toShort())
                    indices.add((k1 + 1).toShort())
                }
                if((i + 1) != stackCount){
                    indices.add((k1 + 1).toShort())
                    indices.add(k2.toShort())
                    indices.add((k2 + 1).toShort())
                }
                k1++
                k2++
            }
        }

        vertexBuffer =
            ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(vertices.toFloatArray())
                    position(0)
                }
            }

        textureBuffer =
            ByteBuffer.allocateDirect(texCoords.size * Float.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(texCoords.toFloatArray())
                    position(0)
                }
            }

        indexBuffer =
            ByteBuffer.allocateDirect(indices.size * Short.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asShortBuffer().apply {
                    put(indices.toShortArray())
                    position(0)
                }
            }

        GLES20.glUseProgram(shaderProgram)
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        GLES20.glVertexAttribPointer(
            positionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        textureHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoords")
        GLES20.glVertexAttribPointer(
            textureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            textureStride,
            textureBuffer
        )

        textureUnitLocation = GLES20.glGetUniformLocation(shaderProgram, "u_TextureUnit")
        colorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor")
        mvpMatrixLocation = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        GLES20.glUseProgram(0)

        Matrix.setIdentityM(transformationMatrix, 0)
        Matrix.setIdentityM(originMatrix, 0)
        Matrix.setIdentityM(rotationMatrix, 0)
    }

    fun draw(vpMatrix: FloatArray){
        GLES20.glUseProgram(shaderProgram)

        GLES20.glVertexAttribPointer(
            positionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        GLES20.glVertexAttribPointer(
            textureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            textureStride,
            textureBuffer
        )

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(textureHandle)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glUniform1i(textureUnitLocation, 0)
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        Matrix.multiplyMM(modelMatrix, 0, originMatrix, 0, transformationMatrix, 0)
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpMatrixLocation, 1, false, mvpMatrix, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)
        GLES20.glUseProgram(0)
        getErrors()
    }
    fun translate(x: Float, y: Float, z: Float){
        Matrix.translateM(transformationMatrix, 0, x, y, z)
    }

    fun rotate(angle: Float, x: Float, y: Float, z: Float){
        Matrix.rotateM(transformationMatrix, 0, angle, x, y, z)
    }

    fun rotateSelf(angle: Float, x: Float, y: Float, z: Float){
        Matrix.rotateM(rotationMatrix, 0, angle, x, y, z)
    }

    fun scale(x: Float, y: Float, z: Float){
        Matrix.scaleM(transformationMatrix, 0, x, y, z)
    }

    fun setRotate(angle: Float, x: Float, y: Float, z: Float){
        Matrix.setRotateM(transformationMatrix, 0, angle, x, y, z)
    }

    fun getTransformationMatrix(): FloatArray {
        return transformationMatrix
    }

    fun applyOriginTransformation(origin: FloatArray){
        originMatrix = origin
    }
}