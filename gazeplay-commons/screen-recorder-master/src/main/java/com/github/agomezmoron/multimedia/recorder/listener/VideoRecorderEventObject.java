package com.github.agomezmoron.multimedia.recorder.listener;

import java.util.EventObject;

import com.github.agomezmoron.multimedia.capture.ScreenCapture;

public class VideoRecorderEventObject extends EventObject {
	
	private static final long serialVersionUID = 7841721360521723629L;
	private ScreenCapture screenCapture;
	
	public ScreenCapture getScreenCapture(){
		return this.screenCapture;
	}
	
	public VideoRecorderEventObject(Object source, ScreenCapture screenCapture) {
		super(source);
		this.screenCapture = screenCapture;
	}

}
