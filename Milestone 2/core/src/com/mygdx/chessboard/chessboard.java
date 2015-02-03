package com.mygdx.chessboard;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;

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
	public Mat m1;
	public Mat cameraFrame;
	public VideoCapture camera;
	public Model model;
	public ModelInstance boxInstance;
	public ModelBatch modelBatch;
	public Environment environment;
	public PerspectiveCamera myCamera;
	public CameraInputController cameraController;
	
	@Override
	public void create () {
		// Set up camera
		myCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		myCamera.position.set(3, 3, 3);
		myCamera.lookAt(0, 0, 0);
		myCamera.near = 1;
		myCamera.far = 300;
		myCamera.update();
		
		camera = new VideoCapture(0);
		camera.open(0);
		cameraFrame = new Mat();

		// Set up model batch
		modelBatch = new ModelBatch();

		// Set up model, and create instance
		ModelBuilder modelBuilder = new ModelBuilder();
		
		// Box
		model = modelBuilder.createBox(1, 1, 1, 
				new Material(ColorAttribute.createDiffuse(Color.YELLOW)),
				Usage.Position | Usage.Normal);
		boxInstance = new ModelInstance(model, 1f, 0.5f, 0.75f);

		// Make control of camera possible
		cameraController = new CameraInputController(myCamera);
		Gdx.input.setInputProcessor(cameraController);

		// Set up lighting
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.Specular, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}

	@Override
	public void render () {
		if (camera.read(cameraFrame)) {
			UtilAR.imDrawBackground(cameraFrame);
		}

		// Set size of image
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// Model batch will render objects
		modelBatch.begin(myCamera);
		modelBatch.render(boxInstance, environment);
		modelBatch.end();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		camera.release();
		modelBatch.dispose();
		model.dispose();
	}
}
