package com.ok.yo.lash;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.google.ar.core.Frame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by buckfast on 25.10.2017.
 */

public class CameraRenderer {

    private int textureId = -666;
    private int textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

    private final int FLOAT_SIZE = 4;

    private FloatBuffer quadVerts;
    private FloatBuffer quadTexCoords;
    private FloatBuffer quadTexCoordsTransformed;

    private int quadProgram;
    private int quadPositionParam;
    private int quadTexCoordParam;


    public final float[] QUAD_VERT_COORDS = new float[]{
            -1.0f, -1.0f, 0.0f,
            -1.0f, +1.0f, 0.0f,
            +1.0f, -1.0f, 0.0f,
            +1.0f, +1.0f, 0.0f,
    };

    public final float[] QUAD_TEX_COORDS = new float[]{
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    //get context as parameter to run in GL thread
    public void create(Context c) {
        int textures[] = new int[1];

        GLES20.glGenTextures(1,textures,0); //amount, where to, offset
        textureId = textures[0];
        GLES20.glBindTexture(textureTarget,textureId);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


        //convert float data to bytebuffer for opengl
        ByteBuffer vertices = ByteBuffer.allocateDirect(QUAD_VERT_COORDS.length * FLOAT_SIZE);
        vertices.order(ByteOrder.nativeOrder());
        quadVerts = vertices.asFloatBuffer();
        quadVerts.put(QUAD_VERT_COORDS);
        quadVerts.position(0);

        ByteBuffer texCoords = ByteBuffer.allocateDirect(4 * 2 * FLOAT_SIZE); //number of vertices * texture coordinates per vertex * float size
        texCoords.order(ByteOrder.nativeOrder());
        quadTexCoords = texCoords.asFloatBuffer();
        quadTexCoords.put(QUAD_TEX_COORDS);
        quadTexCoords.position(0);

        //used when screen size changes
        ByteBuffer texCoordsTransformed = ByteBuffer.allocateDirect(4 * 2 * FLOAT_SIZE); //number of vertices * texture coordinates per vertex * float size
        texCoordsTransformed.order(ByteOrder.nativeOrder());
        quadTexCoordsTransformed = texCoordsTransformed.asFloatBuffer();



        int vertexShader = Utils.loadShader(c, GLES20.GL_VERTEX_SHADER, R.raw.quad_vert);
        int fragmentShader = Utils.loadShader(c, GLES20.GL_FRAGMENT_SHADER, R.raw.quad_frag);

        quadProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(quadProgram, vertexShader);
        GLES20.glAttachShader(quadProgram, fragmentShader);
        GLES20.glLinkProgram(quadProgram);
        GLES20.glUseProgram(quadProgram);

        quadPositionParam = GLES20.glGetAttribLocation(quadProgram, "a_Position");
        quadTexCoordParam = GLES20.glGetAttribLocation(quadProgram, "a_TexCoords");
    }

    public void draw(Frame frame) {

        if (frame.isDisplayRotationChanged()) {
            frame.transformDisplayUvCoords(quadTexCoords, quadTexCoordsTransformed);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glUseProgram(quadProgram);

        //set vertex position params for shaders  -- second parameter: number of coords per vertex
        GLES20.glVertexAttribPointer(quadPositionParam, 3, GLES20.GL_FLOAT, false, 0, quadVerts);

        //set texture coordinates for shaders -- second parameter: number of tex coords per vertex
        GLES20.glVertexAttribPointer(quadTexCoordParam, 2, GLES20.GL_FLOAT, false, 0, quadTexCoordsTransformed);


        //draw
        GLES20.glEnableVertexAttribArray(quadPositionParam);
        GLES20.glEnableVertexAttribArray(quadTexCoordParam);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(quadPositionParam);
        GLES20.glDisableVertexAttribArray(quadTexCoordParam);

        //enable depth for further drawing
        GLES20.glDepthMask(true);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }

    public int getTextureId() {
        return textureId;
    }
}
