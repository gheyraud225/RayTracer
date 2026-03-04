public class RenderScratchpad {
    public final Vec3 sumColor = new Vec3();
    public final Vec3 pixelColor = new Vec3();
    public final Vec3 pointSurEcran = new Vec3();
    public final Vec3 rayDirection = new Vec3();
    public final Vec3 uv = new Vec3();
    public final Rayon r = new Rayon();
    public final Vec3 L = new Vec3();

    public final Vec3 pointImpact = new Vec3();
    public final Vec3 normal = new Vec3();
    public final Vec3 albedo = new Vec3();
    public final Vec3 lumiereDirecte = new Vec3();
    public final Vec3 couleurRebond = new Vec3();

    public final Rayon reflectedRay = new Rayon();
    public final Rayon refractedRay = new Rayon();
    public final Rayon shadowRay = new Rayon();
    public final Vec3 reflectedColor = new Vec3();
    public final Vec3 refractedColor = new Vec3();

    public final Vec3 D_local = new Vec3();
    public final Vec3 W = new Vec3();
    public final Vec3 A = new Vec3();
    public final Vec3 U = new Vec3();
    public final Vec3 V = new Vec3();
    public final Vec3 D_world = new Vec3();
}
