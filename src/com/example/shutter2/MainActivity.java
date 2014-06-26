package com.example.shutter2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MainActivity extends Activity {

	private static final String TAG = "ExampleSportsActivity";
	static long axis;
	private PebbleKit.PebbleDataReceiver dataHandler = null;
	 private final static UUID PEBBLE_APP_UUID = UUID.fromString("332ebb0b-e476-4a24-a8c3-91fb37d7fe7f");

		public static final int MEDIA_TYPE_IMAGE = 1;
		public static final int MEDIA_TYPE_VIDEO = 2;

		/** Create a file Uri for saving an image or video */
		private static Uri getOutputMediaFileUri(int type){
		      return Uri.fromFile(getOutputMediaFile(type));
		}

		/** Create a File for saving an image or video */
		private static File getOutputMediaFile(int type){
		    // To be safe, you should check that the SDCard is mounted
		    // using Environment.getExternalStorageState() before doing this.

		    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
		              Environment.DIRECTORY_PICTURES), "MyCameraApp");
		    // This location works best if you want the created images to be shared
		    // between applications and persist after your app has been uninstalled.

		    // Create the storage directory if it does not exist
		    if (! mediaStorageDir.exists()){
		        if (! mediaStorageDir.mkdirs()){
		            Log.d("MyCameraApp", "failed to create directory");
		            return null;
		        }
		    }

		    // Create a media file name
		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		    File mediaFile;
		    if (type == MEDIA_TYPE_IMAGE){
		        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
		        "IMG_"+ timeStamp + ".jpg");
		    } else if(type == MEDIA_TYPE_VIDEO) {
		        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
		        "VID_"+ timeStamp + ".mp4");
		    } else {
		        return null;
		    }

		    return mediaFile;
		}
		
	    private Camera mCamera;
	    private CameraPreview mPreview;
	    private PictureCallback mPicture = new PictureCallback() {

	        @Override
	        public void onPictureTaken(byte[] data, Camera camera) {

	            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	            if (pictureFile == null){
	                Log.d("TAG", "Error creating media file, check storage permissions");
	                return;
	            }

	            try {
	                FileOutputStream fos = new FileOutputStream(pictureFile);
	                fos.write(data);
	                fos.close();
	            } catch (FileNotFoundException e) {
	                Log.d("TAG", "File not found: " + e.getMessage());
	            } catch (IOException e) {
	                Log.d("TAG", "Error accessing file: " + e.getMessage());
	            }
	        }
	    };
	    
	    /** A safe way to get an instance of the Camera object. */
	    public static Camera getCameraInstance(){
	        Camera c = null;
	        try {
	            c = Camera.open(); // attempt to get a Camera instance
	        }
	        catch (Exception e){
	            // Camera is not available (in use or does not exist)
	        }
	        return c; // returns null if camera is unavailable
	    }
	    
	    public static void setCameraDisplayOrientation(Activity activity,
	            int cameraId, Camera camera) {
	        android.hardware.Camera.CameraInfo info =
	                new android.hardware.Camera.CameraInfo();
	        android.hardware.Camera.getCameraInfo(cameraId, info);
	        int rotation = activity.getWindowManager().getDefaultDisplay()
	                .getRotation();
	        int degrees = 0;
	        switch (rotation) {
	            case Surface.ROTATION_0: degrees = 0; break;
	            case Surface.ROTATION_90: degrees = 90; break;
	            case Surface.ROTATION_180: degrees = 180; break;
	            case Surface.ROTATION_270: degrees = 270; break;
	        }

	        int result;
	        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	            result = (info.orientation + degrees) % 360;
	            result = (360 - result) % 360;  // compensate the mirror
	        } else {  // back-facing
	            result = (info.orientation - degrees + 360) % 360;
	        }
	        camera.setDisplayOrientation(result);
	    }
	    
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
							 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		PebbleKit.registerPebbleConnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) {
			    Log.i(getLocalClassName(), "Pebble connected!");
			  }
			});

			PebbleKit.registerPebbleDisconnectedReceiver(getApplicationContext(), new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) {
			    Log.i(getLocalClassName(), "Pebble disconnected!");
			  }
			});

	}

	@Override
    protected void onPause() {
        super.onPause();
        // Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
        if (dataHandler != null) {
            unregisterReceiver(dataHandler);
            dataHandler = null;
        }
    }
	
	 @Override
	    protected void onResume() {
	        super.onResume();
	        final Handler handler = new Handler();
	        final int REQUEST_IMAGE_CAPTURE = 1;
	        final int CAMERA_REQUEST = 1888;
	        final int TAKE_PHOTO_CODE = 0;
	        dataHandler = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
	            @Override
	            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {

	                PebbleKit.sendAckToPebble(context, transactionId);

	                handler.post(new Runnable() {
	                    @Override
	                    public void run() {
	                        //updateUi();
	                    	
	                    	axis = data.getUnsignedInteger(1);
	                    	Log.w("axis", ""+axis);
	                    	//0 = x
	                    	//1 = y
	                    	//2 = z
	                    	
	                    	if(axis == 0) {
	                    		/*IntentService intent = new IntentService("android.media.action.IMAGE_CAPTURE");
	                    		startService(intent);*/
	                    		
//	                    		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//	                    	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//	                    	        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//	                    	    }
	                    		
	                    		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
	                            startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);

/*	                            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	                            preview.addView(mPreview);
	                            mCamera.takePicture(null, null, mPicture);*/
	                    		
	                    	} else if(axis == 1) {
/*	                    		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	                    		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
	                    	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
	                    	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);*/
	                    		Intent i = new Intent(Intent.ACTION_VIEW);  
	                    		// Android 2.2+  
	                    		i.setData(Uri.parse("content://com.android.calendar/time"));
	                    		startActivity(i);
	                    	} else if(axis == 2){
	                    		Intent i = new Intent();
	                    		i.setClassName("com.android.calculator2",
	                    		               "com.android.calculator2.Calculator");
	                    		startActivity(i); 
	                    	}
	                    }
	                });
	            }
	        };
	        PebbleKit.registerReceivedDataHandler(this, dataHandler);
	    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
