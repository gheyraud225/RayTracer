public class Vec3 {
    double x;
    double y;
    double z;

    public Vec3(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3 add(Vec3 OtherVec3){
        return new Vec3(this.x + OtherVec3.x, this.y + OtherVec3.y, this.z + OtherVec3.z);
    }
    public Vec3 inverse(){
        return new Vec3(-this.x,-this.y,-this.z);
    }
    public Vec3 sub(Vec3 v){
        return this.add(v.inverse());
    }
    public Vec3 multipl(double a){
        return new Vec3(this.x*a, this.y*a,this.z*a);
    }
    public Vec3 multVec(Vec3 v) {
        return new Vec3(this.x * v.x, this.y * v.y, this.z * v.z);
    }
    public double dotProduct(Vec3 v){
        return (this.x*v.x + this.y*v.y + this.z*v.z);
    }
    public Vec3 crossProduct(Vec3 v){
        return new Vec3(this.y*v.z- this.z*v.y, this.z*v.x - this.x*v.z, this.x*v.y-this.y*v.x);
    }
    public Vec3 toUnit(){
        double norm = Math.sqrt(this.dotProduct(this));
        if(norm < 1e-10){
            return this.multipl(0.0);
        }
        return this.multipl(1.0/norm);
    }

    // methode pour optimisier la classe
    public Vec3(){ // constructeur rapide et sans GC
        this.x = 0; this.y = 0; this.z = 0;
    }
    public void set(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void set(Vec3 v){
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }
    public void addInPlace(Vec3 v){
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }
    public void subInPlace(Vec3 v){
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }
    public void multInPlace(double a) {
        this.x *= a;
        this.y *= a;
        this.z *= a;
    }
    public void multVecInPlace(Vec3 v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
    }
    public void inverseInPlace() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }
    public void toUnitInPlace() {
        double lengthSquared = this.dotProduct(this);
        if(lengthSquared <1e-10){
            return;
        }
        double length = Math.sqrt(lengthSquared);
        this.multInPlace(1.0 / length);
    }
}
