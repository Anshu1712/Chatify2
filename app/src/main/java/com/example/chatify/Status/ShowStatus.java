package com.example.chatify.Status;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatify.R;
import com.example.chatify.model.StatusModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ShowStatus extends AppCompatActivity implements StoriesProgressView.StoriesListener {


    List<String> image;
    List<String> uid;
    List<String> caption;
    List<String> delete;
    List<String> time;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    StatusModel model;
    StoriesProgressView storiesProgressView;
    String userId;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference statusRef, lastStatus;
    int counter = 0;
    ImageView s_iv, userIv;
    TextView tvName, StoryViewTv, captionTv, timeTv;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_status);

        storiesProgressView = findViewById(R.id.stories);
        s_iv = findViewById(R.id.iv_status_shows);
        userIv = findViewById(R.id.iv_au);

        tvName = findViewById(R.id.tv_uname_ss);
        StoryViewTv = findViewById(R.id.statusCount);
        captionTv = findViewById(R.id.StoryCap_tv);
        timeTv = findViewById(R.id.tv_time_ss);

        lastStatus = database.getReference("laststatus");

        model = new StatusModel();
        View reverse = findViewById(R.id.viewnext);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });

        reverse.setOnTouchListener(onTouchListener);

        reverse.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                storiesProgressView.pause();
                return false;
            }
        });

        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.viewprev);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        skip.setOnTouchListener(onTouchListener);


        skip.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                storiesProgressView.pause();
                return false;
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getString("uid");
        } else {

        }
        statusRef = database.getReference("Status").child(userId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            getstories(userId);
        } catch (Exception e) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    private void getstories(String userId) {
        image = new ArrayList<>();
        uid = new ArrayList<>();
        delete = new ArrayList<>();
        caption = new ArrayList<>();
        time = new ArrayList<>();

        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                image.clear();
                uid.clear();
                caption.clear();
                time.clear();
                delete.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    StatusModel statusModel = snapshot1.getValue(StatusModel.class);

                    long timecurrent = System.currentTimeMillis();

                    image.add(statusModel.getImage());
                    uid.add(statusModel.getUid());
                    time.add(statusModel.getTime());
                    caption.add(statusModel.getCaption());
                    delete.add(statusModel.getDelete());

                }

                storiesProgressView.setStoriesCount(image.size());
                storiesProgressView.setStoriesListener(ShowStatus.this);
                storiesProgressView.startStories(counter);
                storiesProgressView.setStoryDuration(5000L);


                s_iv.setVisibility(View.VISIBLE);
                captionTv.setText(caption.get(counter));
                Picasso.get().load(image.get(counter)).into(s_iv);

                timeTv.setText(time.get(counter));

                fetchuserinfo(uid.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void fetchuserinfo(String s) {

        documentReference = db.collection("Users").document(s);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    String name = task.getResult().getString("name");
                    String about = task.getResult().getString("about");
                    String phone = task.getResult().getString("phone");
                    String url = task.getResult().getString("url");

                    if (url.equals("")) {
                        tvName.setText(name);
                    } else {
                        tvName.setText(name);
                        Picasso.get().load(url).into(s_iv);
                    }
                } else {
                    Toast.makeText(ShowStatus.this, "no data found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onNext() {

    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0)return;
        Picasso.get().load(image.get(--counter)).into(s_iv);
        captionTv.setText(caption.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }
}