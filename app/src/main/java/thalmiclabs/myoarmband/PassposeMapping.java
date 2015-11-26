package thalmiclabs.myoarmband;

public class PassposeMapping {

    //private variables
    int _id;
    String _pose;

    // Empty constructor
    public PassposeMapping(){

    }
    // constructor
    public PassposeMapping(int id, String pose){
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

    // getting name
    public String getPose(){
        return this._pose;
    }

    // setting name
    public void setPose(String pose){
        this._pose = pose;
    }
}