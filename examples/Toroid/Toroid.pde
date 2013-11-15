
import java.util.Iterator;
import java.util.LinkedList;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.opengl.*;
import tubesP5.library.CurveFactory;
import tubesP5.library.LineStrip3D;
import tubesP5.library.ParallelTransportFrame;
import tubesP5.library.Tube;
import toxi.geom.Vec3D;
import toxi.processing.ToxiclibsSupport;
import controlP5.*;

// references:

// http://www.gamedev.net/community/forums/topic.asp?topic_id=378061
// http://www.openframeworks.cc/forum/viewtopic.php?f=8&t=3383
// http://www.gamedev.net/community/forums/topic.asp?topic_id=577287
// http://www.gamedev.net/community/forums/topic.asp?topic_id=577517

/**
 * 
 */
private static final long serialVersionUID = 1L;

public static void main(String _args[]) {
  PApplet.main(new String[] { Toroid.class.getName() });
}

//-------------------------------------------------------------------- variables

PeasyCam cam;  
ToxiclibsSupport fx;
ControlP5 gui;

LineStrip3D curve;
ParallelTransportFrame ptf;  
int curve_length = 150; // number of points that represent the curve
Tube tube = null;

private int diameter_quality = 84;
private int radius = 6;
private int P = 3;
private int Q = 1;

private boolean curve_visibility = false;
private boolean frames_visibility = false; 
private boolean mesh_visibility = true;

private boolean perlin = false;
private LinkedList<Float> perlinArray = new LinkedList<Float>();
private float xoff = 0;
private float perlinDelta = .1f;
private int perlinOctave = 1;

private Textlabel fpsLabel;

private boolean record;


//-------------------------------------------------------------------- setup

public void setup() {;
  size(900,600, P3D);
  frameRate(60);
   
  fx = new ToxiclibsSupport(this);
  cam = new PeasyCam(this, 350);
  gui = new ControlP5(this);
  gui.setAutoDraw(false);
  
  initGui();
  initCurve();
  
  System.out.println("==============================");
}

private void initCurve() {    
  if(curve_length>2) {
    curve = CurveFactory.getPQKnot(curve_length, P, Q);
    ptf = new ParallelTransportFrame(curve.getVertices());
    tube = new Tube(ptf, radius, diameter_quality);
    tube.computeVertexNormals();
  }
}

//-------------------------------------------------------------------- draw

public void draw() {
  
  if (perlin) updatePerlin();
  
  background(0);
  lights();

  strokeWeight(1);
  fx.origin(10);
  
  noFill();
  stroke(155);
  strokeWeight(1);
  if(curve_visibility) fx.lineStrip3D(curve.getVertices());
  strokeWeight(3);
  if(frames_visibility) drawFrames();

  noStroke();
  fill(155);

  if (record) beginRaw(DXF, frame+".dxf");
  if (mesh_visibility) fx.mesh(this.tube, false);
  if (record) endRaw();
  
  fpsLabel.setText("FPS: " + (int)this.frameRate);
  gui();
}

private void updatePerlin() {
  perlinArray.removeLast();
  perlinArray.push( noise(xoff)*radius );
  
  int i=0;
  float c[] = new float[perlinArray.size()];
  Iterator<Float> itr = perlinArray.iterator();
  while(itr.hasNext()) {
    c[i] = itr.next().floatValue();
    i++;
  }
  c[0] = c[curve_length];
  c[1] = (c[0]+c[2])/2.0f;
  c[2] = (c[1]+c[3])/2.0f;
  
  tube.setCachedRadius(c);
  tube.compute();
  
  xoff += this.perlinDelta;
}

void gui() {
  noLights();
  
  cam.beginHUD();
  gui.draw();
  cam.endHUD();
  
  cam.setMouseControlled(true);
  if(gui.isMouseOver()) {
    cam.setMouseControlled(false);
  } 
}

void drawFrames() {    
  int tube_size = ptf.getVertices().size();
  for(int i=0; i<tube_size-1; i++) {
    
    stroke(255,0,0, 100);      
    drawVectorOnPoint(ptf.vertices.get(i), ptf.getTangent(i));
    stroke(0,255,0, 100);
    drawVectorOnPoint(ptf.vertices.get(i), ptf.getBinormal(i));
    stroke(0,0,255, 100);
    drawVectorOnPoint(ptf.vertices.get(i), ptf.getNormal(i));
  }
}

void drawVectorOnPoint(Vec3D pos, Vec3D vector) {
  float k = 10;
  beginShape();
  vertex(pos.x, pos.y, pos.z);
  vertex(pos.x + vector.x*k, pos.y + vector.y*k, pos.z + vector.z*k);
  endShape(); 
}


//-------------------------------------------------------------------- gui stuff

private void initGui() {
  
  gui.setAutoInitialization(false);
  
  this.fpsLabel = gui.addTextlabel("fps").setPosition(width-70, 20).setText("0");
  
  gui.addToggle("visibility")
    .setHeight(10)
    .setPosition(10, 10);
  gui.addToggle("curve")
    .setHeight(10)
    .setPosition(10,40);
  gui.addToggle("tube")
    .setHeight(10)
    .setPosition(10, 70);
  gui.addToggle("set_perlin")
    .setHeight(10)
    .setCaptionLabel("perlin")
    .setPosition(10, 100);
//    gui.addToggle("bezier")
//      .setHeight(10)
//      .setPosition(10, 130);
  gui.addToggle("record")
  .setHeight(10)
  .setPosition(10, 160);
  
  //--------------------------------------- visibility
  gui.addGroup("visibility_group");
  
  gui.addToggle("curve_visibility")
    .setHeight(10)
    .setCaptionLabel("curve")
    .setPosition(100,10)
    .setValue(0)
    .setGroup("visibility_group");
  gui.addToggle("frames_visibility")
    .setHeight(10)
    .setCaptionLabel("frenet frames")
    .setPosition(100,40)
    .setValue(0)
    .setGroup("visibility_group");
  gui.addToggle("mesh_visibility")
    .setHeight(10)
    .setCaptionLabel("mesh")
    .setPosition(100,70)
    .setValue(1)
    .setGroup("visibility_group");

  gui.getGroup("visibility_group").setVisible(false);
  
  //--------------------------------------- curve
  gui.addGroup("curve_group");

  gui.addSlider("set_curve_lenght")
    .setCaptionLabel("curve length")
    .setPosition(100, 100)
//      .setSize(200, 20)
    .setRange(10, 500)
    .setValue(150)
    .setGroup("curve_group");
  gui.addSlider("set_P")
    .setCaptionLabel("P")
    .setPosition(100, 130)
//      .setSize(200, 20)
    .setRange(1, 10)
    .setValue(3)
    .setNumberOfTickMarks(10)
    .setGroup("curve_group");
  gui.addSlider("set_Q")
    .setCaptionLabel("Q")
    .setPosition(100, 160)
//      .setSize(200, 20)
    .setRange(1, 10)
    .setValue(1)
    .setNumberOfTickMarks(10)
    .setGroup("curve_group");

  gui.getGroup("curve_group").setVisible(false);
  
  //--------------------------------------- curve
  gui.addGroup("tube_group");
  
  gui.addSlider("set_diameter_points")
    .setCaptionLabel("diameter points")
    .setPosition(100, 190)
//      .setSize(200, 20)
    .setRange(4, 84)
    .setValue(36)
    .setNumberOfTickMarks((84-4)/8)
    .setGroup("tube_group");
  gui.addSlider("set_radius")
    .setCaptionLabel("radius")
    .setPosition(100, 220)
//      .setSize(200, 20)
    .setRange(2, 100)
    .setValue(15)
    .setGroup("tube_group");
  
  gui.getGroup("tube_group").setVisible(false);
  
  //--------------------------------------- perlin
  gui.addGroup("perlin_group");
  
  gui.addSlider("perlinDelta")
    .setCaptionLabel("delta perlin")
    .setRange(0.001f, 1.5f)
    .setPosition(100, 250)
    .setGroup("perlin_group");
  gui.addSlider("set_perlinOctaves")
    .setCaptionLabel("octaves perlin")
    .setRange(1, 10)
    .setPosition(100, 280)
    .setGroup("perlin_group");
  
  gui.getGroup("perlin_group").setVisible(false);
  
}

public void visibility(boolean n) {
  gui.getGroup("visibility_group").setVisible(n);
}
public void curve(boolean n) {
  gui.getGroup("curve_group").setVisible(n);
}
public void tube(boolean n) {
  gui.getGroup("tube_group").setVisible(n);
}  

public void set_curve_lenght(int n) {
  this.curve_length = n;
  
  LinkedList<Float> _perlinArray = new LinkedList<Float>();
  int i;
  float r = radius;
  for(i=0; i<perlinArray.size(); i++) {
    r = perlinArray.get(i);
    _perlinArray.add(r);
  } for(; i<curve_length+1; i++)
    _perlinArray.add(r);
  
  this.perlinArray = _perlinArray;
  initCurve();
}

public void set_diameter_points(int n) {
  this.diameter_quality = n;
  initCurve();
}

public void set_radius(int n) {
  this.radius = n;
  this.tube.setRadius(n);
  this.tube.compute();
  //initCurve();
}

public void set_P(int n) {
  this.P = n;
  initCurve();
}

public void set_Q(int n) {
  this.Q = n;
  initCurve();
}

public void set_perlin(boolean n) {
  
  perlin = n;
  gui.getGroup("perlin_group").setVisible(n);
  
  perlinArray = new LinkedList<Float>();
  for(int i=0; i<curve_length+1; i++) {
    float r = radius;
    perlinArray.add(r);
  }
}

public void set_perlinOctaves(int n) {
  this.perlinOctave = n;
  noiseDetail(perlinOctave);
}


