package com.example.chatify.Status;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatify.R;
import com.example.chatify.adapters.ViewListAdapter;
import com.example.chatify.adapters.ViewVH;
import com.example.chatify.model.StatusModel;
import com.example.chatify.model.ViewListModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;
import timber.log.Timber;

public class ShowStatus extends AppCompatActivity implements StoriesProgressView.StoriesListener, PopupMenu.OnMenuItemClickListener, GestureDetector.OnGestureListener {


    List<String> image;
    List<String> uid;
    List<String> caption;
    List<Long> delete;
    List<String> time;
    Long deleteValue;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    StatusModel model;
    String admin;
    StoriesProgressView storiesProgressView;
    String userId, currentUid, name, url, phone, about;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference statusRef, lastStatus, viewCountRef;
    int counter = 0;
    ImageView s_iv, userIv;
    TextView tvName, StoryViewTv, captionTv, timeTv, replyTv;
    private float x1, x2, y1, y2;
    private static int MIN_DISTANCE = 50;
    private GestureDetector gestureDetector;
    ViewListModel viewListModel;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_status);

        storiesProgressView = findViewById(R.id.stories);
        s_iv = findViewById(R.id.iv_status_shows);
        userIv = findViewById(R.id.iv_au);

        tvName = findViewById(R.id.tv_uname_ss);
        StoryViewTv = findViewById(R.id.statusCount);
        captionTv = findViewById(R.id.StoryCap_tv);
        timeTv = findViewById(R.id.tv_time_ss);

        replyTv = findViewById(R.id.replyTv_Statsu);

        lastStatus = database.getReference("Laststatus");

        this.gestureDetector = new GestureDetector(ShowStatus.this, this);

        model = new StatusModel();

        viewListModel = new ViewListModel();


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

        reverse.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                storiesProgressView.pause();
                return false;
            }
        });


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

        Intent intent = getIntent();

        if(intent != null){

            Log.d("imagel", "imag: "+intent.getStringExtra("image"));

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM");
            final String saveDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:a");
            final String saveTime = currentTime.format(callForTime.getTime());


            viewListModel.setName(intent.getStringExtra("admin"));
            viewListModel.setTime(saveDate + " : " + saveTime);
            viewListModel.setUid(FirebaseAuth.getInstance().getUid());
            viewListModel.setUrl(intent.getStringExtra("uns"));

           if(!intent.getStringExtra("uid").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) FirebaseDatabase.getInstance().getReference().child("seenList").child(intent.getStringExtra("uid")).push().setValue(viewListModel);

        }
        userId = intent.getStringExtra("uid");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = user.getUid();


        statusRef = database.getReference("Status").child(userId);

        viewCountRef = database.getReference("seenList").child(currentUid);

        StoryViewTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewBs();
            }
        });

        if (userId.equals(currentUid)) {
            StoryViewTv.setVisibility(View.VISIBLE);
            replyTv.setVisibility(View.GONE);
            getViewCount();
        } else {
            replyTv.setVisibility(View.VISIBLE);
            StoryViewTv.setVisibility(View.GONE);
           // storeSeenUserData();
        }
    }

    private void getViewCount() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewCountRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int viewCount = (int) snapshot.getChildrenCount();
                        StoryViewTv.setText(String.valueOf(viewCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, 2000);
    }

    private void viewBs() {
        final Dialog dialog = new Dialog(ShowStatus.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.view_bs);

        TextView viewCountTv = dialog.findViewById(R.id.viewedbyTv);
        TextView noViewsYet = dialog.findViewById(R.id.noviewyet_tv);
        ImageButton moreBtn = dialog.findViewById(R.id.more_btn_view);
        RecyclerView recyclerView = dialog.findViewById(R.id.rv_viewBs);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ShowStatus.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        storiesProgressView.pause();

        HashSet<String> set = new HashSet<>();
        List<ViewListModel> list = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("seenList").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                set.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d("DEBUG_SNAPSHOT", dataSnapshot.toString());

                    ViewListModel viewListModel1 = dataSnapshot.getValue(ViewListModel.class);
                    if (viewListModel1 != null && !set.contains(viewListModel1.getUid())) {
                        list.add(viewListModel1);
                        set.add(viewListModel1.getUid());
                    }
                }
                viewCountTv.setText("Viewed by " + list.size());

                if (list.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noViewsYet.setVisibility(View.VISIBLE);
                    Log.d("status tag 44", "not working");
                } else {
                    noViewsYet.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    ViewListAdapter adapter = new ViewListAdapter(ShowStatus.this, list);
                    recyclerView.setAdapter(adapter);

                    Log.d("status tag 44", " working");
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Timber.tag("status tag 44").d("viewBs: k" + list.toString());


        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ShowStatus.this, v);
                popupMenu.setOnMenuItemClickListener(ShowStatus.this);
                popupMenu.inflate(R.menu.bottom_nav_menu);
                popupMenu.show();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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
                storiesProgressView.setStoryDuration(10000L);


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
                    name = task.getResult().getString("username");
                    about = task.getResult().getString("about");
                    phone = task.getResult().getString("phone");
                    url = task.getResult().getString("imageProfile");

                    if (url.equals("")) {
                        tvName.setText(name);
                    } else {
                        tvName.setText(name);
                        Picasso.get().load(url).into(userIv);
                    }
                } else {
                    Toast.makeText(ShowStatus.this, "no data found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onNext() {
        Picasso.get().load(image.get(++counter)).into(s_iv);
        captionTv.setText(caption.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        Picasso.get().load(image.get(--counter)).into(s_iv);
        captionTv.setText(caption.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.delete) {

            StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(image.get(counter));
            reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Query query = statusRef.orderByChild("delete").equalTo(delete.get(counter));
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                snapshot1.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        snapshot1.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            return true;

        } else if (itemId == R.id.download) {
            // Download status
            String imageUrl = image.get(counter);
            String fileName = "status_" + System.currentTimeMillis() + ".jpg";

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
            request.setTitle("Downloading Status");
            request.setDescription("Saving status image...");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);

            Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();

                float valueX = y2 - y1;
                if (Math.abs(valueX) > MIN_DISTANCE) {

                    if (x2 > x1) {
                        if (userId.equals(currentUid)) {
                            viewBs();
                        } else {

                        }
                    } else {

                    }
                } else {

                }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {

    }


    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return false;
    }


    @Override
    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }


    @Override
    public void onLongPress(@NonNull MotionEvent e) {

    }

    @Override
    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}