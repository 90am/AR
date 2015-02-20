package com.mygdx.milstone3;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
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
	public Mat binaryImage2;
	public Mat blur;
	public VideoCapture camera;
	public List<MatOfPoint> contours;
	public Mat hierarchy;
	public MatOfPoint2f polygon;
	public List<MatOfPoint> rects;
	
	@Override
	public void create () {
		camera = new VideoCapture(0);
		camera.open(0);
		cameraFrame = new Mat();
		grayImage = new Mat();
		binaryImage = new Mat();
		binaryImage2 = new Mat();
		blur = new Mat();
		hierarchy = new Mat();
		polygon = new MatOfPoint2f();

	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		if (camera.read(cameraFrame)) {
			Imgproc.cvtColor(cameraFrame, grayImage, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(grayImage, blur, new Size(5,5), 0);
			//Imgproc.adaptiveThreshold(blur, binaryImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);			
			Imgproc.threshold(blur, binaryImage, -1, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
			//Imgproc.GaussianBlur(binaryImage, binaryImage2, new Size(3,3), 0);
			contours = new ArrayList<MatOfPoint>();
			rects = new ArrayList<MatOfPoint>();
			Imgproc.findContours(binaryImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
			System.out.println(contours.size());
			//Imgproc.drawContours(cameraFrame, contours, -1, new Scalar(245, 7, 253));
			for(int i=0; i<contours.size(); i++){
				MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
				double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
				Imgproc.approxPolyDP(contour2f, polygon, approxDistance , true);
				MatOfPoint points = new MatOfPoint(polygon.toArray());
				//Rect rect = Imgproc.boundingRect(points);
				if(points.rows()==4 && Imgproc.arcLength(contour2f, true) > 320){
					Point point1 = new Point(points.get(0,0));
					Point point2 = new Point(points.get(1,0));
					if(point1.x*point2.y < point1.y*point2.x ){
						rects.add(points);
					}
				}
			}				
			for(int j=0; j<rects.size(); j++){
				Point point1 = new Point(rects.get(j).get(0,0));
				Point point2 = new Point(rects.get(j).get(1,0));
				Point point3 = new Point(rects.get(j).get(2,0));
				Point point4 = new Point(rects.get(j).get(3,0));
				Core.circle(cameraFrame, point1, 4, new Scalar(68, 228, 153), -1, 8, 0);
				Core.circle(cameraFrame, point2, 4, new Scalar(68, 228, 153), -1, 8, 0);
				Core.circle(cameraFrame, point3, 4, new Scalar(68, 228, 153), -1, 8, 0);
				Core.circle(cameraFrame, point4, 4, new Scalar(68, 228, 153), -1, 8, 0);
				Core.line(cameraFrame, point1, point2, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point2, point3, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point3, point4, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point4, point1, new Scalar(68, 228, 153), 2);
			}		
			UtilAR.imDrawBackground(cameraFrame);
		}	
	}
}
