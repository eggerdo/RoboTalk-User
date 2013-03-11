package org.dobots.robotalk.user;

import java.io.ByteArrayInputStream;

import org.dobots.robotalk.user.msg.RobotMessage;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

public class VideoHandler {
	
	private ZContext m_oZContext;
	private ZmqSettings m_oSettings;

	// the channel used to receive the partner's video
	private Socket m_oVideoReceiver = null;
	
	// thread handling the video reception
	private VideoReceiveThread m_oVideoRecvThread;
	
	// handler to display received frames
	private Handler m_oUiHandler;

	private boolean m_bVideoEnabled = true;

    private int m_nFpsCounterPartner = 0;
    private long m_lLastTimePartner = System.currentTimeMillis();

    private boolean m_bDebug;
    
    // on startup don't display the video of anybody, but wait until a partner is selected
    private String m_strRobotName = "nobody";
    
	private boolean m_bConnected;
    
	public VideoHandler(ZmqHandler i_oZmqHandler, Handler i_oUiHandler) {
		m_oZContext = i_oZmqHandler.getContext();
		m_oSettings = i_oZmqHandler.getSettings();
		m_oUiHandler = i_oUiHandler;
	}

	public String getPartner() {
		return m_strRobotName;
	}

	public void setRobotName(String i_strRobotName) {
		// unsubscribe from previous partner
		m_oVideoReceiver.unsubscribe(m_strRobotName.getBytes());
		
		m_strRobotName = i_strRobotName;
		// subscribe to new partner
		m_oVideoReceiver.subscribe("".getBytes());
	}

	class VideoReceiveThread extends Thread {

		public boolean bRun = true;
		
		@Override
		public void run() {
			while(bRun) {
				ZMsg msg = ZMsg.recvMsg(m_oVideoReceiver);
				if (msg != null) {
					// create a video message out of the zmq message
					RobotMessage oVideoMsg = RobotMessage.fromZMsg(msg);
	
					// decode the received frame from jpeg to a bitmap
					ByteArrayInputStream stream = new ByteArrayInputStream(oVideoMsg.data);
					Bitmap bmp = BitmapFactory.decodeStream(stream);
					
					Message uiMsg = m_oUiHandler.obtainMessage();
					uiMsg.what = RoboTalkTypes.INCOMING_VIDEO_MSG;
					uiMsg.obj = bmp;
					m_oUiHandler.dispatchMessage(uiMsg);
				
                    if (m_bDebug) {
	                    ++m_nFpsCounterPartner;
	                    long now = System.currentTimeMillis();
	                    if ((now - m_lLastTimePartner) >= 1000)
	                    {
	        	        	uiMsg = m_oUiHandler.obtainMessage();
	        	        	uiMsg.what = RoboTalkTypes.SET_FPS;
	        	        	uiMsg.obj = m_nFpsCounterPartner;
	    					m_oUiHandler.dispatchMessage(uiMsg);
	        	            
	                        m_lLastTimePartner = now;
	                        m_nFpsCounterPartner = 0;
	                    }
                    }
				}
			}
		}
		
	}

	public void setupConnections() {

		if (m_bVideoEnabled) {
			m_oVideoReceiver = m_oZContext.createSocket(ZMQ.SUB);

			// obtain video ports from settings
			// receive port is always equal to send port + 1
			int nVideoRecvPort = m_oSettings.getVideoPort();
			
			m_oVideoReceiver.connect(String.format("tcp://%s:%d", m_oSettings.getAddress(), nVideoRecvPort));
			
			// subscribe to the partner's video
			m_oVideoReceiver.subscribe("".getBytes());
	
			m_oVideoRecvThread = new VideoReceiveThread();
			m_oVideoRecvThread.start();
			
			m_bConnected = true;
		}
		
	}


	public void closeConnections() {
		
		if (m_oVideoRecvThread != null) {
			m_oVideoRecvThread.bRun = false;
			m_oVideoRecvThread.interrupt();
			m_oVideoRecvThread = null;
		}
		
		if (m_oVideoReceiver != null) {
			m_oVideoReceiver.close();
			m_oVideoReceiver = null;
		}
		
		m_bConnected = false;
	}
	
}
