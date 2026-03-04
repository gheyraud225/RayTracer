public class Material {

    String type;
    Texture albedo;
    double ior;

    public Material(String type, Texture vec3, double v) {
        this.type = type;
        this.albedo = vec3;
        this.ior = v;
    }
}
