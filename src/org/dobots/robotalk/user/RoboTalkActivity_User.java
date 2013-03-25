package org.dobots.robotalk.user;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.robotalk.control.CommandHandler;
import org.dobots.robotalk.control.ZmqRemoteControlHelper;
import org.dobots.robotalk.control.ZmqRemoteListener;
import org.dobots.robotalk.control.CommandHandler.CommandListener;
import org.dobots.robotalk.video.VideoDisplayThread;
import org.dobots.robotalk.video.VideoDisplayThread.FPSListener;
import org.dobots.robotalk.video.VideoDisplayThread.VideoListener;
import org.dobots.robotalk.video.VideoHandler;
import org.dobots.robotalk.video.VideoTypes;
import org.dobots.robotalk.zmq.ZmqHandler;
import org.dobots.robotalk.zmq.ZmqSettings;
import org.dobots.robotalk.zmq.ZmqTypes;
import org.dobots.robotalk.zmq.ZmqSettings.SettingsChangeListener;
import org.dobots.utilities.ScalableImageView;
import org.dobots.utilities.Utils;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
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

public class RoboTalkActivity_User extends Activity implements VideoListener, FPSListener {

	// menu id
	private static final int SETTINGS_ID 		= 0;
	private static final int REFRESH			= 1;
//	private static final int SCALE_ID 			= 2;
	private static final int ROTATE_LEFT_ID 	= 3;
	private static final int ROTATE_RIGHT_ID	= 4;
	private static final int CAMERA_TOGGLE_ID	= 5;
	
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
	private ZmqRemoteControlHelper m_oRemoteCtrl;
	
	private Camera camera;

	private boolean m_bOverride;
	
	private VideoHandler m_oVideoHandler;
	
	private ZmqHandler m_oZmqHandler;
	private ZmqSettings m_oSettings;
	
	boolean m_bDebug = true;
	private CommandHandler m_oCmdHandler_External;
	
	// flag defines if the received video frame should be scaled to the
	// available image size
//	private boolean m_bScaleReceivedVideo = false;

	// defines by which angle the received video frame should be rotated
	// by default the image received from the camera (and the one sent over
	// zmq is rotated by 90°. thus we have to rotate it back again to 
	// display it normally on the screen
	private int nRotation = -90;
	private VideoDisplayThread m_oVideoDisplayer;
	private ZmqRemoteListener m_oZmqRemoteListener;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CONTEXT = this;

        setProperties();
        
        m_oZmqHandler = new ZmqHandler(this);
        
        m_oSettings = m_oZmqHandler.getSettings();
        m_oSettings.setSettingsChangeListener(new SettingsChangeListener() {
			
			@Override
			public void onChange() {
				// if the settings change, close and reopen the connections / sockets
				closeConnections();
				setupConnections();
			}

		});
        
        m_oVideoHandler = new VideoHandler(m_oZmqHandler.getContext());

    	m_oCmdHandler_External = new CommandHandler(m_oZmqHandler);

        if (m_oSettings.isValid()) {
            setupConnections();
        }

    	m_oZmqRemoteListener = new ZmqRemoteListener(m_oCmdHandler_External);

		m_oRemoteCtrl = new ZmqRemoteControlHelper(this, null, m_oZmqRemoteListener);
        m_oRemoteCtrl.setProperties();
        m_oRemoteCtrl.setCameraControlListener(m_oZmqRemoteListener);
//        m_oRemoteCtrl.setupCommandConnections();

		PowerManager powerManager =
				(PowerManager)getSystemService(Context.POWER_SERVICE);
		m_oWakeLock =
				powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"Full Wake Lock");

		m_oExecutor = Executors.newCachedThreadPool();
    }

	private void closeConnections() {
		m_oVideoHandler.closeConnections();
		m_oCmdHandler_External.closeConnections();
	}

	private void setupConnections() {
		setupVideoConnection();
		setupCommandConnection();
	}
	
	private void setupCommandConnection() {

//        m_oZmqHandler.setupCommandConnections();
        
		ZMQ.Socket oCommandOut = m_oZmqHandler.createSocket(ZMQ.PUSH);
		
		// obtain command ports from settings
		// receive port is always equal to send port + 1
		int nCommandOutPort = m_oSettings.getCommandPort();
		
		oCommandOut.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nCommandOutPort));
		
		m_oCmdHandler_External.setupConnections(null, oCommandOut);

        m_oCmdHandler_External.setCommandListener(new CommandListener() {
			
			@Override
			public void onCommand(ZMsg i_oMsg) {
				m_oZmqHandler.getCommandHandler().sendZmsg(i_oMsg);
			}
		});        
        
	}
	
	private void setupVideoConnection() {

		ZMQ.Socket oExt_VideoIn = m_oZmqHandler.createSocket(ZMQ.SUB);

		// obtain video ports from settings
		// receive port is always equal to send port + 1
		int nVideoRecvPort = m_oSettings.getVideoPort() + 1;
		
		oExt_VideoIn.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nVideoRecvPort));
		
		// subscribe to the partner's video
		oExt_VideoIn.subscribe("".getBytes());

		m_oVideoHandler.setupConnections(oExt_VideoIn, null);
		
		ZMQ.Socket oInt_VideoIn = m_oZmqHandler.createSocket(ZMQ.SUB);
		oInt_VideoIn.connect(m_oVideoHandler.getIntVideoAddr());
		oInt_VideoIn.subscribe("".getBytes());
		m_oVideoDisplayer = new VideoDisplayThread(m_oZmqHandler.getContext().getContext(), oInt_VideoIn);
		m_oVideoDisplayer.setVideoListner(this);
		m_oVideoDisplayer.setFPSListener(this);
		m_oVideoDisplayer.start();
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
		m_oRemoteCtrl.toggleCamera();
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
			case VideoTypes.INCOMING_VIDEO_MSG:
				
                break;
			case VideoTypes.SET_FPS:
				
				break;
			}
		}
	};

	@Override
	public void onFPS(final int i_nFPS) {
		if (m_bDebug) {
			// update the user's FPS
			Utils.runAsyncUiTask(new Runnable() {
				
    			@Override
    			public void run() {
    				lblFPS.setText("FPS: " + String.valueOf(i_nFPS));
    			}
            	
            });
		}
	}

	@Override
	public void onFrame(final Bitmap i_oBmp) {
		
	//      Canvas canvas = null;
	      try {
	//          canvas = m_svVideo.getHolder().lockCanvas(null);
	
	//          Matrix matrix = new Matrix();
	
	//          int dstWidth = m_svVideo.getWidth();
	//          int dstHeight = m_svVideo.getHeight();
	          
	//          if (m_bScaleReceivedVideo) {
	//          	// if the video should be scaled, determine the scaling factors
	//          	int srcWidth = bmp.getWidth();
	//          	int srcHeight = bmp.getHeight();
	//              if ((srcWidth != dstWidth) || (srcHeight != dstHeight)) {
	//                  matrix.postScale((float) dstWidth / srcWidth, (float) dstHeight / srcHeight);
	//              }
	//          }
	
	          // set the rotation
	//          matrix.postRotate(nRotation, bmp.getWidth()/2, bmp.getHeight()/2);
	          
	//          Paint paint = new Paint();
	//          paint.setColor(Color.BLACK);
	          // clear the canvas
	//          canvas.drawPaint(paint);
	          // then draw the bitmap with the matrix (scale and rotation) to apply
	//          canvas.drawBitmap(bmp, matrix, paint);
	          
	//          final Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
	          
	          Utils.runAsyncUiTask(new Runnable() {
					
					@Override
					public void run() {
	                  m_svVideo.setImageBitmap(i_oBmp);
					}
				});
	      } finally
	      {
	//          if (canvas != null) {
	//          	m_svVideo.getHolder().unlockCanvasAndPost(canvas);
	//          }
	      }
	}
	
}