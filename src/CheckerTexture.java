public class CheckerTexture implements Texture {
    private Texture even; // Couleur des cases paires
    private Texture odd;  // Couleur des cases impaires
    private double scale; // Pour ajuster la taille du damier

    public CheckerTexture(Texture even, Texture odd, double scale) {
        this.even = even;
        this.odd = odd;
        this.scale = scale;
    }

    @Override
    public void value(double u, double v, Vec3 p, Vec3 out_color) {
        // On utilise le point 3D 'p', pas les UV
        double sines = Math.sin(scale * p.x) * Math.sin(scale * p.y) * Math.sin(scale * p.z);
        if (sines < 0) {
            odd.value(u, v, p, out_color);
        } else {
            even.value(u, v, p, out_color);
        }
    }
}