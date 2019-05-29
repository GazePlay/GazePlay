package com.github.agomezmoron.multimedia.recorder.listener;

import java.util.EventListener;

public interface VideoRecorderEventListener extends EventListener {
	void frameAdded(VideoRecorderEventObject args);
}
