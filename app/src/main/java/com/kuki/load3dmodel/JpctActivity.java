package com.kuki.load3dmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author ：yeton
 * date : 2021/2/23 18:04
 * package：com.kuki.load3dmodel
 * description :
 */
public class JpctActivity extends AppCompatActivity {

    public final static int MSG_LOAD_MODEL_SUC = 0;

    //GLSurfaceView,负责OpenGL渲染
    private GLSurfaceView mGLSurfaceView;
    //自定义Renderer类(渲染器)
    private GlRenderer mRenderer;

    //jpct_ae中的3D物体
    private Object3D object3D = null;
    //位置
    private float xpos = -1;
    private float ypos = -1;

    //旋转角
    private float rotateX;
    private float rotateY;

    //帧缓冲对象
    private FrameBuffer fb = null;

    //jpct_ae中的世界
    private World world = null;
    private RGBColor backColor = new RGBColor(50, 50, 100);

    private static JpctActivity master = null;

    //光照类
    private Light light = null;

    public Handler mHandler;
    private Thread threadLoadModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        mRenderer = new GlRenderer();
        mGLSurfaceView.setRenderer(mRenderer);

        setContentView(mGLSurfaceView);

        laodData();

    }

    private void laodData() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LOAD_MODEL_SUC:
                        Toast.makeText(JpctActivity.this, "模型加载成功", Toast.LENGTH_SHORT).show();
                        object3D = (Object3D) msg.obj;
                        world.addObject(object3D);

                        setData();
                        break;
                }
            }
        };

        threadLoadModel = new Thread(new Runnable() {
            @Override
            public void run() {
                mRenderer.addObject(JpctActivity.this);
            }
        });
        Toast.makeText(JpctActivity.this, "模型加载中...", Toast.LENGTH_SHORT).show();
        threadLoadModel.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        // TODO Auto-generated method stub

        // 按键开始
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            // 保存按下的初始x,y位置于xpos,ypos中
            xpos = me.getX();
            ypos = me.getY();
            return true;
        }
        // 按键结束
        if (me.getAction() == MotionEvent.ACTION_UP) {
            // 设置x,y及旋转角度为初始值
            xpos = -1;
            ypos = -1;
            rotateX = 0;
            rotateY = 0;
            return true;
        }

        if (me.getAction() == MotionEvent.ACTION_MOVE) {
            // 计算x,y偏移位置及x,y轴上的旋转角度
            float xd = me.getX() - xpos;
            float yd = me.getY() - ypos;

            xpos = me.getX();
            ypos = me.getY();

            rotateX = xd / -100f;
            rotateY = yd / -100f;
            return true;
        }

        // 每Move一下休眠毫秒
        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }
        return super.onTouchEvent(me);
    }

    /**
     * Renderer类
     */
    public class GlRenderer implements GLSurfaceView.Renderer {

        private boolean stop = false;

        public void setStop() {
            stop = true;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // 如果FrameBuffer不为NULL,释放fb所占资源
            if (fb != null) {
                fb.dispose();
            }

            // 创建一个宽度为w,高为h的FrameBuffer
            fb = new FrameBuffer(gl, width, height);

            // 如果master为空
            if (master == null) {

                // 实例化World对象
                world = new World();

                // 设置了环境光源强度。负:整个场景会变暗;正:将照亮了一切。
                world.setAmbientLight(25, 25, 25);

                // 在World中创建一个新的光源
                light = new Light(world);

               /* // 创建一个纹理
                Bitmap image = BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.shoes)), 64, 64);
                Texture texture = new Texture(image);

                // 纹理的名字
                String textureName = "texture";

                // TextureManager.getInstance()取得一个Texturemanager对象
                // addTexture(textureName,texture)添加一个纹理
                TextureManager.getInstance().addTexture(textureName, texture);

                // Object3D对象开始了:-)

                // Primitives提供了一些基本的三维物体，假如你为了测试而生成一些对象或为
                // 其它目的使用这些类将很明智，因为它即快速又简单，不需要载入和编辑。
                // 调用public static Object3D getCube(float scale) scale:角度
                // 返回一个立方体
                cube = Primitives.getCube(10);

                // 以纹理的方式给对象所有面"包装"上纹理
                cube.calcTextureWrapSpherical();

                // 给对象设置纹理
                cube.setTexture(textureName);

                // 除非你想在事后再用PolygonManager修改,否则释放那些不再需要数据的内存
                cube.strip();

                // 初始化一些基本的对象是几乎所有进一步处理所需的过程。
                // 如果对象是"准备渲染"(装载，纹理分配，安置，渲染模式设置，
                // 动画和顶点控制器分配),那么build()必须被调用，
                cube.build();

                // 将Object3D对象添加到world集合
                world.addObject(cube);*/


            }
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (!stop) {
                // 如果rotateX不为0,向Y轴旋转rotateX角度
                if (rotateX != 0) {
                    // 旋转物体的绕Y旋转
                    object3D.rotateY(rotateX);
                    // 将rotateX置0
                    rotateX = 0;
                }

                if (rotateY != 0) {
                    // 旋转物体的旋转围绕x由给定角度宽（弧度，逆时针为正值）轴矩阵,应用到对象下一次渲染时。
                    object3D.rotateX(rotateY);
                    // 将rotateY置0
                    rotateY = 0;
                }

                // 用给定的颜色(backColor)清除FrameBuffer
                fb.clear(backColor);

                // 变换和灯光所有多边形
                world.renderScene(fb);

                // 绘制
                world.draw(fb);

                // 渲染图像显示
                fb.display();
            } else {
                if (fb != null) {
                    fb.dispose();
                    fb = null;
                }
            }
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
            msg.what = MSG_LOAD_MODEL_SUC;
            msg.obj = newObject;
            mHandler.sendMessage(msg);

        }

        private void createTextures(Context context) {

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.shoes);
            //            Log.e(TAG, "bitmap==" + bitmap);

            Texture texture = new Texture(bitmap);
            if (!TextureManager.getInstance().containsTexture("shoes_texture")) {
                TextureManager.getInstance().addTexture("shoes_texture", texture);
            }
        }
    }

    private void setData() {
        // 该Camera代表了Camera/viewer在当前场景的位置和方向，它也包含了当前视野的有关信息
        // 你应该记住Camera的旋转矩阵实际上是应用在World中的对象的一个旋转矩阵。
        // 这一点很重要，当选择了Camera的旋转角度，一个Camera(虚拟)围绕w旋转和通过围绕World围绕w旋转、
        // 将起到相同的效果，因此，考虑到旋转角度，World围绕camera时，camera的视角是静态的。假如你不喜欢
        // 这种习惯，你可以使用rotateCamera()方法
        Camera cam = world.getCamera();

        // 以50有速度向后移动Camera（相对于目前的方向）
        cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);

        // cub.getTransformedCenter()返回对象的中心
        // cam.lookAt(SimpleVector lookAt))
        // 旋转这样camera以至于它看起来是在给定的world-space 的位置
        cam.lookAt(object3D.getTransformedCenter());

        // SimpleVector是一个代表三维矢量的基础类，几乎每一个矢量都
        // 是用SimpleVector或者至少是一个SimpleVector变体构成的(有时由于
        // 某些原因比如性能可能会用(float x,float y,float z)之类)。
        SimpleVector simpleVector = new SimpleVector();

        // 将当前SimpleVector的x,y,z值设为给定的SimpleVector(cube.getTransformedCenter())的值
        simpleVector.set(object3D.getTransformedCenter());

        // Y方向上减去100
        simpleVector.y -= 100;

        // Z方向上减去100
        simpleVector.z -= 100;

        // 设置光源位置
        light.setPosition(simpleVector);
        light.setIntensity(250, 250, 250);
        light.setPosition(new SimpleVector(0, 0, -15));

        // 强制GC和finalization工作来试图去释放一些内存，同时将当时的内存写入日志，
        // 这样可以避免动画不连贯的情况，然而，它仅仅是减少这种情况发生的机率
        MemoryHelper.compact();

        // 如果master为空,使用日志记录且设master为HelloWorld本身
        if (master == null) {
            // Logger.log("Saving master Activity!");
            master = JpctActivity.this;
        }
    }
}
