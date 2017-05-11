package com.aj.collection.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aj.collection.R;
import com.aj.collection.service.CameraPreview;
import com.aj.collection.tools.MyOrientationDetector;
import com.aj.collection.tools.ScreenUtil;
import com.aj.collection.tools.Util;

/**
 * 拍照类
 *
 * @author Administrator
 */
public class CameraView extends Activity implements OnClickListener {
    private final CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static final String LOG_TAG = "CameraView";
    public static Bitmap cameraBitmap;//用于存储拍完照的图像
    public static final int ORIENTATION_UNKNOWN = -1;
    private Camera mCamera;
    private CameraPreview mPreview;
    private Button takePicture;
    private TextView timeDisplay, gpsDisplay;
    private Intent rIntent;
    public String pName;//图片名称
    private String num;    //样品编号
    private String gps_info;    //gps infomation
    private static String root_path;//任务文件夹路径
    private File file;
    public static Uri fileUri;
    private int view_id;    //点击拍照控件的id
    private boolean hasTakenPhoto = false;


    /**
     * 获取当前的context
     */
    private Context mContext = this;
    /**
     * 当前传感器的方向，当方向发生改变的时候能够自动从传感器管理类接受通知的辅助类
     */
    MyOrientationDetector cameraOrientation;

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //data参数值就是照片数据，将这些数据以key-value形式保存
            //以便其他调用该窗口的程序可以获得照片数据
            //获得图片2560*1920，后续处理须先压缩
            cameraBitmap = mBitmap(size.width, size.height, data);
            System.out.println("压缩后宽和高：" + cameraBitmap.getWidth() + "\n" + cameraBitmap.getHeight());
            Bitmap b = embedWatermark(cameraBitmap);
            byte[] p = Bitmap2Bytes(b);
//        	byte[] p=Bitmap2Bytes(cameraBitmap);
            save_picture(file, p);
            Intent i = new Intent();
            i.putExtra("view_id", view_id);
            i.putExtra("pName", pName);
            i.putExtra("picture_path", file.getPath());
            //返回结果代码
            setResult(SheetActivity.Companion.getREQUESTCODEFORPICTURE(), i);
            //停止照片拍摄
            mCamera.stopPreview();
            mCamera = null;
            b = null;
            //关闭当前窗口
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera_preview);
        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our mContext.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_view);
        preview.addView(mPreview);

        //unsave=(Button)findViewById(R.id.btn_unsavePicture);
        //save=(Button)findViewById(R.id.btn_savePicture);
        takePicture = (Button) findViewById(R.id.btn_takePicture);
        //unsave.setOnClickListener(this);
        //save.setOnClickListener(this);
        takePicture.setOnClickListener(this);

        timeDisplay = (TextView) findViewById(R.id.tv_timeDisplay);
        long time = System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        String t1 = format.format(d1);
        timeDisplay.setText(t1);
        gpsDisplay = (TextView) findViewById(R.id.tv_GPSDisplay);

        //接收意图里的信息
        rIntent = getIntent();
        view_id = rIntent.getIntExtra("view_id", 0);
        root_path = rIntent.getStringExtra("mediaRootPath");
        num = rIntent.getStringExtra("number");
        gps_info = rIntent.getStringExtra("location");
        gpsDisplay.setText(gps_info);
//        图片名称
        pName = "CAMERA" + "_" + ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() + Util.getCurrentTime("yyMMddHHmmss") + ".jpg";
//        图片文件全路径名称
        file = getOutputMediaFile(pName, root_path);
        System.out.println("***********file" + "\n" + file);
        fileUri = getOutputMediaFileUri(pName, root_path);
        Log.d(LOG_TAG, "fileUri is " + fileUri.getPath());


    }

    Size size = null;

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            Parameters parameters = c.getParameters();
            parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);//设置闪光点
            parameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);//设置白平衡
            parameters.setPreviewFpsRange(10, 60);


            List<Size> pictureSizeSet = parameters.getSupportedPictureSizes();
            for (Size s :
                    pictureSizeSet) {
                if (s.width < s.height && s.width > 1000 && s.width < 1500) {
                    size = s;
                    break;
                }else if (s.width > s.height && s.height > 1000 && s.height < 1500){
                    size = s;
                    break;
                }
            }
            if (size == null && pictureSizeSet.size() != 0) {
                size = pictureSizeSet.get(pictureSizeSet.size() - 1);
            }
            Size pictureSize = size;
            //预览大小
            Size previewSize = size;
            if (previewSize != null) {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            } else {
                Log.e("拍照调试", "prieview size is null");
                parameters.setPreviewSize(1280, 720);    //设置薄片的预览尺寸
            }

            if (pictureSize != null) {
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
            } else {
                Log.e("拍照调试", "picture size is null");
                parameters.setPictureSize(1280, 720);    //设置照片的尺寸
            }

            Log.e("拍照调试", "picture size -- width:" + pictureSize.width + "\theight:" + pictureSize.height);
            System.out.println("picture size -- width:" + pictureSize.width + "\theight:" + pictureSize.height);
            parameters.setJpegQuality(70);
            c.setParameters(parameters);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onClick(View v) {

        if (v == takePicture) {
            takePicture();
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mCamera != null) {
            try {
                mCamera.autoFocus(new AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
//						mCamera.takePicture(null, null, mPicture);
                        if (hasTakenPhoto == true)
                            return;

                        takePhoto();
                    }
                });
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                e.printStackTrace();
            }
        }
    }

    /**
     * 调整照相的方向，设置拍照相片的方向
     */
    private void takePhoto() {
        cameraOrientation = new MyOrientationDetector(mContext);
        if (mCamera != null) {
            int orientation = cameraOrientation.getOrientation();
            Camera.Parameters cameraParameter = mCamera.getParameters();
            cameraParameter.setRotation(90);
            cameraParameter.set("rotation", 90);
            if ((orientation >= 45) && (orientation < 135)) {
                cameraParameter.setRotation(180);
                cameraParameter.set("rotation", 180);
            }
            if ((orientation >= 135) && (orientation < 225)) {
                cameraParameter.setRotation(270);
                cameraParameter.set("rotation", 270);
            }
            if ((orientation >= 225) && (orientation < 315)) {
                cameraParameter.setRotation(0);
                cameraParameter.set("rotation", 0);
            }
            mCamera.setParameters(cameraParameter);
            mCamera.takePicture(null, null, mPicture);

            hasTakenPhoto = true;
        }
    }


//	/**
//	 * 用图片工厂处理一下
//	 * @param width
//	 * @param height
//	 * @param fileUri
//	 * @return
//	 */
//	public Bitmap mBitmap(int width,int height, Uri fileUri)
//	{
//		
//
//    	BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
//    	
//    	factoryOptions.inJustDecodeBounds = true;
//    	BitmapFactory.decodeFile(fileUri.getPath(),
//    			factoryOptions);
//    	int imageWidth = factoryOptions.outWidth;
//    	int imageHeight = factoryOptions.outHeight;
//    	
//    	// Determine how much to scale down the image
//    	int scaleFactor = Math.min(imageWidth / width, imageHeight
//    			/ height); 
//    	
//    	// Decode the image file into a Bitmap sized to fill the
//    	// View
//    	factoryOptions.inJustDecodeBounds = false;
//    	factoryOptions.inSampleSize = scaleFactor;
//    	factoryOptions.inPurgeable = true;
//    	
//    	Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
//    			factoryOptions);
//    	return bitmap;
//	}
//	public Bitmap mBitmap(int width,int height, String str)
//	{
//		
//		
//		BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
//		
//		factoryOptions.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(str,
//				factoryOptions);
//		int imageWidth = factoryOptions.outWidth;
//		int imageHeight = factoryOptions.outHeight;
//		
//		// Determine how much to scale down the image
//		int scaleFactor = Math.min(imageWidth / width, imageHeight
//				/ height); 
//		
//		// Decode the image file into a Bitmap sized to fill the
//		// View
//		factoryOptions.inJustDecodeBounds = false;
//		factoryOptions.inSampleSize = scaleFactor;
//		factoryOptions.inPurgeable = true;
//		
//		Bitmap bitmap = BitmapFactory.decodeFile(str,
//				factoryOptions);
//		return bitmap;
//	}

    /**
     * 缩放图片比例最好是4:3
     *
     * @param width  缩放后的图片宽度
     * @param height 缩放后的图片高度
     * @param data   原图
     * @return bitmap 新图
     */
    public Bitmap mBitmap(int width, int height, byte[] data) {

//		BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
//		
//		//图片不加载到内存中
//		factoryOptions.inJustDecodeBounds = true;
//		BitmapFactory.decodeByteArray(data, 0, data.length,factoryOptions);
//		int imageWidth = factoryOptions.outWidth;
//		int imageHeight = factoryOptions.outHeight;
//		Log.d(LOG_TAG, "factory输出的宽和高"+imageWidth+"\n"+imageHeight);
//		// Determine how much to scale down the image
//		int scaleFactor = Math.min(imageWidth / width, imageHeight
//				/ height); 
//		
//		// Decode the image file into a Bitmap sized to fill the
//		// View
//		factoryOptions.inJustDecodeBounds = false;
//		factoryOptions.inSampleSize = scaleFactor;
//		Log.d(LOG_TAG, "压缩比例"+factoryOptions.inSampleSize);
//		factoryOptions.inPurgeable = true;
//		
//		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,factoryOptions);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap.getWidth() > bitmap.getHeight() && width > height)
            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        else if (bitmap.getWidth() < bitmap.getHeight() && width > height)
            return Bitmap.createScaledBitmap(bitmap, height, width, true);
        else if (bitmap.getWidth() < bitmap.getHeight() && width < height)
            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        else if (bitmap.getWidth() > bitmap.getHeight() && width < height)
            return Bitmap.createScaledBitmap(bitmap, height, width, true);
        else
            return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static Uri getOutputMediaFileUri(String name, String filePath) {
        return Uri.fromFile(getOutputMediaFile(name, filePath));
    }

    public static File getOutputMediaFile(String name, String filePath) {
        File mediaStorageDir = null;
        try {
            //mediaStorageDir=new File(Environment.getExternalStoragePublicDirectory("/my阿杰"),"myPicture");
            mediaStorageDir = new File(root_path);
            System.out.println("Successfully created mediaStorageDir: " + mediaStorageDir);
            Log.d(LOG_TAG, "Successfully created mediaStorageDir: "
                    + mediaStorageDir);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in Creating mediaStorageDir: " + mediaStorageDir);
            Log.d(LOG_TAG, "Error in Creating mediaStorageDir: "
                    + mediaStorageDir);
        }
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_TAG,
                        "failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + name);
        return mediaFile;
    }

    //保存照片
    private void save_picture(File filePath, byte[] data) {
        try {
            //保存图片
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //bitmap to byte[]
    private byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 绘制水印信息
     *
     * @param src
     * @return
     */
    private Bitmap embedWatermark(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        String mstrTime = timeDisplay.getText().toString();
        String mstrGps = gpsDisplay.getText().toString();
        String number = num;
        Bitmap bmpTemp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpTemp);
        Paint p = new Paint();
        String familyName = "宋体";
        Typeface font = Typeface.create(familyName, Typeface.BOLD);
        p.setColor(Color.WHITE);
        p.setTypeface(font);
        p.setTextSize(ScreenUtil.spToPx(this, 13f));
        canvas.drawBitmap(src, 0, 0, p);
        canvas.translate(20, 20);//从20，20开始画
//        canvas.drawText(mstrTime, 0, 20, p);
//        canvas.drawText(mstrGps, 0, 60, p);

        TextPaint textPaint = new TextPaint();
//		textPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
        textPaint.setColor(getResources().getColor(R.color.camera_text_color));
        textPaint.setTextSize(ScreenUtil.spToPx(this, 13f));
        textPaint.setTypeface(font);
        textPaint.setAntiAlias(true);
        StaticLayout layout = new StaticLayout(mstrTime + "\n" + mstrGps + "\n" + number, textPaint, ScreenUtil.getScreenWidth(this),
                Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        layout.draw(canvas);

//		canvas.drawText("样品编号："+number, 0, 100, p);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bmpTemp;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
//		mLocationClient.unRegisterLocationListener(myListener);	//取消监听函数
//		mLocationClient.stop();
    }

    public Size getPreviewSize(List<Camera.Size> list, int th) {
        Collections.sort(list, sizeComparator);
        Size size = null;
        for (int i = 0; i < list.size(); i++) {
            size = list.get(i);
            if ((size.width > th) && equalRate(size, 1.5f)) {
                break;
            }
        }
        return size;
    }

    public Size getPictureSize(List<Camera.Size> list, int th) {
        Collections.sort(list, sizeComparator);
        Size size = null;
        for (int i = 0; i < list.size(); i++) {
            size = list.get(i);
            if ((size.width > th) && equalRate(size, 1.5f)) {
                break;
            }
        }
        return size;

    }

    public boolean equalRate(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.3) {
            return true;
        } else {
            return false;
        }
    }

    public class CameraSizeComparator implements Comparator<Camera.Size> {
        //按升序排列
        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }
}
