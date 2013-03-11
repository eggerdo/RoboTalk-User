package org.dobots.robotalk.user.control;

import org.dobots.robotalk.user.control.RemoteControlHelper.Move;
import org.dobots.robotalk.user.msg.RoboCommands;
import org.dobots.robotalk.user.msg.RoboCommands.BaseCommand;
import org.dobots.robotalk.user.msg.RoboCommands.DriveCommand;

import android.util.Log;

public class RoboControl implements RemoteControlListener {
	
	private static final String TAG = "RoboControl";
	
	// if this is sent as speed the controller will chose
	// a predefined base speed for the correspondig move
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
		
		// execute this move
		switch(i_oMove) {
		case NONE:
			sendMoveStop();
			Log.i(TAG, "stop()");
			break;
		case BACKWARD:
			sendMoveBackward(i_dblSpeed, i_dblAngle);
			Log.i(TAG, String.format("bwd(s=%f, a=%f)", i_dblSpeed, i_dblAngle));
			break;
		case STRAIGHT_BACKWARD:
			sendMoveBackward(i_dblSpeed);
			Log.i(TAG, String.format("bwd(s=%f)", i_dblSpeed));
			break;
		case FORWARD:
			sendMoveForward(i_dblSpeed, i_dblAngle);
			Log.i(TAG, String.format("fwd(s=%f, a=%f)", i_dblSpeed, i_dblAngle));
			break;
		case STRAIGHT_FORWARD:
			sendMoveForward(i_dblSpeed);
			Log.i(TAG, String.format("fwd(s=%f)", i_dblSpeed));
			break;
		case LEFT:
			sendRotateCounterClockwise(i_dblSpeed);
			Log.i(TAG, String.format("c cw(s=%f)", i_dblSpeed));
			break;
		case RIGHT:
			sendRotateClockwise(i_dblSpeed);
			Log.i(TAG, String.format("cw(s=%f)", i_dblSpeed));
			break;
		}
	}

	private void sendRotateClockwise(double i_dblSpeed) {
		DriveCommand oCmd = RoboCommands.createDriveCommand(Move.RIGHT, i_dblSpeed);
		sendCommand(oCmd);
	}

	private void sendRotateCounterClockwise(double i_dblSpeed) {
		DriveCommand oCmd = RoboCommands.createDriveCommand(Move.LEFT, i_dblSpeed);
		sendCommand(oCmd);
	}

	private void sendMoveForward(double i_dblSpeed) {
		DriveCommand oCmd = RoboCommands.createDriveCommand(Move.STRAIGHT_FORWARD, i_dblSpeed);
		sendCommand(oCmd);
	}

	private void sendMoveForward(double i_dblSpeed, double i_dblAngle) {
		DriveCommand oCmd = RoboCommands.createDriveCommand(Move.FORWARD, i_dblSpeed, i_dblAngle);
		sendCommand(oCmd);
	}

	private void sendMoveBackward(double i_dblSpeed) {
		DriveCommand oCmd = RoboCommands.createDriveCommand(Move.STRAIGHT_BACKWARD, i_dblSpeed);
		sendCommand(oCmd);
	}

	private void sendMoveBackward(double i_dblSpeed, double i_dblAngle) {
		DriveCommand oCmd = RoboCommands.createDriveCommand(Move.BACKWARD, i_dblSpeed, i_dblAngle);
		sendCommand(oCmd);
	}

	private void sendMoveStop() {
		DriveCommand oCmd = RoboCommands.createDriveCommand(Move.NONE, 0);
		sendCommand(oCmd);
	}

	@Override
	// callback function when the arrow keys are used
	public void onMove(Move i_oMove) {
		// execute this move
		switch(i_oMove) {
		case NONE:
			sendMoveStop();
			Log.i(TAG, "stop()");
			break;
		case STRAIGHT_BACKWARD:
		case BACKWARD:
			sendMoveBackward();
			Log.i(TAG, "bwd()");
			break;
		case STRAIGHT_FORWARD:
		case FORWARD:
			sendMoveForward();
			Log.i(TAG, "fwd()");
			break;
		case LEFT:
			sendRotateCounterClockwise();
			Log.i(TAG, "c cw()");
			break;
		case RIGHT:
			sendRotateClockwise();
			Log.i(TAG, "cw()");
			break;
		}
	}

	private void sendRotateClockwise() {
		sendRotateClockwise(BASE_SPEED);
	}

	private void sendRotateCounterClockwise() {
		sendRotateCounterClockwise(BASE_SPEED);
	}

	private void sendMoveForward() {
		sendMoveForward(BASE_SPEED);
	}

	private void sendMoveBackward() {
		sendMoveBackward(BASE_SPEED);
	}

	@Override
	public void enableControl(boolean i_bEnable) {
		// enabling the control will be handled by the robot
		// controller
	}


}
