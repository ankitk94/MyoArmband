package thalmiclabs.myoarmband;

public class Passpose {

    //private variables
    int _id;
    int _pose;

    // Empty constructor
    public Passpose(){

    }
    // constructor
    public Passpose(int id, int pose){
        this._id = id;
        this._pose = pose;
    }

    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    public int getPose() {
        return this._pose;
    }

    public void setPose(int pose){
        this._pose = pose;
    }
}