package thalmiclabs.myoarmband;

import java.util.ArrayList;

public class MapAndFetchPoses
{
    private DatabaseHandler db;

    MapAndFetchPoses(DatabaseHandler db)
    {
        this.db = db;
        mapPoses();
        dummyRealPassPose();
    }

    public void mapPoses()
    {
        PassposeMapping p1 = new PassposeMapping(0, "FIST");
        PassposeMapping p2 = new PassposeMapping(1, "WAVE_IN");
        PassposeMapping p3 = new PassposeMapping(2, "WAVE_OUT");
        PassposeMapping p4 = new PassposeMapping(3, "FINGER_SPREAD");
        db.addPoseMapping(p1);
        db.addPoseMapping(p2);
        db.addPoseMapping(p3);
        db.addPoseMapping(p4);
    }

    public ArrayList<Integer> getRealPassPose()
    {
        ArrayList<Integer> passPose = db.getAllPasspose();
        return passPose;
    }

    public void setRealPassPose(ArrayList<Integer> realPassPose)
    {
        db.getWritableDatabase().execSQL("delete from " + "passpose");
        for(int i=0;i<realPassPose.size();i++)
        {
            db.addPasspose(new Passpose(i, (int)realPassPose.get(i)));
        }
    }

    public void dummyRealPassPose()
    {
        ArrayList<Integer> passpose = new ArrayList<Integer>();
        passpose.add(0);
        passpose.add(1);
        passpose.add(2);
        passpose.add(3);
        setRealPassPose(passpose);
    }
}
