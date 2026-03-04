public class SolidColor implements Texture{
    private  Vec3 colorValue;

    public SolidColor(Vec3 c) { this.colorValue = c; }
    public SolidColor(double r, double g, double b) { this.colorValue = new Vec3(r, g, b); }

    @Override
    public void value(double u, double v, Vec3 p, Vec3 out_color) {
        out_color.set(this.colorValue);
    }
}
