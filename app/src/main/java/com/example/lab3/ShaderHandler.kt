package com.example.lab3

import android.content.Context
import android.opengl.GLES20
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ShaderHandler {
    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0
    private var shaderProgram: Int = 0

    fun loadShaders(context: Context, vertexSrcId: Int, fragmentSrcId: Int): Int {
        println("Vertex shader")
        val vertexSrc = BufferedReader(context.resources.openRawResource(vertexSrcId).reader()).readText()
        vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSrc)
        println("Fragment shader")
        val fragmentSrc = BufferedReader(context.resources.openRawResource(fragmentSrcId).reader()).readText()
        fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc)
        shaderProgram = GLES20.glCreateProgram()
        getErrors()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        getErrors()
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        getErrors()
        GLES20.glLinkProgram(shaderProgram)
        println(GLES20.glGetProgramInfoLog(shaderProgram))
        getErrors()
        return shaderProgram
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        getErrors()
        GLES20.glShaderSource(shader, source)
        getErrors()
        GLES20.glCompileShader(shader)
        println(GLES20.glGetShaderInfoLog(shader))
        getErrors()
        return shader
    }
}