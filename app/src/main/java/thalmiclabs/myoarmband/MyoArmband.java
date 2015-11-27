/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package thalmiclabs.myoarmband;

import thalmiclabs.myoarmband.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class MyoArmband extends Activity {

    private TextView mLockStateView;
    private TextView mTextView;
    private ArrayList<Integer> realPasspose;
    private ArrayList<Integer> passposePerformed;
    private int maxLength = 20;
    private boolean locked = true;
    private HashMap<String, Integer> passPoseMapping;
    private boolean takingPassPose;
    private Button passPoseToggle;
    private MapAndFetchPoses mapAndFetchPoses;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mTextView.setTextColor(Color.CYAN);
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            mTextView.setTextColor(Color.RED);
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mTextView.setText(R.string.hello_world);
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            passposePerformed = new ArrayList<>();
            myo.unlock(Myo.UnlockType.HOLD);
            locked = false;
            mLockStateView.setText(R.string.unlocked);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            if(!takingPassPose) {
                matchPasspose(passposePerformed, realPasspose);
            }
            else {
                mapAndFetchPoses.setRealPassPose(passposePerformed);
                realPasspose = mapAndFetchPoses.getRealPassPose();
                mTextView.setText("Passpose changed");
                Log.i("band", realPasspose.toString());
                takingPassPose = false;
            }
            mLockStateView.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }

            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            /*mTextView.setRotation(roll);
            mTextView.setRotationX(pitch);
            mTextView.setRotationY(yaw);*/
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.

            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
            TextView newTextView = new TextView(getApplicationContext());
            newTextView.setTextColor(Color.BLACK);
            switch (pose) {
                case UNKNOWN:
                    newTextView.setText(getString(R.string.hello_world));
                    myo.unlock(Myo.UnlockType.HOLD);
                    break;
                case REST:
                    break;
                case DOUBLE_TAP:
                    newTextView.setText("Double Tap");
                    newTextView.setTextColor(Color.RED);
                    linearLayout.addView(newTextView);
                    if(locked)
                    {
                        myo.unlock(Myo.UnlockType.HOLD);
                        locked = false;
                        Log.i("band", "Unlocked");
                    }
                    else {
                        myo.lock();
                        Log.i("band", "Locked");
                        locked = true;
                    }
                    int restTextId = R.string.hello_world;
                    switch (myo.getArm()) {
                        case LEFT:
                            restTextId = R.string.arm_left;
                            break;
                        case RIGHT:
                            restTextId = R.string.arm_right;
                            break;
                    }
                    //newTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    if(passposePerformed.size() == 0 || passposePerformed.get(passposePerformed.size()-1) != 0) {
                        newTextView.setText(getString(R.string.pose_fist));
                        linearLayout.addView(newTextView);
                        passposePerformed.add(0);
                    }
                    else
                    {
                        newTextView.setText("Repeated");
                    }
                    break;
                case WAVE_IN:
                    if(passposePerformed.size() == 0 || passposePerformed.get(passposePerformed.size()-1) != 1) {
                        newTextView.setText(getString(R.string.pose_wavein));
                        linearLayout.addView(newTextView);
                        passposePerformed.add(1);
                    }
                    else
                    {
                        newTextView.setText("Repeated");
                    }
                    break;
                case WAVE_OUT:
                    if(passposePerformed.size() == 0 || passposePerformed.get(passposePerformed.size()-1) != 2) {
                        newTextView.setText(getString(R.string.pose_waveout));
                        linearLayout.addView(newTextView);
                        passposePerformed.add(2);
                    }
                    else
                    {
                        newTextView.setText("Repeated");
                    }
                    break;
                case FINGERS_SPREAD:
                    if(passposePerformed.size() == 0 || passposePerformed.get(passposePerformed.size()-1) != 3) {
                        newTextView.setText(getString(R.string.pose_fingersspread));
                        linearLayout.addView(newTextView);
                        passposePerformed.add(3);
                    }
                    else
                    {
                        newTextView.setText("Repeated");
                    }
                    break;
            }
            if(newTextView.getText().equals("Double Tap"));
            else if(takingPassPose) {
                newTextView.setTextColor(Color.BLUE);
            }
            else
            {
                newTextView.setTextColor(Color.BLACK);
            }
            newTextView.setMovementMethod(new ScrollingMovementMethod());
            ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
            scrollView.scrollTo(0, scrollView.getBottom());
            Log.i("band", newTextView.getText().toString());
            /*if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }*/
        }
    };


    public void matchPasspose(ArrayList<Integer> passposePerformed, ArrayList<Integer> realPasspose)
    {
        int index = 0;
        boolean correctPasspose = false;
        for(int i=0;i<passposePerformed.size();i++)
        {
            if(passposePerformed.get(i) == realPasspose.get(index))
            {
                index++;
                if(index == realPasspose.size()) {
                    correctPasspose = true;
                    break;
                }
            }
        }
        if(correctPasspose)
        {
            TextView textView = (TextView)findViewById(R.id.text);
            textView.setText("Unlocked Successfully");
            textView.setTextColor(Color.GREEN);
        }
        else{
            TextView textView = (TextView)findViewById(R.id.text);
            textView.setText("Incorrect Passpose");
            textView.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);
        passPoseToggle = (Button) findViewById(R.id.set_button);
        takingPassPose = false;
        DatabaseHandler db = new DatabaseHandler(this);
        mapAndFetchPoses = new MapAndFetchPoses(db);
        realPasspose = mapAndFetchPoses.getRealPassPose();
        Log.i("band", realPasspose.toString());
        passPoseToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!takingPassPose) {
                    takingPassPose = true;
                    mTextView.setText("Taking passpose");
                    passposePerformed = new ArrayList<Integer>();
                } else {
                    takingPassPose = false;
                    mTextView.setText("Passpose saved");
                }
            }
        });
        /*realPasspose = new ArrayList<>();
        realPasspose.add(0); //FIST
        realPasspose.add(1); //WAVE_IN
        realPasspose.add(2); //Wave_OUT
        realPasspose.add(3); //FINGERS_SPREAD*/

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent intent = new Intent(this, ScanActivity.class);
        this.startActivity(intent);

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
}
