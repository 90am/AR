package com.mygdx.milstone3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files.FileType;
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
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.UBJsonReader;
import com.mygdx.milstone3.UtilAR;


public class milestone3 extends ApplicationAdapter {
	public Mat cameraFrame;
	public Mat grayImage;
	public Mat grayImage2;
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
	public ArrayList<ModelInstance> instancesToRender;
	public ModelBatch modelBatch;
	public Environment environment;
	public MatOfPoint2f rvec = new MatOfPoint2f();
	public MatOfPoint2f tvec = new MatOfPoint2f();
	public boolean hasDrawn = false;
	public Mat dst;
	public MatOfPoint2f imagePoints;
	public Mat output;
	public String marker1 = "0000000001011010010000100010001001111000011001000000111000000000";
	public String marker2 = "0000000000000000000010100110101001001110011101100001110000000000";
	public List<Mat> knownMarkers;
	public List<Model> models3d;
	public ModelInstance fancy3dModelInstance;
	public ModelBuilder modelBuilder;
	
	@Override
	public void create () {
		
		// Set up camera
		myCamera = new PerspectiveCamera(40, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		myCamera.near = 0.01f;
		myCamera.far = 300;
		UtilAR.setNeutralCamera(myCamera);
		myCamera.update();
		
		instancesToRender = new ArrayList<ModelInstance>();
		knownMarkers = new ArrayList<Mat>();
		models3d = new ArrayList<Model>();	
		
		addMarker(marker1);
		addMarker(marker2);
		
		UBJsonReader jsonReader = new UBJsonReader();
		G3dModelLoader modelLoader = new G3dModelLoader(jsonReader);
		model = modelLoader.loadModel(Gdx.files.getFileHandle("SpaceShip/statek.dae.g3db", FileType.Internal));
		models3d.add(model);
		/*model = modelLoader.loadModel(Gdx.files.getFileHandle("Eye/eye_pot.g3db", FileType.Internal));
		models3d.add(model);*/	
		model = modelLoader.loadModel(Gdx.files.getFileHandle("Jeep/jeep.g3db", FileType.Internal));
		models3d.add(model);
		
		camera = new VideoCapture(0);
		camera.open(0);
		cameraFrame = new Mat();
		grayImage = new Mat();
		grayImage2 = new Mat();
		binaryImage = new Mat();
		binaryImage2 = new Mat();
		blur = new Mat();
		hierarchy = new Mat();
		polygon = new MatOfPoint2f();
		
		dst = new Mat(200, 200, CvType.CV_32FC1);
		
		Mat temp = Mat.zeros(2*2, 1, CvType.CV_32FC3);
		temp.put(0, 0, 0, 0, 0);
		temp.put(1, 0, 1, 0, 0);
		temp.put(2, 0, 1, 0, 1);
		temp.put(3, 0, 0, 0, 1);
		
		objectPoints = new MatOfPoint3f(temp);
		
		temp = Mat.zeros(2*2, 1, CvType.CV_32FC2);
		temp.put(0, 0, 0, 0);
		temp.put(1, 0, 200, 0);
		temp.put(2, 0, 200, 200);
		temp.put(3, 0, 0, 200);
		
		homographyPoints = new MatOfPoint2f(temp);	
       	
		modelBuilder = new ModelBuilder();
		/*model = modelBuilder.createArrow(0f, 0f, 0f, 0.5f, 0f, 0f, 0.1f, 0.3f, 200, 1,
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
		
		instancesToRender.add(instanceX);
        instancesToRender.add(instanceY);
        instancesToRender.add(instanceZ);*/
		
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
			//System.out.println(contours.size());
			//Imgproc.drawContours(cameraFrame, contours, -1, new Scalar(245, 7, 253));
			for(int i=0; i<contours.size(); i++){
				MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
				double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
				Imgproc.approxPolyDP(contour2f, polygon, approxDistance , true);
				MatOfPoint points = new MatOfPoint(polygon.toArray());
				if(points.rows()==4 && Imgproc.arcLength(contour2f, true) > 200){
					Point point1 = new Point(points.get(0,0));
					Point point2 = new Point(points.get(1,0));
					Mat vector1 = Mat.zeros(1, 1, CvType.CV_32FC3);
					Mat vector2 = Mat.zeros(1, 1, CvType.CV_32FC3);
					vector1.put(0, 0, point1.x, point1.y, 0);
					vector2.put(0, 0, point2.x, point2.y, 0);
					Mat result = Mat.zeros(1, 1, CvType.CV_32FC3); 
					result = vector1.cross(vector2);
					if(result.get(0, 0)[2] < 0){
						rects.add(points);
					}
				}
			}				
			/*for(int j=0; j<rects.size(); j++){
				Point point1 = new Point(rects.get(j).get(0,0));
				Point point2 = new Point(rects.get(j).get(1,0));
				Point point3 = new Point(rects.get(j).get(2,0));
				Point point4 = new Point(rects.get(j).get(3,0));
				Core.line(cameraFrame, point1, point2, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point2, point3, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point3, point4, new Scalar(68, 228, 153), 2);
				Core.line(cameraFrame, point4, point1, new Scalar(68, 228, 153), 2);		
			}*/
			instancesToRender = new ArrayList<ModelInstance>();
			int first = 0;
			int seccond = 0;
			for(int j=0; j<rects.size(); j++){
				intrinsics = UtilAR.getDefaultIntrinsicMatrix((int)cameraFrame.size().width, (int)cameraFrame.size().height);
				distortionCoefficients = UtilAR.getDefaultDistortionCoefficients();
				imagePoints = new MatOfPoint2f(rects.get(j).toArray());
				//System.out.println(imagePoints.dump());
				Calib3d.solvePnP(objectPoints, imagePoints, intrinsics, distortionCoefficients, rvec, tvec);
				output = Calib3d.findHomography(imagePoints, homographyPoints);				
				Imgproc.warpPerspective(cameraFrame, dst, output, new Size(200,200));
				//System.out.println(output.dump());
				Imgproc.cvtColor(dst, grayImage2, Imgproc.COLOR_BGR2GRAY);
				Imgproc.threshold(grayImage2, binaryImage2, -1, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
				UtilAR.imShow(binaryImage2);
				Mat currentMarker = getCode(binaryImage2);
				//UtilAR.setCameraByRT(rvec, tvec, myCamera);			
				int modelIndex = knownMarkersContains(currentMarker);				
				if(modelIndex != -1){
					fancy3dModelInstance = new ModelInstance(models3d.get(modelIndex), 0f, 0f, 0f);
					UtilAR.setTransformByRT(rvec, tvec, fancy3dModelInstance.transform);
					fancy3dModelInstance.transform.translate(0.5f, 0.5f, 0.5f);
					instancesToRender.add(fancy3dModelInstance);
					if(modelIndex == 0){
						first = j;
					}
					else{
						seccond = j;
					}
				}		
				if(instancesToRender.size() > 1){
					BoundingBox box1 = new BoundingBox();
					instancesToRender.get(0).calculateBoundingBox(box1);
					BoundingBox box2 = new BoundingBox();
					instancesToRender.get(1).calculateBoundingBox(box2);
					System.out.println(instancesToRender.size());
					Point3 p2 = new Point3();
					System.out.println(box1.getCenter());
					System.out.println(box2.getCenter());
					
					//Core.line(cameraFrame, new Point(rects.get(first).get(0,0)), new Point(rects.get(seccond).get(0,0)), new Scalar(68, 228, 153), 2);
				}						
			}
			UtilAR.imDrawBackground(cameraFrame);
			modelBatch.begin(myCamera);
			modelBatch.render(instancesToRender, environment);
			modelBatch.end();
		}	
	}
	
	public void addMarker(String s){		
		Mat marker = Mat.zeros(8, 8, CvType.CV_32FC1);
		char[] stringArray = s.toCharArray();
		int counter = 0;
		for(int i=0; i<8; i++){
			for(int j=0; j<8; j++){
				int current = Character.getNumericValue(stringArray[counter]);
				marker.put(i, j, current);
				counter++;
			}
		}	
		knownMarkers.add(marker);
	}
	
	public int knownMarkersContains(Mat m){
		Mat temp = new Mat();
		Mat temp2 = new Mat();
		temp2 = m;
		for(int i=0; i<4; i++){
			Core.transpose(temp2, temp);
			Core.flip(temp, temp2, 1);
			for(int j=0; j<knownMarkers.size(); j++){
				if(knownMarkers.get(j).dump().equals(m.dump())){
					return j;
				}
			}
		}
		return -1;
	}
	
	public Mat getCode(Mat m){
		int squareSize = (int) m.size().height/8;
		Mat result = Mat.zeros(8, 8, CvType.CV_32FC1);
		int row = 0;
		int col = 0;
		for(int i=0; i<8; i++){
			row = i*squareSize;
			for(int j=0; j<8; j++){
				col = j*squareSize;
				int temp = 0;
					for(int h=0; h<squareSize; h++){
						for(int k=0; k<squareSize; k++){				
							if((int) m.get(row+h, col+k)[0] > 0){
								temp += 1;
							}
							else{
								temp += 0;
							}
						}
					}
				if(temp > (squareSize*squareSize)/2){
					result.put(i, j, 1);
				}
				else{
					result.put(i, j, 0);
				}
			}
		}
		return result;
	}
	
	
}



