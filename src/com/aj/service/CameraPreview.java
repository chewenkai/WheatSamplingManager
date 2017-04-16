package com.aj.service;
import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
/**
 * 用于承载影像视图的控件,预览类
 * @author Administrator
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
	private static final String TAG = "IOException";
	private SurfaceHolder mHolder;
	private Camera mCamera;
	@SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera)
	{
		super(context);
		// TODO Auto-generated constructor stub
		mCamera=camera;
		// Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
		mHolder=getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		// The Surface has been created, now tell the camera where to draw the preview.
		try
		{//TODO 此处偶尔会有问题 但是还不确定问题所在
			//设置预览角度
			mCamera.setDisplayOrientation(90);
			//Sets the Surface to be used for live preview
			mCamera.setPreviewDisplay(holder);
			//Starts capturing and drawing preview frames to the screen.
			mCamera.startPreview();
		}
		//IOException if the method fails (for example, if the surface
		//	          is unavailable or unsuitable).
		catch (Exception e)
		{
			// TODO Auto-generated catch block
//			mCamera.release();
//			mCamera=null;
			e.printStackTrace();
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{
		// If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
		if (mHolder.getSurface() == null)
		{
	         // preview surface does not exist
	         return;
	    }

	    // stop preview before making changes
	    try 
	    {
	        mCamera.stopPreview();
	    } catch (Exception e)
	    {
	    	// ignore: tried to stop a non-existent preview
	    }

	    // set preview size and make any resize, rotate or
	    // reformatting changes here

	    // start preview with new settings
	    try 
	    {
	        mCamera.setPreviewDisplay(mHolder);
	        mCamera.startPreview();
	    } 
	    catch (Exception e){
	            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
	    }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// Take care of releasing the Camera preview in your activity.
		mCamera.release();
	}
}

