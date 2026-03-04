public interface SceneObject {
    public double hit(Rayon r, double t_min, double t_max);
    public Vec3 getNormalAt(Vec3 pointImpact);
    public Material getMaterial();
    void getUV(Vec3 p, Vec3 out_uv);
}
