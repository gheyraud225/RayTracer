// Nouveau fichier : ImageTexture.java
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageTexture implements Texture {
    private BufferedImage image;
    private int width;
    private int height;

    public ImageTexture(String filename) {
        try {
            // Charge l'image depuis le fichier
            this.image = ImageIO.read(new File(filename));
            this.width = image.getWidth();
            this.height = image.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERREUR: Impossible de charger le fichier texture: " + filename);
            // Crée une image par défaut (rose vif) pour signaler l'erreur
            this.image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            this.image.setRGB(0, 0, 0xFFFF00FF); // Rose
            this.width = 1;
            this.height = 1;
        }
    }

    @Override
    public void value(double u, double v, Vec3 p, Vec3 out_color) {
        // --- C'est ici que le "UV Mapping" se connecte ---

        // 1. Gérer l'enroulement (wrap) de la texture
        // (Assure que U et V sont toujours entre 0.0 et 1.0)
        u = u - Math.floor(u);
        v = 1.0 - (v - Math.floor(v)); // Inverse V car les images (0,0) sont en HAUT à gauche

        // 2. Convertir les UV (0..1) en coordonnées pixel (0..width)
        int i = (int)(u * width);
        int j = (int)(v * height);

        // 3. Sécurité (Clamp) : évite de dépasser les bords
        if (i >= width)  i = width - 1;
        if (j >= height) j = height - 1;
        if (i < 0) i = 0;
        if (j < 0) j = 0;

        // 

        // 4. Lire le pixel (entier RGB "empaqueté")
        int rgb = image.getRGB(i, j);

        // 5. "Dépaqueter" l'entier en composantes R, G, B (0..255)
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb >> 0) & 0xFF;

        // 6. Convertir en (0..1) et stocker dans le Vec3 de sortie
        out_color.set(r / 255.0, g / 255.0, b / 255.0);
    }
}