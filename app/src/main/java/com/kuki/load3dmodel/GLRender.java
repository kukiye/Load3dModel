package com.kuki.load3dmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Message;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author ：yeton
 * date : 2021/2/22 17:19
 * package：com.kuki.load3dmodel
 * description :
 */
public class GLRender implements GLSurfaceView.Renderer {

    public static final String TAG = GLRender.class.getSimpleName();

    public World myWorld;
    private FrameBuffer frameBuffer;
    private Object3D selectedObj;
    private SimpleVector simpleVector;

    public GLRender(Context context) {
        myWorld = new World();
        myWorld.setAmbientLight(25, 25, 25);

        Light light = new Light(myWorld);
        light.setIntensity(250, 250, 250);
        light.setPosition(new SimpleVector(0, 0, -15));

        Camera cam = myWorld.getCamera();
        cam.setFOVLimits(0.1f, 2.0f);
        cam.setFOV(1.08f);
        cam.setYFOV(1.92f);
        cam.setClippingPlanes(0f, 2000f);
        /*System.out.println(cam.getFOV());
        System.out.println(cam.getYFOV());
        System.out.println(cam.getPosition());
        String[] names = Config.getParameterNames();
        for (String i : names) {
            System.out.println(i);
        }*/

    }


    public void onSurfaceChanged(GL10 gl, int w, int h) {
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
        frameBuffer = new FrameBuffer(gl, w, h);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0.3f);
    }

    public void onDrawFrame(GL10 gl) {
        frameBuffer.clear(Color.TRANSPARENT);
        myWorld.renderScene(frameBuffer);
        myWorld.draw(frameBuffer);

        frameBuffer.display();
    }


    public void addObject(Context context) {
        Object3D newObject = null;
        try {
            createTextures(context);
            Object3D[] objectsArray2 = Loader.loadOBJ(context.getResources().getAssets().open("shoes.obj"), context.getResources()
                    .getAssets().open("shoes.mtl"), 1f);
            newObject = Object3D.mergeAll(objectsArray2);
            newObject.setTexture("shoes_texture");
            newObject.setOrigin(new SimpleVector(0, 0, 300));
            newObject.rotateZ(3.1415926f);
            newObject.setName("shoes.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        newObject.strip();
        newObject.build();
        Message msg = new Message();
        msg.what = MainActivity.MSG_LOAD_MODEL_SUC;
        msg.obj = newObject;
        MainActivity.handler.sendMessage(msg);
        selectedObj = newObject;
    }

    public void applyTranslation(float incX, float incY, float incZ) {
        if (this.selectedObj != null) {

            SimpleVector objOrigin = this.selectedObj.getOrigin();
            SimpleVector currentPoition = this.selectedObj.getTransformedCenter();
            System.out.println(currentPoition);
            this.selectedObj.translate(incX, incY, incZ);
        }
    }

    public void rotate(float incX, float incY, float incZ) {
        if (this.selectedObj != null) {

            //            SimpleVector objOrigin = this.selectedObj.getOrigin();
            //            SimpleVector currentPoition = this.selectedObj.getTransformedCenter();
            //            System.out.println(currentPoition);

            this.selectedObj.rotateX(incX);
            this.selectedObj.rotateY(incY);
            this.selectedObj.rotateZ(incZ);

            /*if (simpleVector == null) {
                simpleVector = new SimpleVector();
            }
            simpleVector.set(incX, incY, incZ);

            selectedObj.rotateAxis(simpleVector, 1);*/
        }
    }

    public void rotateX(float incX) {
        if (this.selectedObj != null) {

            //            SimpleVector objOrigin = this.selectedObj.getOrigin();
            //            SimpleVector currentPoition = this.selectedObj.getTransformedCenter();
            //            System.out.println(currentPoition);

            this.selectedObj.rotateX(incX);
            //            this.selectedObj.rotateZ(incZ);

            /*if (simpleVector == null) {
                simpleVector = new SimpleVector();
            }
            simpleVector.set(incX, incY, incZ);

            selectedObj.rotateAxis(simpleVector, 1);*/
        }
    }

    public void rotateY( float incY) {
        if (this.selectedObj != null) {

            //            SimpleVector objOrigin = this.selectedObj.getOrigin();
            //            SimpleVector currentPoition = this.selectedObj.getTransformedCenter();
            //            System.out.println(currentPoition);

            this.selectedObj.rotateY(incY);
            //            this.selectedObj.rotateZ(incZ);

            /*if (simpleVector == null) {
                simpleVector = new SimpleVector();
            }
            simpleVector.set(incX, incY, incZ);

            selectedObj.rotateAxis(simpleVector, 1);*/
        }
    }

    /*public void rotate(float incX, float incY, float incZ) {
        if (this.selectedObj != null) {

            SimpleVector objOrigin = this.selectedObj.getOrigin();
            SimpleVector currentPoition = this.selectedObj.getTransformedCenter();
            System.out.println(currentPoition);

           *//* this.selectedObj.rotateX(incX);
            this.selectedObj.rotateY(incY);
            this.selectedObj.rotateZ(incZ);*//*

            if (simpleVector == null) {
                simpleVector = new SimpleVector();
            }
            simpleVector.set(incX, incY, incZ);

            selectedObj.rotateAxis(simpleVector, 1);
        }
    }*/

    private void createTextures(Context context) {

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.shoes);
        Log.e(TAG, "bitmap==" + bitmap);

        Texture texture = new Texture(bitmap);
        if (!TextureManager.getInstance().containsTexture("shoes_texture")) {
            TextureManager.getInstance().addTexture("shoes_texture", texture);
        }
    }
}

