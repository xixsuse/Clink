package com.testlabic.datenearu.Activities;

import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stepstone.stepper.StepperLayout;
import com.testlabic.datenearu.R;
import com.testlabic.datenearu.Utils.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class EditSeriousItems extends AppCompatActivity {

    private static final String TAG = EditSeriousItems.class.getSimpleName();
    private TextView option1, option2, option3, option4, option5, option6, choice1, choice2, choice3, choice4, choice5, choice6;
    public CharSequence dragData;
    Button save;
    Typeface normal, bold;
    Map<Integer, String> order = new TreeMap<>();
    int count =0;
    View rootView;
    LinearLayout choiceLL;
    private SweetAlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_serious_items);

        choiceLL = findViewById(R.id.choiceLL);
        save=findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveseriousitems();
            }
        });

        bold = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/SF-Pro-Display-Bold.otf");
        normal = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/SF-Pro-Display-Light.otf");
        //get both sets of text views
        dialog =  new SweetAlertDialog(EditSeriousItems.this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("In progress")
                .setContentText(".....");


        //views to drag
        option1 = (TextView)findViewById(R.id.option_1);
        option2 = (TextView)findViewById(R.id.option_2);
        option3 = (TextView)findViewById(R.id.option_3);
        option4 = (TextView)findViewById(R.id.option_4);
        option5 = (TextView)findViewById(R.id.option_5);
        option6 = (TextView)findViewById(R.id.option_6);

        //views to drop onto
        choice1 = (TextView)findViewById(R.id.choice_1);
        choice2 = (TextView)findViewById(R.id.choice_2);
        choice3 = (TextView)findViewById(R.id.choice_3);
        choice4 = (TextView)findViewById(R.id.choice_4);
        choice5 = (TextView)findViewById(R.id.choice_5);
        choice6 = (TextView)findViewById(R.id.choice_6);

        //set touch listeners
        option1.setOnTouchListener(new EditSeriousItems.ChoiceTouchListener());
        option2.setOnTouchListener(new EditSeriousItems.ChoiceTouchListener());
        option3.setOnTouchListener(new EditSeriousItems.ChoiceTouchListener());
        option4.setOnTouchListener(new EditSeriousItems.ChoiceTouchListener());
        option5.setOnTouchListener(new EditSeriousItems.ChoiceTouchListener());
        option6.setOnTouchListener(new EditSeriousItems.ChoiceTouchListener());

        //set drag listeners
        choice1.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice2.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice3.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice4.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice5.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice6.setOnDragListener(new EditSeriousItems.ChoiceDragListener());

        ImageView reset = findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("NewApi")



    public void saveseriousitems() {
        if(count==6) {
            dialog.show();
            // Toast.makeText(getActivity(), "All done go! "+count, Toast.LENGTH_SHORT).show();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.userInfo)
                    .child(Constants.uid)
                    .child("matchAlgo");

            String seq = "";
            //getting values from treemap
            for(int i=1; i<7; i++)
                seq += order.get(i);

            //Log.e(TAG, "The sequence is "+seq);
            final String finalSeq = seq;

            DatabaseReference prefRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.userPreferences)
                    .child(Constants.uid)
                    .child("matchAlgo");
            prefRef.setValue(seq);

            reference.setValue(seq).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences(Constants.userDetailsOff, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constants.matchAlgo, finalSeq).apply();
                    dialog.dismiss();
                    finish();

                    //UpdateXPoints();
                    //DuplicateUserInfo();

                }
            });
        }
        else
            Toast.makeText(this, "Order all items priority wise", Toast.LENGTH_SHORT).show();
    }
    /**
     * ChoiceTouchListener will handle touch events on draggable views
     *
     */
    private final class ChoiceTouchListener implements View.OnTouchListener {
        @SuppressLint("NewApi")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                /*
                 * Drag details: we only need default behavior
                 * - clip data could be set to pass data as part of drag
                 * - shadow can be tailored
                 */
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                //start dragging the item touched
                view.startDrag(data, shadowBuilder, view, 0);
                return true;
            } else {
                return false;
            }
        }

    }
    /**
     * DragListener will handle dragged views being dropped on the drop area
     * - only the drop action will have processing added to it as we are not
     * - amending the default behavior for other parts of the drag process
     *
     */
    @SuppressLint("NewApi")
    private class ChoiceDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    //no action necessary
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //no action necessary
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    //no action necessary
                    break;
                case DragEvent.ACTION_DROP:

                    //handle the dragged view being dropped over a drop view
                    View view = (View) event.getLocalState();
                    //view dragged item is being dropped on
                    TextView dropTarget = (TextView) v;
                    //view being dragged and dropped
                    TextView dropped = (TextView) view;
                    //checking whether first character of dropTarget equals first character of dropped
                    //if(dropTarget.getText().toString().charAt(0) == dropped.getText().toString().charAt(0))
                {
                    //stop displaying the view where it was before it was dragged
                    view.setVisibility(View.GONE);
                    //update the text in the target view to reflect the data being dropped
                    char alpha = (char)dropTarget.getText().charAt(0);
                    char num = dropped.getText().charAt(0);
                    dropTarget.setText(dropped.getText().toString()+" - "+dropTarget.getText().toString());
                    order.put(Integer.parseInt(String.valueOf(num)), String.valueOf(alpha));
                    //Toast.makeText(getActivity(), "The match is "+num+" "+alpha,Toast.LENGTH_SHORT).show();
                    count++;
                    if(count==6)
                        choiceLL.setVisibility(View.GONE);
                    //make it bold to highlight the fact that an item has been dropped
                    dropTarget.setTypeface(bold);
                    //if an item has already been dropped here, there will be a tag
                    Object tag = dropTarget.getTag();
                    //if there is already an item here, set it back visible in its original place
                    if(tag!=null)
                    {
                        //the tag is the view id already dropped here
                        int existingID = (Integer)tag;
                        //set the original view visible again
                        findViewById(existingID).setVisibility(View.VISIBLE);
                    }
                    //set the tag in the target view being dropped on - to the ID of the view being dropped
                    dropTarget.setTag(dropped.getId());
                    //remove setOnDragListener by setting OnDragListener to null, so that no further drag & dropping on this TextView can be done
                    dropTarget.setOnDragListener(null);
                }
                //  else
                //displays message if first character of dropTarget is not equal to first character of dropped
                //  Toast.makeText(ArrangingItems.this, dropTarget.getText().toString() + "is not " + dropped.getText().toString(), Toast.LENGTH_LONG).show();
                break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //no action necessary
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    public void reset()
    {
        choiceLL.setVisibility(View.VISIBLE);
        count =0;
        option1.setVisibility(TextView.VISIBLE);
        option2.setVisibility(TextView.VISIBLE);
        option3.setVisibility(TextView.VISIBLE);
        option4.setVisibility(TextView.VISIBLE);
        option5.setVisibility(TextView.VISIBLE);
        option6.setVisibility(TextView.VISIBLE);

        choice1.setText("A) The eye catcher:Physical features");
        choice2.setText("B) I am Sapiosexual (intelligence n intellectual ability)");
        choice3.setText("C) Must be helpful, caring and compassionate towards you and society");
        choice4.setText("D) Has an ambition in life and is serious towards it");
        choice5.setText("E) has proved his/her metal (secured a good job)");
        choice6.setText("F) Understands you and your nature.");

        choice1.setTag(null);
        choice2.setTag(null);
        choice3.setTag(null);
        choice4.setTag(null);
        choice5.setTag(null);
        choice6.setTag(null);


        choice1.setTypeface(normal);
        choice2.setTypeface(normal);
        choice3.setTypeface(normal);
        choice4.setTypeface(normal);
        choice5.setTypeface(normal);
        choice6.setTypeface(normal);

        choice1.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice2.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice3.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice4.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice5.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
        choice6.setOnDragListener(new EditSeriousItems.ChoiceDragListener());
    }
}

