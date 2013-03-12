package org.dobots.robotalk.user.control;

import org.dobots.robotalk.user.R;
import org.dobots.robotalk.user.R.id;
import org.dobots.robotalk.user.utility.LockableScrollView;
import org.dobots.robotalk.user.utility.Utils;
import org.dobots.robotalk.user.utility.joystick.Joystick;
import org.dobots.robotalk.user.utility.joystick.JoystickListener;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class RemoteControlHelper implements JoystickListener {

	private static final String TAG = "RemoteControlHelper";
	
	public enum Move {
		NONE, STRAIGHT_FORWARD, FORWARD, STRAIGHT_BACKWARD, BACKWARD, LEFT, RIGHT
	}
	
	private RemoteControlListener m_oRemoteControlListener;
	
	private Move lastMove = Move.NONE;

	private long lastTime = SystemClock.uptimeMillis();
	private double updateFrequency = 5.0; // Hz
	private int threshold = 20;
	
	private Activity m_oActivity;
	
	private boolean m_bControl = true;
	private boolean m_bAdvancedControl = false;

//	private ToggleButton m_btnControl;
	private Button m_btnFwd;
	private Button m_btnBwd;
	private Button m_btnLeft;
	private Button m_btnRight;

	private LockableScrollView m_oScrollView;
	private LinearLayout m_oAdvancedControl;
	
	private Joystick m_oJoystick;
	
	// At least one of the parameters i_oRobot or i_oListener has to be assigned! the other can be null.
	// It is also possible to assign both
	public RemoteControlHelper(Activity i_oActivity, RemoteControlListener i_oListener) {
		this.m_oActivity = i_oActivity;
		m_oRemoteControlListener = i_oListener;
	}
	
	public void setRemoteControlListener(RemoteControlListener i_oListener) {
		m_oRemoteControlListener = i_oListener;
	}
	
	public void removeRemoteControlListener(RemoteControlListener i_oListener) {
		if (m_oRemoteControlListener == i_oListener) {
			m_oRemoteControlListener = null;
		}
	}
	
	public void setProperties() {

		m_oScrollView = (LockableScrollView) m_oActivity.findViewById(R.id.scrollview);
		
		m_oAdvancedControl = (LinearLayout) m_oActivity.findViewById(R.id.layAdvancedControl);
		
		m_oJoystick = (Joystick) m_oActivity.findViewById(R.id.oJoystick);
		m_oJoystick.setUpdateListener(this);

//		m_btnControl = (ToggleButton) m_oActivity.findViewById(R.id.btnRemoteControl);
////		m_btnControl.setText("Remote Control: OFF");
//		m_btnControl.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				m_bControl = !m_bControl;
//				if (m_oRemoteControlListener != null) {
//					m_oRemoteControlListener.enableControl(m_bControl);
//				}
//				showControlButtons(m_bControl);
////				((Button)v).setText("Remote Control: " + (m_bControl ? "ON" : "OFF"));
//			}
//		});
	
		m_btnFwd = (Button) m_oActivity.findViewById(R.id.btnFwd);
		m_btnLeft = (Button) m_oActivity.findViewById(R.id.btnLeft);
		m_btnBwd = (Button) m_oActivity.findViewById(R.id.btnBwd);
		m_btnRight = (Button) m_oActivity.findViewById(R.id.btnRight);
		
		m_btnFwd.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (m_oRemoteControlListener != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						m_oRemoteControlListener.onMove(Move.NONE);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						m_oRemoteControlListener.onMove(Move.STRAIGHT_FORWARD);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						break;					
					case MotionEvent.ACTION_MOVE:
						break;
					}
				}
				return true;
			}
		});
		
		m_btnBwd.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (m_oRemoteControlListener != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						m_oRemoteControlListener.onMove(Move.NONE);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						m_oRemoteControlListener.onMove(Move.STRAIGHT_BACKWARD);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						break;					
					case MotionEvent.ACTION_MOVE:
						break;
					}
				}
				return true;
			}
		});

		m_btnLeft.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (m_oRemoteControlListener != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						m_oRemoteControlListener.onMove(Move.NONE);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						m_oRemoteControlListener.onMove(Move.LEFT);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						break;					
					case MotionEvent.ACTION_MOVE:
						break;
					}
				}
				return true;
			}
		});

		m_btnRight.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (m_oRemoteControlListener != null) {
					int action = e.getAction();
					switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						m_oRemoteControlListener.onMove(Move.NONE);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						break;
					case MotionEvent.ACTION_DOWN:
						m_oRemoteControlListener.onMove(Move.RIGHT);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						break;					
					case MotionEvent.ACTION_MOVE:
						break;
					}
				}
				return true;
			}
		});
		
//		updateButtons(false);
		showControlButtons(true);
	}
	
	public void showControlButtons(boolean visible) {
		if (!visible) {
			Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
			if (m_bAdvancedControl) {
				Utils.showLayout(m_oAdvancedControl, visible);
			} 
		} else {
			Utils.showLayout((LinearLayout)m_oActivity.findViewById(R.id.layRemoteControl), visible);
			if (m_bAdvancedControl) {
				Utils.showLayout(m_oAdvancedControl, visible);
			} 
		}
	}
	
	public void resetLayout() {
//		m_btnControl.setChecked(false);
//		m_bControl = false;
//		updateButtons(false);
//		showControlButtons(false);
	}

//	public void updateButtons(boolean enabled) {
//		m_btnControl.setEnabled(enabled);
//	}
	
	@Override
	public void onJoystickTouch(boolean start) {
		if (m_oScrollView != null) {
			if (start) {
				m_oScrollView.setScrollingEnabled(false);
			} else {
				m_oScrollView.setScrollingEnabled(true);
			}
		} else {
			Log.e(TAG, "scroll view not lockable!");
		}
	}

	final static int ROTATE_THRESHOLD = 40;
	final static int STRAIGHT_THRESHOLD = 2;
	final static int DIRECTION_THRESHOLD_1 = 10;
	final static int DIRECTION_THRESHOLD_2 = 30;
	
	@Override
	public void onUpdate(double i_dblPercentage, double i_dblAngle) {
		
		if (i_dblPercentage == 0 && i_dblAngle == 0) {
			// if percentage and angle is 0 this means the joystick was released
			// so we stop the robot
			m_oRemoteControlListener.onMove(Move.NONE, 0, 0);
			lastMove = Move.NONE;
		} else {

			// only allow a rate of updateFrequency to send drive commands
			// otherwise it will overload the robot's command queue
			if ((SystemClock.uptimeMillis() - lastTime) < 1/updateFrequency * 1000)
				return;

			lastTime = SystemClock.uptimeMillis();
			double dblAbsAngle = Math.abs(i_dblAngle);
			
			// determine which move should be executed based on the
			// last move and the angle of the joystick
			Move thisMove = Move.NONE;
			switch(lastMove) {
			case NONE:
				// for a low percentage (close to the center of the joystick) the
				// angle is too sensitive, so we only start once the percentage
				// is over the threshold
				if (i_dblPercentage < threshold) {
					return;
				}
			case LEFT:
			case RIGHT:
				// if the last move was left (or right respectively) we use a window
				// of +- 30 degrees, otherwise we switch to moving forward or backward
				// depending on the angle
				if (dblAbsAngle < ROTATE_THRESHOLD) {
					thisMove = Move.RIGHT;
				} else if ((180 - dblAbsAngle) < ROTATE_THRESHOLD) {
					thisMove = Move.LEFT;
				} else if (i_dblAngle > 0) {
					thisMove = Move.FORWARD;
				} else if (i_dblAngle < 0) {
					thisMove = Move.BACKWARD;
				}
				break;
			case STRAIGHT_BACKWARD:
			case BACKWARD:
				// if the last move was backward and the angle is within
				// 10 degrees of 0 or 180 degrees we still move backward
				// and cap the degree to 0 or 180 respectively
				// if the angle is within 30 degree we rotate on the spot
				// otherwise we change direction
				if (Utils.inInterval(i_dblAngle, -90, STRAIGHT_THRESHOLD)) {
					thisMove = Move.STRAIGHT_BACKWARD;
				} else if (i_dblAngle < 0) {
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle < DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 0;
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle > 180 - DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 180;
					thisMove = Move.BACKWARD;
				} else if (i_dblAngle < DIRECTION_THRESHOLD_2) {
					thisMove = Move.RIGHT;
				} else if (i_dblAngle > 180 - DIRECTION_THRESHOLD_2) {
					thisMove = Move.LEFT;
				} else {
					thisMove = Move.FORWARD;
				}
				break;
			case STRAIGHT_FORWARD:
			case FORWARD:
				// if the last move was forward and the angle is within
				// 10 degrees of 0 or 180 degrees we still move forward
				// and cap the degree to 0 or 180 respectively
				// if the angle is within 30 degree we rotate on the spot
				// otherwise we change direction
				if (Utils.inInterval(i_dblAngle, 90, STRAIGHT_THRESHOLD)) {
					thisMove = Move.STRAIGHT_FORWARD;
				} else if (i_dblAngle > 0) {
					thisMove = Move.FORWARD;
				} else if (i_dblAngle > -DIRECTION_THRESHOLD_1) {
					dblAbsAngle = 0;
					thisMove = Move.FORWARD;
				} else if (i_dblAngle < -(180 - DIRECTION_THRESHOLD_1)) {
					dblAbsAngle = 180;
					thisMove = Move.FORWARD;
				} else if (i_dblAngle > -DIRECTION_THRESHOLD_2) {
					thisMove = Move.RIGHT;
				} else if (i_dblAngle < -(180 - DIRECTION_THRESHOLD_2)) {
					thisMove = Move.LEFT;
				} else {
					thisMove = Move.BACKWARD;
				}
				break;
			}
			
			m_oRemoteControlListener.onMove(thisMove, i_dblPercentage, dblAbsAngle);
			lastMove = thisMove;
		}
	}

	public boolean isControlEnabled() {
		return m_bControl;
	}

	public void toggleAdvancedControl() {
		m_bAdvancedControl = !m_bAdvancedControl;
	}
	
	public void setAdvancedControl(boolean i_bAdvancedControl) {
		m_bAdvancedControl = i_bAdvancedControl;
		Utils.showLayout(m_oAdvancedControl, m_bAdvancedControl);
	}

	public boolean isAdvancedControl() {
		return m_bAdvancedControl;
	}
		
}
