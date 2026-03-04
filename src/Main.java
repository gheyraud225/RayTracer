import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static List<SceneObject> scene = new ArrayList<>();
    public static Vec3 lightPos = new Vec3(-5,5,-5);
    public static Vec3 cameraOrigin= new Vec3(0.0,0.0,0.0);
    public static double focal_distance = 5.0;
    public static double aperture = 0.0;


    public static int imageWidth = 800;
    public static int imageHeight = 600;
    public static double aspectRatio = (double) imageWidth / (double) imageHeight;
    public static BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

    public static int samples_per_pixel = 1024;
    public static double epsilon = 0.00001;



    public static void main(String[] args) {


        long startTime = System.nanoTime();

        // Crée les textures
        Texture texSol = new ImageTexture("wood.png"); // <-- Charge ton image !
        Texture texVerre = new SolidColor(1, 1, 1);

        // Crée les matériaux
        Material matVerre = new Material("glass", texVerre, 1.5);
        Material matSol = new Material("diffuse", texSol, 1.0); // Le sol utilise la texture image

        // objet présent
        scene.add(new Sphere(new Vec3(0,0,5),1.0, matVerre));
        scene.add(new Plan(new Vec3(0,-1,0), new Vec3(0,1,0), matSol)); // Applique le matériau au plan


        int[] outputBuffer = new int[imageHeight*imageWidth];

        int numThreads = 20;
        //int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("Démarage du rendu avec " + numThreads + " threads...");

        List<Thread> threads = new ArrayList<>();

        int chunkSize = imageHeight/numThreads;

        for(int t = 0; t < numThreads; t++){
            int j_start = t * chunkSize;
            int j_end = (t == numThreads-1) ? imageHeight : (1+t)* chunkSize;

            Runnable task = new RenderTask(outputBuffer, j_start, j_end);
            Thread thread = new Thread(task);
            thread.start();
            threads.add(thread);
        }
        try{
            System.out.println("Attente de la fin des threads...");
            for(Thread thread : threads){
                thread.join();
            }
        }catch(InterruptedException e ){
            e.printStackTrace();
        }
        image.getRaster().setDataElements(0, 0, imageWidth, imageHeight, outputBuffer);
        System.out.println("Rendu terminé");
        long endTime = System.nanoTime(); // Arrêter le chrono
        long durationMs = (endTime - startTime) / 1000000; // Convertir en millisecondes
        System.out.println("TEMPS (" + numThreads + " thread) : " + durationMs + " ms");
        System.out.println("------------------------------------");
        try {
            ImageIO.write(image, "png",new File("Render.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static ThreadLocal<RenderScratchpad> scratchpad =
            ThreadLocal.withInitial(RenderScratchpad::new);

    public static void calculateColor(Rayon r, int depth, Vec3 out_color){

        RenderScratchpad scratch = scratchpad.get();

        if(depth <= 0){
            out_color.set(0,0,0);
            return;
            //fini
        }
        double t_closest = Double.POSITIVE_INFINITY;
        SceneObject objet_touche = null;
        for(SceneObject  obj : scene){
            double t = obj.hit(r,0.0001, Double.POSITIVE_INFINITY);
            if(t > 0 && t < t_closest){
                t_closest = t;
                objet_touche = obj;
            }
        }
        if(objet_touche == null){
            // Crée un dégradé pour le ciel
            // 1. Normalise la direction du rayon
            scratch.L.set(r.D);
            scratch.L.toUnitInPlace();
            // 2. Calcule 't' (0.0 pour l_horizon, 1.0 pour le zénith)
            double t = 0.5 * (scratch.L.y + 1.0);
            // 3. Fait un mélange linéaire (lerp) entre blanc et bleu ciel
            // Couleur = (1-t)*Blanc + t*BleuCiel

            // Vec3 blanc = new Vec3(1.0, 1.0, 1.0);
            // Vec3 bleuCiel = new Vec3(0.5, 0.7, 1.0);

            // On le fait "in-place"
            scratch.albedo.set(1.0, 1.0, 1.0); // Blanc
            scratch.albedo.multInPlace(1.0 - t);

            scratch.L.set(0.5, 0.7, 1.0); // Bleu Ciel
            scratch.L.multInPlace(t);

            scratch.albedo.addInPlace(scratch.L);
            out_color.set(scratch.albedo);
            return;
        }

        r.at(t_closest, scratch.pointImpact);
        scratch.normal.set(objet_touche.getNormalAt(scratch.pointImpact));
        Material mat = objet_touche.getMaterial();
        objet_touche.getUV(scratch.pointImpact, scratch.uv);
        mat.albedo.value(scratch.uv.x, scratch.uv.y, scratch.pointImpact, scratch.albedo);


        if(mat.type.equals("mirror")){
            double dot_DN = r.D.dotProduct(scratch.normal);

            // 1. Calcul de R (ton code est correct et malin)
            scratch.reflectedRay.D.set(scratch.normal);
            scratch.reflectedRay.D.multInPlace(2.0 * dot_DN);
            scratch.reflectedRay.D.subInPlace(r.D);
            scratch.reflectedRay.D.inverseInPlace();

            // 2. Calcul de l'origine du rayon (SANS ALLOCATION)
            scratch.L.set(scratch.normal);          // Utilise L comme temporaire
            scratch.L.multInPlace(epsilon);         // L = normal * epsilon

            scratch.shadowRay.Origine.set(scratch.pointImpact); // Réutilise shadowRay.Origine
            scratch.shadowRay.Origine.addInPlace(scratch.L); // 3. Origine = Origine + L    // Origine = pointImpact + (normal * epsilon)

            // 3. Lancer le rayon
            scratch.reflectedRay.set(scratch.shadowRay.Origine, scratch.reflectedRay.D);
            calculateColor(scratch.reflectedRay, depth - 1, out_color);
            return;
        }
        else if(mat.type.equals("glass")){
            // 1. Gérer l'entrée/sortie et les normales (Ton code est correct)
            double dot_DN_original = r.D.dotProduct(scratch.normal);
            double n1, n2;
            if (dot_DN_original > 0) {
                scratch.normal.inverseInPlace();
                n1 = mat.ior;
                n2 = 1.0;
            } else {
                n1 = 1.0;
                n2 = mat.ior;
            }

            // 2. Calculer le rayon RÉFLÉCHI (toujours nécessaire)
            double cos_theta_in = Math.min(-r.D.dotProduct(scratch.normal), 1.0);

            // Calcul de la direction R (Ton code est correct)
            scratch.reflectedRay.D.set(scratch.normal);
            scratch.reflectedRay.D.multInPlace(2.0 * r.D.dotProduct(scratch.normal));
            scratch.reflectedRay.D.subInPlace(r.D);
            scratch.reflectedRay.D.inverseInPlace();

            // --- CORRECTION 1 : Utilise 'shadowRay.Origine' pour le REFLET ---
            scratch.L.set(scratch.normal);
            scratch.L.multInPlace(epsilon);
            scratch.shadowRay.Origine.set(scratch.pointImpact);
            scratch.shadowRay.Origine.addInPlace(scratch.L);
            scratch.reflectedRay.set(scratch.shadowRay.Origine, scratch.reflectedRay.D); // OK

            // Appel récursif pour la couleur du reflet
            calculateColor(scratch.reflectedRay, depth - 1, scratch.reflectedColor);

            // 3. Calculer le rayon RÉFRACTÉ (Loi de Snell)
            double eta = n1 / n2;
            double sin2_theta_out = eta * eta * (1.0 - cos_theta_in * cos_theta_in);

            if (sin2_theta_out > 1.0) {
                // Réflexion Totale Interne
                out_color.set(scratch.reflectedColor);
                return;
            }

            // Calcul de la direction T (Ton code est correct)
            double cos_theta_out = Math.sqrt(1.0 - sin2_theta_out);
            scratch.refractedRay.D.set(r.D);
            scratch.refractedRay.D.multInPlace(eta);
            scratch.L.set(scratch.normal);
            scratch.L.multInPlace(eta * cos_theta_in - cos_theta_out);
            scratch.refractedRay.D.addInPlace(scratch.L);
            scratch.refractedRay.D.toUnitInPlace();

            // --- CORRECTION 2 : Utilise 'refractedRay.Origine' pour la RÉFRACTION ---
            scratch.L.set(scratch.normal);
            scratch.L.multInPlace(epsilon);
            scratch.refractedRay.Origine.set(scratch.pointImpact); // Utilise l'origine propre au rayon
            scratch.refractedRay.Origine.subInPlace(scratch.L);
            // 'scratch.refractedRay' a maintenant sa propre origine ET direction

            // Appel récursif pour la couleur de la réfraction
            calculateColor(scratch.refractedRay, depth - 1, scratch.refractedColor);

            // 4. Mélange de Fresnel (Ton code est correct)
            double R0 = ((n1 - n2) / (n1 + n2)) * ((n1 - n2) / (n1 + n2));
            double reflectance = R0 + (1 - R0) * Math.pow((1 - cos_theta_in), 5);

            // 5. Couleur finale (Ton code est correct)
            scratch.reflectedColor.multInPlace(reflectance);
            scratch.refractedColor.multInPlace(1.0 - reflectance);
            out_color.set(scratch.reflectedColor);
            out_color.addInPlace(scratch.refractedColor);
            return;
        }else{ // lumiere diffuse, on ajoute le pathtracing

            // 1. Lumière directe
            // Calcule la direction de la lumière et stocke-la dans scratch.L
            scratch.L.set(lightPos);
            scratch.L.subInPlace(scratch.pointImpact);
            scratch.L.toUnitInPlace(); // scratch.L est maintenant correct

            // --- CORRECTION DU BUG ---
            // On calcule l'offset SANS écraser scratch.L
            // On utilise D_local comme temporaire
            scratch.D_local.set(scratch.normal);
            scratch.D_local.multInPlace(epsilon);

            // Calcule l'origine du rayon d'ombre
            scratch.shadowRay.Origine.set(scratch.pointImpact);
            scratch.shadowRay.Origine.addInPlace(scratch.D_local); // Origine = pointImpact + (normal * epsilon)

            // Le rayon d'ombre utilise la VRAIE direction de la lumière
            scratch.shadowRay.set(scratch.shadowRay.Origine, scratch.L);
            // --- FIN DE LA CORRECTION ---

            boolean est_a_l_ombre = false;
            for (SceneObject obj_ombre : scene) {
                double t_ombre = obj_ombre.hit(scratch.shadowRay, 0.001, Double.POSITIVE_INFINITY);
                if (t_ombre > 0) {
                    est_a_l_ombre = true;
                    break;
                }
            }

            if (est_a_l_ombre) {
                scratch.lumiereDirecte.set(0, 0, 0); // Ombre
            } else {
                double intensite = Math.max(0, scratch.normal.dotProduct(scratch.L));
                scratch.lumiereDirecte.set(scratch.albedo);
                scratch.lumiereDirecte.multInPlace(intensite);
            }

            // 2. Lumière indirecte (Rebond)
            genererRebondAleatoire(scratch.normal, scratch.D_world); // D_world est le out-param

            // On réutilise D_local et shadowRay.Origine pour le rayon de rebond
            scratch.D_local.set(scratch.normal);
            scratch.D_local.multInPlace(epsilon);
            scratch.shadowRay.Origine.set(scratch.pointImpact);
            scratch.shadowRay.Origine.addInPlace(scratch.D_local);

            scratch.refractedRay.set(scratch.shadowRay.Origine, scratch.D_world); // Réutilise un autre rayon

            calculateColor(scratch.refractedRay, depth - 1, scratch.couleurRebond);

            scratch.couleurRebond.multVecInPlace(scratch.albedo); // albedo * lumiere_indirecte

            // 3. Total
            out_color.set(scratch.lumiereDirecte);
            out_color.addInPlace(scratch.couleurRebond);
            return;
        }

    }

    public static void genererRebondAleatoire(Vec3 normal, Vec3 out_D_world){

        RenderScratchpad scratch = scratchpad.get();

        // Dans genererRebondAleatoire :
        double r1 = java.util.concurrent.ThreadLocalRandom.current().nextDouble();
        double r2 = java.util.concurrent.ThreadLocalRandom.current().nextDouble();

        double phi = 2.0 * Math.PI * r1;
        double cos_theta = Math.sqrt(r2);
        double sin_theta = Math.sqrt(1.0 - cos_theta * cos_theta);

        double x = Math.cos(phi) * sin_theta;
        double y = Math.sin(phi) * sin_theta;
        double z = cos_theta;

        scratch.D_local.set(x, y, z);

        scratch.W.set(normal); // toUnit n'est pas nécessaire si normal l'est déjà

        if (Math.abs(scratch.W.x) > 0.9) {
            scratch.A.set(0, 1, 0);
        } else {
            scratch.A.set(1, 0, 0);
        }
        scratch.U.set(scratch.A.crossProduct(scratch.W)); // crossProduct() crée un new Vec3
        scratch.U.toUnitInPlace();
        scratch.V.set(scratch.W.crossProduct(scratch.U));

        Vec3 resU = scratch.U.multipl(scratch.D_local.x); // Alloue 1 Vec3
        Vec3 resV = scratch.V.multipl(scratch.D_local.y); // Alloue 1 Vec3
        Vec3 resW = scratch.W.multipl(scratch.D_local.z); // Alloue 1 Vec3

        out_D_world.set(resU);
        out_D_world.addInPlace(resV);
        out_D_world.addInPlace(resW);
        out_D_world.toUnitInPlace();
    }
}