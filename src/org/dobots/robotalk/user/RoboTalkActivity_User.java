package org.dobots.robotalk.user;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dobots.robotalk.user.ZmqSettings.SettingsChangeListener;
import org.dobots.robotalk.user.control.CommandHandler;
import org.dobots.robotalk.user.control.RemoteControlHelper;
import org.dobots.robotalk.user.control.RoboControl;
import org.dobots.robotalk.user.utility.Utils;
import org.dobots.robotalk.user.utility.gui.ScalableImageView;
import org.zeromq.ZContext;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class RoboTalkActivity_User extends Activity {

	// menu id
	private static final int SETTINGS_ID 		= 0;
	private static final int REFRESH			= 1;
//	private static final int SCALE_ID 			= 2;
//	private static final int ROTATE_LEFT_ID 	= 3;
//	private static final int ROTATE_RIGHT_ID	= 4;
	
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
//		case ROTATE_RIGHT_ID:
//			nRotation = (nRotation + 90) % 360;
//			break;
//		case ROTATE_LEFT_ID:
//			nRotation = (nRotation - 90) % 360;
//			break;
		}
		return true;
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

                    Matrix matrix = new Matrix();

                    int dstWidth = m_svVideo.getWidth();
                    int dstHeight = m_svVideo.getHeight();
                    
//                    if (m_bScaleReceivedVideo) {
//                    	// if the video should be scaled, determine the scaling factors
//                    	int srcWidth = bmp.getWidth();
//                    	int srcHeight = bmp.getHeight();
//                        if ((srcWidth != dstWidth) || (srcHeight != dstHeight)) {
//	                        matrix.preScale((float) dstWidth / srcWidth, (float) dstHeight / srcHeight);
//                        }
//                    }

//                    // set the rotation
//                    matrix.postRotate(nRotation, dstWidth/2, dstHeight/2);
                    
                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    // clear the canvas
//                    canvas.drawPaint(paint);
                    // then draw the bitmap with the matrix (scale and rotation) to apply
//                    canvas.drawBitmap(bmp, matrix, paint);
                    
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