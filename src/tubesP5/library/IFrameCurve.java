package tubesP5.library;

import toxi.geom.Vec3D;

public interface IFrameCurve {

	Vec3D getTangent(int i);
	Vec3D getNormal(int i);
	Vec3D getBinormal(int i);
}
