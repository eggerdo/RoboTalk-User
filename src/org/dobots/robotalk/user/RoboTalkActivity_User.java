package org.dobots.robotalk.user;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.robotalk.user.ZmqSettings.SettingsChangeListener;
import org.dobots.robotalk.user.control.CommandHandler;
import org.dobots.robotalk.user.control.RemoteControlHelper;
import org.dobots.robotalk.user.control.RoboControl;
import org.dobots.robotalk.user.utility.Utils;
import org.dobots.robotalk.user.utility.gui.ScalableImageView;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class RoboTalkActivity_User extends Activity {

	// menu id
	private static final int SETTINGS_ID 		= 0;
	private static final int REFRESH			= 1;
//	private static final int SCALE_ID 			= 2;
	private static final int ROTATE_LEFT_ID 	= 3;
	private static final int ROTATE_RIGHT_ID	= 4;
	private static final int CAMERA_TOGGLE_ID			= 5;
	
	private static final String TAG = "RoboTalk";

	private static Context CONTEXT;

	// general
	private ProgressDialog m_oConnectingProgressDialog;
	
	private WakeLock m_oWakeLock;
	private ExecutorService m_oExecutor;
	
	// video
	private ScalableImageView m_svVideo;
	private TextView lblFPS;
	
	private boolean m_bSessionStarted = false;

	// control
	private RemoteControlHelper m_oRemoteCtrl;
	private RoboControl m_oControl;
	
	private Camera camera;

	private boolean m_bOverride;
	
	private VideoHandler m_oVideoHandler;
	
	private ZmqHandler m_oZmqHandler;
	
	private boolean m_bDebug = true;
	private CommandHandler m_oCommandHandler;
	
	// flag defines if the received video frame should be scaled to the
	// available image size
//	private boolean m_bScaleReceivedVideo = false;

	// defines by which angle the received video frame should be rotated
	// by default the image received from the camera (and the one sent over
	// zmq is rotated by 90°. thus we have to rotate it back again to 
	// display it normally on the screen
	private int nRotation = -90;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CONTEXT = this;

        setProperties();
        
        m_oZmqHandler = new ZmqHandler(this);
        
        ZmqSettings oSettings = m_oZmqHandler.getSettings();
        oSettings.setSettingsChangeListener(new SettingsChangeListener() {
			
			@Override
			public void onChange() {
				// if the settings change, close and reopen the connections / sockets
				closeConnections();
				setupConnections();
			}

		});
        
        m_oVideoHandler = new VideoHandler(m_oZmqHandler, uiHandler);

    	m_oCommandHandler = new CommandHandler(m_oZmqHandler, uiHandler);
        m_oControl = new RoboControl(m_oCommandHandler);

		m_oRemoteCtrl = new RemoteControlHelper(this, m_oControl);
        m_oRemoteCtrl.setProperties();

        if (oSettings.isValid()) {
            setupConnections();
        }

		PowerManager powerManager =
				(PowerManager)getSystemService(Context.POWER_SERVICE);
		m_oWakeLock =
				powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"Full Wake Lock");

		m_oExecutor = Executors.newCachedThreadPool();
    }

	private void closeConnections() {
		m_oVideoHandler.closeConnections();
		m_oCommandHandler.closeConnections();
	}

	private void setupConnections() {
		m_oVideoHandler.setupConnections();
		m_oCommandHandler.setupConnections();
	}
    
    private void setProperties() {
        
        setContentView(R.layout.main);
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

    	m_svVideo = (ScalableImageView) findViewById(R.id.svVideo);
    	m_svVideo.setClickable(true);
		registerForContextMenu(m_svVideo);
//    	m_svVideo.setScale(true);
//    	m_svVideo.getHolder().addCallback(this);
		
		lblFPS = (TextView) findViewById(R.id.lblFPS);
    }
    
	public static Context getContext() {
		return CONTEXT;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!m_oWakeLock.isHeld()) {
			m_oWakeLock.acquire();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (m_oWakeLock.isHeld()) {
			m_oWakeLock.release();
		}

	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, SETTINGS_ID, 0, "Settings");
		menu.add(0, CAMERA_TOGGLE_ID, 0, "Toggle Camera");
		
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SETTINGS_ID:
    		m_oZmqHandler.getSettings().showDialog();
    		break;
//		case SCALE_ID:
//			m_bScaleReceivedVideo = !m_bScaleReceivedVideo;
//			break;
		case ROTATE_RIGHT_ID:
			nRotation = (nRotation + 90) % 360;
			break;
		case ROTATE_LEFT_ID:
			nRotation = (nRotation - 90) % 360;
			break;
		case CAMERA_TOGGLE_ID:
			toggleCamera();
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.svVideo) {
			menu.setHeaderTitle(String.format("Adjust Video"));
//			menu.add(1, SCALE_ID, SCALE_ID, "Scale Image")
//				.setCheckable(true)
//				.setChecked(m_bScaleReceivedVideo);
			menu.add(1, ROTATE_LEFT_ID, ROTATE_LEFT_ID, "Rotate Left 90°");
			menu.add(1, ROTATE_RIGHT_ID, ROTATE_RIGHT_ID, "Rotate Right 90°");
		}
	}
	
	private void toggleCamera() {
		m_oControl.toggleCamera();
	}

	@Override
    public Dialog onCreateDialog(int id) {
    	return m_oZmqHandler.getSettings().onCreateDialog(id);
    }
    
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		m_oZmqHandler.getSettings().onPrepareDialog(id, dialog);
	}

	private Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case RoboTalkTypes.INCOMING_VIDEO_MSG:
				final Bitmap bmp = (Bitmap) msg.obj;
				
//                Canvas canvas = null;
                try {
//                    canvas = m_svVideo.getHolder().lockCanvas(null);

//                    Matrix matrix = new Matrix();

//                    int dstWidth = m_svVideo.getWidth();
//                    int dstHeight = m_svVideo.getHeight();
                    
//                    if (m_bScaleReceivedVideo) {
//                    	// if the video should be scaled, determine the scaling factors
//                    	int srcWidth = bmp.getWidth();
//                    	int srcHeight = bmp.getHeight();
//                        if ((srcWidth != dstWidth) || (srcHeight != dstHeight)) {
//	                        matrix.postScale((float) dstWidth / srcWidth, (float) dstHeight / srcHeight);
//                        }
//                    }

                    // set the rotation
//                    matrix.postRotate(nRotation, bmp.getWidth()/2, bmp.getHeight()/2);
                    
//                    Paint paint = new Paint();
//                    paint.setColor(Color.BLACK);
                    // clear the canvas
//                    canvas.drawPaint(paint);
                    // then draw the bitmap with the matrix (scale and rotation) to apply
//                    canvas.drawBitmap(bmp, matrix, paint);
                    
//                    final Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
                    
                    Utils.runAsyncUiTask(new Runnable() {
						
						@Override
						public void run() {
		                    m_svVideo.setImageBitmap(bmp);
						}
					});
                } finally
                {
//                    if (canvas != null) {
//                    	m_svVideo.getHolder().unlockCanvasAndPost(canvas);
//                    }
                }
                break;
			case RoboTalkTypes.SET_FPS:
				if (m_bDebug) {
					final int fps_user = (Integer) msg.obj;
					
					// update the user's FPS
					Utils.runAsyncUiTask(new Runnable() {
						
		    			@Override
		    			public void run() {
		    				lblFPS.setText("FPS: " + String.valueOf(fps_user));
		    			}
		            	
		            });
				}
				break;
			}
		}
	};
	
}