public class Rayon {
    Vec3 Origine = new Vec3();
    Vec3 D = new Vec3();
    public Rayon(Vec3 O, Vec3 D){
        this.Origine = O;
        this.D = D.toUnit();
    }
    public void at(double t, Vec3 out_result) {
        out_result.set(this.D);      // 1. Commence avec la direction
        out_result.multInPlace(t);   // 2. Multiplie par t
        out_result.addInPlace(this.Origine); // 3. Ajoute l'origine
    }

    //methode rapide
    public Rayon(){   }
    public void set(Vec3 origin, Vec3 direction) {
        this.Origine.set(origin);
        this.D.set(direction);
    }

}
