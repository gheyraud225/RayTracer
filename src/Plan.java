public class Plan implements SceneObject {
    Vec3 c;
    Vec3 n;
    Material material;
    public Plan(Vec3 c, Vec3 n, Material material){
        this.c = c;
        this.n = n;
        this.material = material;
    }
    public double hit(Rayon r, double t_min, double t_max){
        double denominateur = this.n.dotProduct(r.D);
        double epsilon = 1e-6;
        if(Math.abs(denominateur) < epsilon ){
            return -1.0;
        }
        double t = (this.n.dotProduct(this.c) - this.n.dotProduct(r.Origine)) /denominateur;
        if(t < t_min || t > t_max ){
            return -1.0;
        }
        return t;
    }
    public Vec3 getNormalAt(Vec3 i){
        return this.n.toUnit();
    }
    @Override
    public Material getMaterial() {
        return material;
    }
    @Override
    public void getUV(Vec3 p, Vec3 out_uv) {
        // On "enroule" la texture toutes les 1.0 unités
        double u = p.x - Math.floor(p.x);
        double v = p.z - Math.floor(p.z);
        out_uv.set(u, v, 0);
    }
}
