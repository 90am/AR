package com.mygdx.milstone3;

import java.util.ArrayList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
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
	public Mat intrinsics;
	public MatOfDouble distortionCoefficients;
	public MatOfPoint3f objectPoints;
	public MatOfPoint2f homographyPoints;
 	public PerspectiveCamera myCamera;
	public Model model;
	public ModelInstance instanceX;
	public ModelInstance instanceY;
	public ModelInstance instanceZ;
	public ArrayList<ModelInstance> instances;
	public ModelBatch modelBatch;
	public Environment environment;
	MatOfPoint2f rvec = new MatOfPoint2f();
	MatOfPoint2f tvec = new MatOfPoint2f();
	public boolean hasDrawn = false;
	
	@Override
	public void create () {
		// Set up camera
		myCamera = new PerspectiveCamera(40, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		/*myCamera.position.set(10, 10, 10);
		myCamera.lookAt(0, 0, 0);*/
		myCamera.near = 0.01f;
		myCamera.far = 300;
		myCamera.update();
		
		camera = new VideoCapture(0);
		camera.open(0);
		cameraFrame = new Mat();
		grayImage = new Mat();
		binaryImage = new Mat();
		binaryImage2 = new Mat();
		blur = new Mat();
		hierarchy = new Mat();
		polygon = new MatOfPoint2f();
		
		Mat temp = Mat.zeros(2*2, 1, CvType.CV_32FC3);
		temp.put(0, 0, 0, 0, 0);
		temp.put(1, 0, 1, 0, 0);
		temp.put(2, 0, 1, 0, 1);
		temp.put(3, 0, 0, 0, 1);
		
		objectPoints = new MatOfPoint3f(temp);
		
		temp = Mat.zeros(2*2, 1, CvType.CV_32FC3);
		temp.put(0, 0, 0, 0);
		temp.put(1, 0, 200, 0);
		temp.put(2, 0, 200, 200);
		temp.put(3, 0, 0, 200);
       	
		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createArrow(0f, 0f, 0f, 0.5f, 0f, 0f, 0.1f, 0.3f, 200, 1,
        		new Material(ColorAttribute.createDiffuse(Color.BLUE)), 
        		Usage.Position | Usage.Normal);	
		instanceX = new ModelInstance(model, 0f, 0f, 0f);		
		model = modelBuilder.createArrow(0f, 0f, 0f, 0f, 0.5f, 0f, 0.1f, 0.3f, 200, 1,
        		new Material(ColorAttribute.createDiffuse(Color.GREEN)), 
        		Usage.Position | Usage.Normal);
		instanceY = new ModelInstance(model, 0f, 0f, 0f);
		model = modelBuilder.createArrow(0f, 0f, 0f, 0f, 0f, 0.5f, 0.1f, 0.3f, 200, 1,
        		new Material(ColorAttribute.createDiffuse(Color.RED)), 
        		Usage.Position | Usage.Normal);
		instanceZ = new ModelInstance(model, 0f, 0f, 0f);
		
		instances = new ArrayList<ModelInstance>();
        instances.add(instanceX);
        instances.add(instanceY);
        instances.add(instanceZ);
		
		modelBatch = new ModelBatch();
		
		// Set up lighting
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.Specular, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

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
				if(points.rows()==4 && Imgproc.arcLength(contour2f, true) > 200){
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
				Core.line(cameraFrame, point1, point2, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point2, point3, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point3, point4, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point4, point1, new Scalar(68, 228, 153), 2);		
			}
			UtilAR.imDrawBackground(cameraFrame);
			for(int j=0; j<rects.size(); j++){
				intrinsics = UtilAR.getDefaultIntrinsicMatrix((int)cameraFrame.size().width, (int)cameraFrame.size().height);
				distortionCoefficients = UtilAR.getDefaultDistortionCoefficients();
				MatOfPoint2f imagePoints = new MatOfPoint2f(rects.get(j).toArray());
				System.out.println(imagePoints.dump());
				Calib3d.solvePnP(objectPoints, imagePoints, intrinsics, distortionCoefficients, rvec, tvec);
				Mat output = Calib3d.findHomography(imagePoints, homographyPoints);
				UtilAR.imToTexture(cameraFrame, UtilAR.createMatTexture(output));
				UtilAR.setCameraByRT(rvec, tvec, myCamera);			
				myCamera.update();
				modelBatch.begin(myCamera);
				modelBatch.render(instances, environment);
				modelBatch.end();				
			}
		}	
	}
}
