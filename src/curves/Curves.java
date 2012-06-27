package curves;

import java.util.ArrayList;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;
import controlP5.Group;
import peasy.PeasyCam;
import processing.core.PApplet;
import ptf.CurveFactory;
import ptf.LineStrip3D;
import ptf.ParallelTransportFrame;
import ptf.Tube;
import toxi.geom.Vec3D;
import toxi.processing.ToxiclibsSupport;


public class Curves extends PApplet {

	private static final String CURVES = "curves";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String _args[]) {
		PApplet.main(new String[] { curves.Curves.class.getName() });
	}

	//-------------------------------------------------------------------------------- vars

	private ToxiclibsSupport fx;
	private PeasyCam cam;
	private ControlP5 gui;

	LineStrip3D curve;
	ParallelTransportFrame ptf;	
	int curve_length = 150; // number of points that represent the curve
	Tube tube = null;

	boolean drawing;
	ArrayList<Group> guiGroups = new ArrayList<Group>();
	private int currentGroup;
	private int P;
	private int Q;

	//-------------------------------------------------------------------------------- setup

	public void setup() {
		size(900,600, OPENGL);
		frameRate(60);

		fx = new ToxiclibsSupport(this);
		cam = new PeasyCam(this, 350);
		gui = new ControlP5(this);
		gui.setAutoDraw(false);

		initGui();

		curve = new LineStrip3D();

		createTube();
	}

	private void createTube() {
		ptf = new ParallelTransportFrame(curve.getVertices());
		tube = new Tube(ptf, 6, 10);
		tube.computeVertexNormals();
	}

	//-------------------------------------------------------------------------------- draw

	public void draw() {

		//------------------------- update

		if(currentGroup==0 && keyPressed && mouseX!=pmouseX && mouseY!=pmouseY) {
			curve.add(new Vec3D(mouseX, mouseY, 0));
			createTube();
			
			System.out.println("added");
		}

		//------------------------- draw

		background(50);

		lights();

		strokeWeight(1);
		fx.origin(10);

		if(tube!=null) {
			fill(155);
			stroke(155);
			fx.mesh(this.tube, false);
			fx.lineStrip3D(curve.getVertices());
			drawFrames();
		}

		gui();
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

	void drawVectorOnPoint(Vec3D pos, Vec3D vector) {
		float k = 10;
		beginShape();
		vertex(pos.x, pos.y, pos.z);
		vertex(pos.x + vector.x*k, pos.y + vector.y*k, pos.z + vector.z*k);
		endShape(); 
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

	//-------------------------------------------------------------------------------- gui

	private void initGui() {

		DropdownList ddl = gui.addDropdownList(CURVES)
		.setPosition(10, 30);

		ddl.addItem("Mouse", 0);
		ddl.addItem("Toroide", 1);
//		ddl.addItem("Viviani", 2);
//		ddl.addItem("Spline", 3);
//		ddl.addItem("Helicoide", 4);

		//-------------------- mouse
		guiGroups.add( gui.addGroup("mouse") );

		gui.addTextlabel("DUDEE")
		.setText("press a key and move mouse around to draw a tube")
		.setPosition(200, 20)
		.setGroup("mouse");

		gui.getGroup("mouse").setVisible(false);

		//-------------------- torioide
		guiGroups.add( gui.addGroup("toroide") );

		gui.addSlider("set_P")
		.setCaptionLabel("P")
		.setPosition(200, 20)
		.setRange(1, 10)
		.setValue(3)
		.setNumberOfTickMarks(10)
		.setGroup("toroide");
		gui.addSlider("set_Q")
		.setCaptionLabel("Q")
		.setPosition(200, 50)
		.setRange(1, 10)
		.setValue(1)
		.setNumberOfTickMarks(10)
		.setGroup("toroide");

		gui.getGroup("toroide").setVisible(false);

	}

	public void controlEvent(ControlEvent theEvent) {
		if(theEvent.getName()==CURVES) {
			currentGroup = (int) theEvent.getValue();

			// gui visibility
			for (int i = 0; i < guiGroups.size(); i++) {
				Group g = guiGroups.get(i);
				if(currentGroup==i) g.setVisible(true);
				else g.setVisible(false);
			}

			// curve inits
			switch (currentGroup) {
			case 0:
				curve = new LineStrip3D();
				break;
			case 1:
				curve = CurveFactory.getPQKnot(curve_length, 2, 4);
				createTube();
				break;

			default:
				break;
			}
		}

	}

	public void set_P(int n) {
		this.P = n;
		curve = CurveFactory.getPQKnot(curve_length, P, Q);
		createTube();
	}
	
	public void set_Q(int n) {
		this.Q = n;
		curve = CurveFactory.getPQKnot(curve_length, P, Q);
		createTube();
	}
}
