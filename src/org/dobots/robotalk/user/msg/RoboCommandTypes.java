package org.dobots.robotalk.user.msg;


public class RoboCommandTypes {

	public static final int DRIVE_COMMAND 	= 0xab;
	public static final int CAMERA_COMMAND	= 0xac;
	public static final int CONTROL_COMMAND = 0xad;
	
	public static final int CAMERA_ROBOT 	= 0;
	public static final int CAMERA_PHONE	= 1;
	public static final int CAMERA_ROOM		= 2;
	
	// JSON field names
	// S_ are names defining a section
	// F_ are names defining a field
	
	/// HEADER
	public static final String S_HEADER 	= "header";
	public static final String F_HEADER_ID	= "id";
	public static final String F_TID		= "tid";
	public static final String F_TIMESTAMP	= "timestamp";
	
	/// DATA
	public static final String S_DATA		= "data";
	
	/// drive command data fields
	public static final String F_MOVE		= "move";
	public static final String F_SPEED 		= "speed";
	public static final String F_ANGLE		= "angle";

	/// camera command data fields
	public static final String F_TYPE	 	= "type";
	
	// control command data fields
	public static final String F_REQUEST	= "request";

	
	public enum Request {
		DRIVE, CAMERA
	}


	
}
