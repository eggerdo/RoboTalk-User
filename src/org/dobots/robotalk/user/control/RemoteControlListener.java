package org.dobots.robotalk.user.control;

import org.dobots.robotalk.user.control.RemoteControlHelper.Move;

public interface RemoteControlListener {
	
	void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle);
	
	void onMove(Move i_oMove);
	
	void enableControl(boolean i_bEnable);
	
}
