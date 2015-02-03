package com.mygdx.test;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.math.Vector3;

public class MyGdxTest extends ApplicationAdapter {
	public PerspectiveCamera cam;
	public ModelBatch modelBatch;
	public Model model;
	public Model x;
	public Model y;
	public Model z;
    public ModelInstance instance1;
    public ModelInstance instance2;
    public ModelInstance instance3;
    public ModelInstance instance4;
    public Environment environment;
    public CameraInputController camController;
	
	@Override
	public void create() {
		environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		modelBatch = new ModelBatch();
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(3f, 3f, 3f);
        cam.lookAt(0,0,0);
        cam.near = 0.01f;
        cam.far = 300f;
        cam.update();        
        
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(1f, 1f, 1f, 
            new Material(ColorAttribute.createDiffuse(Color.GRAY)),
            Usage.Position | Usage.Normal);
        instance1 = new ModelInstance(model, 0f, 0f, 0f);
        instance1.transform.translate(1f,0.5f,0.75f);
        
        Vector3 zero = new Vector3(0,0,0);
        Vector3 xEnd = new Vector3(2,0,0);
        Vector3 yEnd = new Vector3(0,2,0);
        Vector3 zEnd = new Vector3(0,0,2);
        
        x = modelBuilder.createArrow(zero, xEnd,
        		new Material(ColorAttribute.createDiffuse(Color.BLUE)), 
        		Usage.Position | Usage.Normal);
        instance2 = new ModelInstance(x, 0f, 0f, 0f);
        
        y = modelBuilder.createArrow(zero, yEnd, 
        		new Material(ColorAttribute.createDiffuse(Color.GREEN)), 
        		Usage.Position | Usage.Normal);
        instance3 = new ModelInstance(y, 0f, 0f, 0f);
        
        z = modelBuilder.createArrow(zero, zEnd, 
        		new Material(ColorAttribute.createDiffuse(Color.RED)), 
        		Usage.Position | Usage.Normal);
        instance4 = new ModelInstance(z, 0f, 0f, 0f);
        
        
        /*camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);*/
	}
	
    @Override
    public void render () {	
    	cam.rotateAround(new Vector3(1f, 0.5f, 0.75f), new Vector3(0,1,0), 1);  	
    	cam.update();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        ArrayList<ModelInstance> instances = new ArrayList<ModelInstance>();
        instances.add(instance1);
        instances.add(instance2);
        instances.add(instance3);
        instances.add(instance4);
        
        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }
	
	@Override
    public void dispose () {
		modelBatch.dispose();
        model.dispose();
		x.dispose();
		y.dispose();
		z.dispose();
    }

}
