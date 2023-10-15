package com.example.lab3

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates

class MyGLRenderer(private val context: Context): GLSurfaceView.Renderer {
    private val vpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val shaderHandler = ShaderHandler()
    private val textureHandler = TextureHandler()

    private var sphereShader by Delegates.notNull<Int>()

    private var sunTexture by Delegates.notNull<Int>()
    private var earthTexture by Delegates.notNull<Int>()
    private var moonTexture by Delegates.notNull<Int>()

    private lateinit var sun: Sphere
    private lateinit var earth: Sphere
    private lateinit var moon: Sphere

    private val sunRotationSpeed = 0.5f
    private val earthRotationSpeed = 2f
    private val earthOrbitalSpeed = 1f
    private val moonOrbitalSpeed = 2f

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        sphereShader = shaderHandler.loadShaders(context, R.raw.vertex, R.raw.fragment)

        sunTexture = textureHandler.loadTexture(context, R.drawable.sun)
        earthTexture = textureHandler.loadTexture(context, R.drawable.earth_1)
        moonTexture = textureHandler.loadTexture(context, R.drawable.moon)

        sun = Sphere(0f, 0f, 0f, 2f, sphereShader, sunTexture)
        earth = Sphere(0f, 0f, 0f, 1f, sphereShader, earthTexture)
        earth.translate(0f, 0f, 5f)
        moon = Sphere(0f, 0f, 0f, 0.5f, sphereShader, moonTexture)
        moon.applyOriginTransformation(earth.getTransformationMatrix())
        moon.translate(0f, 0f, 2f)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 100f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        sun.rotate(sunRotationSpeed, 0f, 1f, 0f)
        sun.draw(vpMatrix)

        earth.rotateSelf(earthRotationSpeed, 0f, 1f, 0f)
        earth.translate(0f, 0f, -5f)
        earth.rotate(earthOrbitalSpeed, 0f, 1f, 0f)
        earth.translate(0f, 0f, 5f)

        moon.applyOriginTransformation(earth.getTransformationMatrix())

        moon.translate(0f, 0f, -2f)
        moon.rotate(moonOrbitalSpeed, 0f, 1f, 0f)
        moon.translate(0f, 0f, 2f)
        earth.draw(vpMatrix)
        moon.draw(vpMatrix)
    }
}