package com.mygdx.milstone3;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.milstone3.UtilAR;


public class milestone3 extends ApplicationAdapter {
	public Mat cameraFrame;
	public Mat grayImage;
	public Mat binaryImage;
	public Mat blur;
	public VideoCapture camera;
	public List<MatOfPoint> contours;
	public Mat hierarchy;
	
	@Override
	public void create () {
		camera = new VideoCapture(0);
		camera.open(0);
		cameraFrame = new Mat();
		grayImage = new Mat();
		binaryImage = new Mat();
		blur = new Mat();
		hierarchy = new Mat();
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		if (camera.read(cameraFrame)) {
			Imgproc.cvtColor(cameraFrame, grayImage, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(grayImage, blur, new Size(5,5), 0);
			//Imgproc.adaptiveThreshold(blur, binaryImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);			
			Imgproc.threshold(blur, binaryImage, -1, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
			contours = new ArrayList<MatOfPoint>();
			
			Imgproc.findContours(binaryImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
			System.out.println(contours.size());
			Imgproc.drawContours(cameraFrame, contours, contours.size()-1, new Scalar(245, 7, 253));
			UtilAR.imDrawBackground(cameraFrame);
		}	
	}
}
