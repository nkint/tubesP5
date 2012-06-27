package ptf;

import java.util.ArrayList;
import java.util.List;

import ptf.ParallelTransportFrame;

import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;
import toxi.math.MathUtils;

public class Tube extends TriangleMesh {

	private ParallelTransportFrame soul;
	private int curveLength;
	private int radius = 10;
	private int diameterQuality = 20;
	private float[] cachedRadius = null;
	private boolean usedCachedRadius = false;
	
	private List< List<Vec3D> > circles = new ArrayList<List<Vec3D>>();
	private int num_faces;

	//-------------------------------------------------------- ctor
	
	public Tube(ParallelTransportFrame soul, int radius, int diameter_quality) {
		System.out.println("Tube > constructor");
		
		this.soul = soul;
		this.curveLength = soul.getCurveLength();;
		this.setRadius(radius);
		this.diameterQuality = diameter_quality;
		
		if(soul.getCurveLength()==0) return;
		
		compute();
	}
	
	//-------------------------------------------------------- vertex computation

	public void compute() {
		num_faces = 0;
		
		List<Vec3D> circle1, circle2;
		float radius;
		
		radius = (isUsedCachedRadius() ? cachedRadius[0]: getRadius());
		circle1 = getCircle(0, radius);
		for(int i=1; i<curveLength-1; i++) {
			radius = (isUsedCachedRadius() ? cachedRadius[i]: getRadius());
			circle2 = getCircle(i, radius);
			
			addCircles(circle1, circle2);
			
			circle1 = circle2;
		}
		
	}
	
	List<Vec3D> getCircle(int i, float _radius) {
		int k = diameterQuality;
		List<Vec3D> vert;
		float theta = 0;
		float dt = MathUtils.TWO_PI/(k);
		
		if(i<this.circles.size()) {
			// circle exists, does not create a new one, just modify it
			vert = circles.get(i);
		} else {
			// new length, we have to allocate new objects
			vert = new ArrayList<Vec3D>(k+1);
			for(int j=0; j<=k; j++) 
				vert.add(new Vec3D());
			
		}
		
		for(int j=0; j<=k; j++) {
			float c = MathUtils.cos(theta) * _radius;
			float s = MathUtils.sin(theta) * _radius;

			Vec3D p = vert.get(j);
			p.x = soul.vertices.get(i).x + c*soul.getBinormal(i).x + s*soul.getNormal(i).x;
			p.y = soul.vertices.get(i).y + c*soul.getBinormal(i).y + s*soul.getNormal(i).y;
			p.z = soul.vertices.get(i).z + c*soul.getBinormal(i).z + s*soul.getNormal(i).z;

			theta += dt;
		}  

		// cache the result back
		circles.add(vert);
		
		return vert; 
	}


	void addCircles(List<Vec3D> circle1, List<Vec3D> circle2) {
		Vec3D  p1, p2, p3, p4, p5, p6;
		Face f1, f2;
		boolean must_add = false;
		
		for(int j=0; j<circle1.size()-1; j++) {

			try { // vertices exists, does not create new ones, just modify them
				
				f1 = this.faces.get(num_faces++);
				p1 = f1.a; p2 = f1.b; p3 = f1.c;
				
				f2 = this.faces.get(num_faces++);
				p4 = f2.a; p5 = f2.b; p6 = f2.c;
				
			} catch (IndexOutOfBoundsException e) { // new length, we have to allocate new objects
				
				//System.out.println("addCircles > new");
				
				p1 = new Vec3D(); p2 = new Vec3D(); p3 = new Vec3D();
				p4 = new Vec3D(); p5 = new Vec3D(); p6 = new Vec3D();
				
				must_add = true;
			}
			
			p1.set(circle1.get(j).x,circle1.get(j).y,circle1.get(j).z);       
			p2.set(circle2.get(j).x,circle2.get(j).y,circle2.get(j).z);
			p3.set(circle2.get(j+1).x,circle2.get(j+1).y,circle2.get(j+1).z);			

			p4.set(circle2.get(j+1).x,circle2.get(j+1).y,circle2.get(j+1).z); 
			p5.set(circle1.get(j).x,circle1.get(j).y,circle1.get(j).z);       
			p6.set(circle1.get(j+1).x,circle1.get(j+1).y,circle1.get(j+1).z);

			if(must_add) {
				this.addFace(p1, p2, p3);
				this.addFace(p4, p5, p6);
			}
		}
	}

	
	public void setCachedRadius(float[] c) {
		this.cachedRadius = c;
		if(c!=null) setUsedCachedRadius(true);
		else setUsedCachedRadius(false);
	}

	public void setUsedCachedRadius(boolean usedCachedRadius) {
		this.usedCachedRadius = usedCachedRadius;
	}

	public boolean isUsedCachedRadius() {
		return usedCachedRadius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getRadius() {
		return radius;
	}
	
	public int getDiameterQuality() {
		return diameterQuality;
	}

	public void setDiameterQuality(int diameterQuality) {
		this.diameterQuality = diameterQuality;
	}

	public int getCurveLength() {
		return curveLength;
	}

	public void setCurveLength(int curveLength) {
		this.curveLength = curveLength;
	}

	public List<List<Vec3D>> getCircles() {
		return circles;
	}

	
}
