package org.dobots.robotalk.user.control;

import org.dobots.robotalk.user.control.RemoteControlHelper.Move;
import org.dobots.robotalk.user.msg.RoboCommands;
import org.dobots.robotalk.user.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.user.msg.RoboCommands.CameraCommand;
import org.dobots.robotalk.user.msg.RoboCommands.CameraCommandType;
import org.dobots.robotalk.user.msg.RoboCommands.DriveCommand;

import android.util.Log;

public class RoboControl implements RemoteControlListener {
	
	private static final String TAG = "RoboControl";
	
	// if this is sent as speed the controller will chose
	// a predefined base speed for the corresponding move
	private static final int BASE_SPEED = -1;
	
	private CommandHandler m_oCmdHandler;

	public RoboControl(CommandHandler i_oHandler) {
		m_oCmdHandler = i_oHandler;
	}
	
	private void sendCommand(BaseCommand i_oCmd) {
		m_oCmdHandler.sendCommand(i_oCmd);
	}

	@Override
	// callback function when the joystick is used
	public void onMove(Move i_oMove, double i_dblSpeed, double i_dblAngle) {
		// modify the angle so that it is between -90 and +90
		// instead of 0 and 180
		// where -90 is left and +90 is right
		i_dblAngle -= 90.0;
		
		sendMove(i_oMove, i_dblSpeed, i_dblAngle);
	}
	
	private void sendMove(Move i_eMove, double i_dblSpeed, double i_dblAngle) {
		DriveCommand oCmd = RoboCommands.createDriveCommand(i_eMove, i_dblSpeed, i_dblAngle);
		sendCommand(oCmd);
	}

	@Override
	// callback function when the arrow keys are used
	public void onMove(Move i_oMove) {
		// execute this move
		sendMove(i_oMove, BASE_SPEED, 0.0);
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// enabling the control will be handled by the robot
		// controller
	}

	public void toggleCamera() {
		CameraCommand oCmd = RoboCommands.createCameraCommand(CameraCommandType.cameraToggle);
		sendCommand(oCmd);
	}
	
	public void switchCameraOn() {
		CameraCommand oCmd = RoboCommands.createCameraCommand(CameraCommandType.cameraOn);
		sendCommand(oCmd);
	}

	public void switchCameraOff() {
		CameraCommand oCmd = RoboCommands.createCameraCommand(CameraCommandType.cameraOff);
		sendCommand(oCmd);
	}

}
