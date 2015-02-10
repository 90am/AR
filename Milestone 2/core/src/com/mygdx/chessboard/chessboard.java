package com.mygdx.chessboard;

import java.util.ArrayList;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
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
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class chessboard extends ApplicationAdapter {
	public Mat cameraFrame;
	public VideoCapture camera;
	public Model model;
	public ModelInstance boxInstance;
	public ModelBatch modelBatch;
	public Environment environment;
	public PerspectiveCamera myCamera;
	public CameraInputController cameraController;
	public Size patternSize;
	public MatOfPoint2f corners;
	public MatOfPoint3f objectPoints;
	public int sizeX;
	public int sizeZ;
	public ArrayList<ModelInstance> boxes;
	
	@Override
	public void create() {
		// Set up camera
		myCamera = new PerspectiveCamera(40, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		/*myCamera.position.set(10, 10, 10);
		myCamera.lookAt(0, 0, 0);*/
		myCamera.near = 1;
		myCamera.far = 300;
		myCamera.update();
		
		camera = new VideoCapture(0);
		camera.open(0);
		cameraFrame = new Mat();
		
		sizeX = 9;
		sizeZ = 6;
		patternSize = new Size(sizeX, sizeZ);
		corners = new MatOfPoint2f();
		corners.alloc(sizeX*sizeZ);
		
		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(0.5f, 0.5f, 0.5f, 
				new Material(ColorAttribute.createDiffuse(Color.YELLOW)),
				Usage.Position | Usage.Normal);
		
		boxes = new ArrayList<ModelInstance>();
		
		Mat temp = Mat.zeros(sizeX*sizeZ, 1, CvType.CV_32FC3);
		int position = 0;
		for(int j=0; j<sizeZ; j++){
			for(int i=0; i<sizeX; i++){
				temp.put(position, 0, i, 0, j);
				position++;	
				boxes.add(new ModelInstance(model, i, 0, j));
			}
		}
		
		boxInstance = new ModelInstance(model, 0, 0, 0);
		
		System.out.println(temp.dump());
		objectPoints = new MatOfPoint3f(temp);
		System.out.println(objectPoints.dump());
		
		// Set up model batch
		modelBatch = new ModelBatch();
	
		// Set up lighting
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.Specular, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		MatOfPoint2f rvec = new MatOfPoint2f();
		MatOfPoint2f tvec = new MatOfPoint2f();
		if (camera.read(cameraFrame)) {
			if(Calib3d.findChessboardCorners(cameraFrame, patternSize, corners)){
				Calib3d.drawChessboardCorners(cameraFrame, patternSize, corners, true);
				Mat intrinsics = UtilAR.getDefaultIntrinsicMatrix((int)cameraFrame.size().height, (int)cameraFrame.size().width);			
				Calib3d.solvePnP(objectPoints, corners, intrinsics, UtilAR.getDefaultDistortionCoefficients(), rvec, tvec);
				UtilAR.setCameraByRT(rvec, tvec, myCamera);
				myCamera.update();
				UtilAR.imDrawBackground(cameraFrame);
				modelBatch.begin(myCamera);
				modelBatch.render(boxes, environment);
				modelBatch.end();
			}
			else{
				UtilAR.imDrawBackground(cameraFrame);
			}
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		camera.release();
		modelBatch.dispose();
		model.dispose();
	}
}
