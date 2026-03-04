import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;


class RenderTask implements Runnable{

    BufferedImage image;
    int j_start, j_end, imageWidth, imageHeight, samples_per_pixel;
    double aspectRatio, aperture, focal_distance;
    Vec3 cameraOrigin;
    int[] outputBuffer;
    int depth = 20;


    public RenderTask(int[] pixelData, int j_start, int j_end){
        this.outputBuffer = pixelData;
        this.image = Main.image;
        this.j_start = j_start;
        this.j_end = j_end;
        this.imageWidth = Main.imageWidth;
        this.imageHeight = Main.imageHeight;
        this.samples_per_pixel = Main.samples_per_pixel;
        this.aspectRatio = Main.aspectRatio;
        this.aperture = Main.aperture;
        this.focal_distance = Main.focal_distance;
        this.cameraOrigin = Main.cameraOrigin;
    }

    @Override
    public void run(){

        RenderScratchpad scratch = Main.scratchpad.get();

        int[] localBuffer = new int[(j_end-j_start)*imageWidth];

        for(int j = j_start; j < j_end; j++){
            for(int i = 0; i < imageWidth; i++){
                scratch.sumColor.set(0,0,0);

                for(int s = 0; s < samples_per_pixel ; s++) {
                    double u = ((i + ThreadLocalRandom.current().nextDouble()) - imageWidth / 2.0) / imageWidth * aspectRatio;
                    double v = (imageHeight / 2.0 - j - ThreadLocalRandom.current().nextDouble()) / imageHeight;

                    scratch.pointSurEcran.set(u, v, 1.0);
                    scratch.rayDirection.set(scratch.pointSurEcran);
                    scratch.rayDirection.toUnitInPlace();

                    scratch.r.set(cameraOrigin, scratch.rayDirection);
                    Main.calculateColor(scratch.r, depth, scratch.pixelColor);
                    scratch.sumColor.addInPlace(scratch.pixelColor);
                }
                scratch.sumColor.multInPlace(1.0 / samples_per_pixel);

                double gamma = 2.2;
                double r_gamma = Math.pow(scratch.sumColor.x, 1.0/gamma);
                double g_gamma = Math.pow(scratch.sumColor.y, 1.0/gamma);
                double b_gamma = Math.pow(scratch.sumColor.z, 1.0/gamma);

                float r_final = (float)Math.min(1.0, Math.max(0.0, r_gamma));
                float g_final = (float)Math.min(1.0, Math.max(0.0, g_gamma));
                float b_final = (float)Math.min(1.0, Math.max(0.0, b_gamma));
                Color color1 = new Color(r_final,g_final,b_final);
                int rgb = color1.getRGB();
                int index = (j-j_start)*imageWidth + i;
                localBuffer[index] = rgb;
            }
        }
        int globalStartIndex = j_start * imageWidth;

        // Copie le petit tableau local dans le grand tableau partagé
        System.arraycopy(
                localBuffer,      // Source
                0,                // Index de départ source
                outputBuffer,     // Destination
                globalStartIndex, // Index de départ destination
                localBuffer.length // Longueur à copier
        );
    }
}
