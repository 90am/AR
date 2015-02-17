package com.mygdx.milstone3;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.mygdx.milstone3.UtilAR;


public class milestone3 extends ApplicationAdapter {
	public Mat cameraFrame;
	public VideoCapture camera;
	
	@Override
	public void create () {
		camera = new VideoCapture(0);
		camera.open(0);
		cameraFrame = new Mat();
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		if (camera.read(cameraFrame)) {			
			UtilAR.imDrawBackground(cameraFrame);
		}
	}
}
