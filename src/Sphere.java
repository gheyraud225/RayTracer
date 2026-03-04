public class Sphere implements SceneObject{

    Vec3 Centre;
    double r;
    Material material;
    public Sphere(Vec3 O, double r,Material material){
        this.Centre = O;
        this.r = r;
        this.material = material;
    }

    public double hit( Rayon r, double t_min, double t_max){
        Vec3 OC = r.Origine.sub(this.Centre);
        double a = 1.0; // r.D est un vecteur unitaire
        double b = 2.0 * r.D.dotProduct(OC);
        double c = OC.dotProduct(OC) - this.r*this.r;
        double delta = b*b - 4*a*c;
        if(delta < 0){
            return -1.0;
        }
        double sqrt_delta = Math.sqrt(delta);
        double t1 = (-b - sqrt_delta )/ (2.0*a);
        double t2 = (-b + sqrt_delta )/ (2.0*a);
        if(t1 > t_min && t1 < t_max){
            return t1;
        }
        if(t2 > t_min && t2 < t_max){
            return t2;
        }
        return -1.0;
    }
    public Vec3 getNormalAt(Vec3 i){
        return i.sub(this.Centre).toUnit();
    }

    @Override
    public Material getMaterial() {
        return material;
    }
    @Override
    public void getUV(Vec3 p, Vec3 out_uv) {
        // 1. Calcule le point relatif au centre de la sphère
        // (On a besoin d'un 'temp' du scratchpad, ou d'un 'new Vec3' temporaire ici)
        Vec3 p_local = p.sub(this.Centre); // p.sub() crée un 'new Vec3'

        // 2. Calcule les angles
        // 'p_local.y' est la "hauteur"
        // 'atan2' gère tous les quadrants pour 'phi'

        // theta = angle depuis le pôle sud (y=-1) vers le pôle nord (y=+1)
        double theta = Math.acos(-p_local.y / this.r); // /r pour normaliser

        // phi = angle autour de l'équateur (basé sur x et z)
        double phi = Math.atan2(-p_local.z, p_local.x) + Math.PI;

        // 3. Convertit les angles (en radians) en coordonnées UV (0..1)
        double u = phi / (2.0 * Math.PI);
        double v = theta / Math.PI;

        out_uv.set(u, v, 0);
    }
}
