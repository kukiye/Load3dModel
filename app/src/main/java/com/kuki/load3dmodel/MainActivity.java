package com.kuki.load3dmodel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.threed.jpct.Object3D;

public class MainActivity extends AppCompatActivity {
    public final static int MSG_LOAD_MODEL_SUC = 0;


    private GLSurfaceView myGLView;
    private GLRender myRenderer;
    private Button btnLoad;
    private Button btnLeft;
    private Button btnRight;
    private Button btnTop;
    private Button btnDown;

    private Thread threadLoadModel;
    public static Handler handler;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private long timestamp;
    private float angleX;
    private float angleY;
    private float angleZ;
    private float lastX;
    private float lastY;
    private boolean canRotate;
    private boolean canScale;
    private float last1X;
    private float last1Y;
    private long downTime;
    private long moveSpeed = 200;

    // for touch event handling
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_DRAG = 1;
    private static final int TOUCH_ZOOM = 2;
    private int touchMode = TOUCH_NONE;

    //这里将偏移数值降低
    private final float TOUCH_SCALE_FACTOR = 180.0f / 1080 / 2;
    private float previousX;
    private float previousY;
    private boolean isRotate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LOAD_MODEL_SUC:
                        Toast.makeText(MainActivity.this, "模型加载成功", Toast.LENGTH_SHORT).show();
                        Object3D object3D = (Object3D) msg.obj;
                        myRenderer.myWorld.addObject(object3D);
                        break;
                }
            }
        };
        btnLoad = findViewById(R.id.btnLoadModel);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnTop = findViewById(R.id.btnTop);
        btnDown = findViewById(R.id.btnDown);

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "开始加载模型", Toast.LENGTH_SHORT).show();
                threadLoadModel.start();
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRenderer.applyTranslation(-10, 0, 0);
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRenderer.applyTranslation(10, 0, 0);
            }
        });
        btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRenderer.applyTranslation(0, -10, 0);
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRenderer.applyTranslation(0, 10, 0);
            }
        });

        myGLView = (GLSurfaceView) this.findViewById(R.id.surfaceView);
        myGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        myGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        myGLView.setZOrderOnTop(true);
        myRenderer = new GLRender(this);
        myGLView.setRenderer(myRenderer);

        threadLoadModel = new Thread(new Runnable() {
            @Override
            public void run() {
                myRenderer.addObject(MainActivity.this);
            }
        });

        //        initSensor();

        //        initTouchListener();
        initOtherTouchListener();
    }

    private void initOtherTouchListener() {

        myGLView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                rotateModel(event);
                return true;
            }
        });

    }

    private void initTouchListener() {

        //单指旋转，双指缩放，超过双指不做处理
        myGLView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        //单指触摸情况
                        lastX = event.getX();
                        lastY = event.getY();
                        canRotate = true;
                        canScale = false;
                        downTime = event.getDownTime();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        //双指触摸
                        canRotate = false;
                        canScale = true;
                        //多指的情况
                        if (event.getPointerCount() > 2) {
                            canScale = false;
                            return false;
                        }

                        int index0 = event.getPointerId(0);
                        int index1 = event.getPointerId(1);
                        lastX = event.getX(index0);
                        lastY = event.getY(index0);
                        last1X = event.getX(index1);
                        last1Y = event.getY(index1);
                        //                        controler.rotate(0.0f, 0.0f, 0.0f)
                        break;
                    case MotionEvent.ACTION_POINTER_UP:

                        //还原
                        canScale = false;
                        canRotate = false;
                        //                        controler.rotate(0.0f, 0.0f, 0.0f)
                        break;

                    case MotionEvent.ACTION_MOVE:

                        if (canRotate) {
                            if (event.getPointerCount() >= 2) {
                                //                                controler.rotate(0.0f, 0.0f, 0.0f)
                                return true;
                            }
                            if ((event.getEventTime() - downTime) > moveSpeed) {
                                float curY = event.getY();
                                float curX = event.getX();
                                float vecX = curX - lastX;
                                float vecY = curY - lastY;
                                lastY = curY;
                                lastX = curX;
                                double distance = Math.sqrt(toDouble(vecX * vecX) + toDouble(vecY * vecY));
                                myRenderer.rotate(vecX, vecY, Float.parseFloat(String.valueOf(distance)));

                                downTime = event.getEventTime();
                            }
                        } else if (canScale) {
                            //通过双指按下的距离比例来进行缩放，但是这里的比例并不实际缩放的比例，只是通过简单调整视野的范围来进行缩放
                            int pointer0 = event.getPointerId(0);
                            int pointer1 = event.getPointerId(1);
                            float curX = event.getX(pointer0);
                            float curY = event.getY(pointer0);
                            float cur1X = event.getX(pointer1);
                            float cur1Y = event.getY(pointer1);
                            double lastDis = Math.sqrt(Math.pow((lastX - last1X), 2.0) + Math.pow((lastY - last1Y), 2.0));
                            double curDis = Math.sqrt(Math.pow((curX - cur1X), 2.0) + Math.pow((curY - cur1Y), 2.0));
                            //                            controler.scale((curDis / lastDis).toFloat())
                        }

                        break;


                }
                return true;
            }
        });


    }

    /**
     * 单指旋转model
     */
    private void rotateModel(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // start drag
            case MotionEvent.ACTION_DOWN:
                if (touchMode == TOUCH_NONE && event.getPointerCount() == 1) {
                    touchMode = TOUCH_DRAG;
                    previousX = event.getX();
                    previousY = event.getY();

                    downTime = event.getDownTime();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchMode == TOUCH_DRAG) {
                    float x = event.getX();
                    float y = event.getY();

                    float dx = x - previousX;
                    float dy = y - previousY;
                    //一次只移动一个方向
                    previousX = x;
                    previousY = y;

                    if (isRotate) {
                        if ((event.getEventTime() - downTime) > moveSpeed) {
                            if (Math.abs(dx) > Math.abs(dy)) {
                                angleX = (angleX + dx * TOUCH_SCALE_FACTOR) %
                                        360.0f;

                            } else {
                                angleY = (angleY + dy * TOUCH_SCALE_FACTOR) %
                                        360.0f;
                            }
                            myRenderer.rotate(angleX, angleY, 0);

                            downTime = event.getEventTime();
                        }

                    } else {
                        // change view point
                        /*glRender.positionX += dx * TOUCH_SCALE_FACTOR / 5;
                        glRender.positionY += dy * TOUCH_SCALE_FACTOR / 5;*/
                    }
                   /* glRender.requestRedraw();
                    requestRender();*/
                }
                break;

            // end drag
            case MotionEvent.ACTION_UP:
                //                registerSensor(true);
                if (touchMode == TOUCH_DRAG) {
                    touchMode = TOUCH_NONE;
                    break;
                }
                //                glRender.setsclae();
        }
    }


   /* private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 陀螺仪
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (timestamp != 0) {
                        final float dT = (sensorEvent.timestamp - timestamp) * 1;
                        angleX += sensorEvent.values[0] * dT * 180.0f % 360.0f;
                        angleY += sensorEvent.values[1] * dT * 180.0f % 360.0f;
                        angleZ += sensorEvent.values[2] * dT * 180.0f % 360.0f;

                        myRenderer.rotate(angleX, angleY, angleZ);
                    }
                    timestamp = sensorEvent.timestamp;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager
                .SENSOR_DELAY_GAME);
    }*/

    public Double toDouble(float f) {
        return Double.parseDouble(String.valueOf(f));
    }

    @Override
    protected void onPause() {
        super.onPause();
        myGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myGLView.onResume();
    }
}