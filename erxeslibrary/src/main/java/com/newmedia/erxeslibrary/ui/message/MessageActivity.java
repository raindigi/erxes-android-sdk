package com.newmedia.erxeslibrary.ui.message;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.DB;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.SoftKeyboard;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.R;


import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MessageActivity extends AppCompatActivity implements ErxesObserver {

    private EditText edittext_chatbox;
    private RecyclerView mMessageRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Realm realm ;
    private ImageView profile1,profile2;
    private TextView names,isMessenOnlineImage;
    private ViewGroup container,upload_group;
    private ProgressBar progressBar;
    private Config config;
    private ErxesRequest erxesRequest;
    private Point size;
    private GFilePart gFilePart;
    private Animator currentAnimator;
    private int shortAnimationDuration=500;


    private final String TAG = "MESSAGEACTIVITY";
    @Override
    public void notify(final int returnType, String conversationId,  String message) {


            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MessageListAdapter adapter = (MessageListAdapter)mMessageRecycler.getAdapter();
                    switch (returnType){
                        case ReturnType.Subscription:
                            subscription();
                            break;
                            //without break
                        case ReturnType.Getmessages:
                            if(adapter.getItemCount() > 2 && adapter.refresh_data())
                                mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        case ReturnType.Mutation:

                            if(adapter.getItemCount() > 2 && adapter.refresh_data())
                                mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);

                            gFilePart.end_of();
                            break;
                        case ReturnType.Mutation_new:
                            subscribe_conversation();
                            gFilePart.end_of();
                            break;
                        case ReturnType.IsMessengerOnline:
                            isMessenOnlineImage.setText(config.messenger_status_check()?R.string.online:R.string.offline);
//                            header_profile_change();
                            break;

                        case ReturnType.SERVERERROR:
                            Snackbar.make(container, R.string.serverror, Snackbar.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        case ReturnType.CONNECTIONFAILED:
                            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        case ReturnType.GetSupporters:
                            Log.d("GETSUP","message in ");
                            header_profile_change();
                            break;
                    }
                }
            });


    }
    private void subscription(){
        MessageListAdapter adapter = (MessageListAdapter)mMessageRecycler.getAdapter();
//        header_profile_change();
        isMessenOnlineImage.setText(R.string.online);
        if(adapter.getItemCount() > 2 && adapter.refresh_data())
            mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
        swipeRefreshLayout.setRefreshing(false);
    }
    private void bind(User user,ImageView por){
        if(user.avatar!=null) {
            try {
                Glide.with(this.getApplicationContext())
                        .load(user.avatar)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(por);
            }catch (Exception e){}
            por.setVisibility(View.VISIBLE);
        }
        else{
            por.setVisibility(View.VISIBLE);
            por.setImageResource(R.drawable.avatar);
        }

        String t = user.fullName;
        String upperString = t.substring(0,1).toUpperCase() + t.substring(1);
        String previous = names.getText().toString();
        names.setText( previous.length() == 0 ? upperString : previous +","+ upperString);
        names.setVisibility(View.VISIBLE);
    }
    private void header_profile_change(){
        RealmResults<User> users =  DB.getDB().where(User.class).findAll();
        Log.d("GETSUP","in message" +users.size());
        if(users.size() > 0)
            isMessenOnlineImage.setVisibility(View.VISIBLE);
        else
            names.setVisibility(View.INVISIBLE);

        names.setText("");

        if(users.size() > 0)  bind(users.get(0),profile1); else profile1.setVisibility(View.INVISIBLE);
        if(users.size() > 1)  bind(users.get(1),profile2); else profile2.setVisibility(View.INVISIBLE);

        isMessenOnlineImage.setText(config.messenger_status_check()?R.string.online:R.string.offline);

//        isMessenOnlineImage.setVisibility(
//                (Config.isNetworkConnected()&&Config.IsMessengerOnline) ?View.VISIBLE:View.INVISIBLE);

    }
    private void load_findViewByid(){
        container = this.findViewById(R.id.container);

        size = Helper.display_configure(this,container,"#00000000");
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        SoftKeyboard softKeyboard;
        softKeyboard = new SoftKeyboard((ViewGroup)this.findViewById(R.id.linearlayout), im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height =size.y*8/10;
                        container.requestLayout();
                    }
                });
            }
            @Override
            public void onSoftKeyboardShow() {
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;
                        container.requestLayout();
                    }
                });
            }
        });
        upload_group = this.findViewById(R.id.upload_group);
        swipeRefreshLayout = this.findViewById(R.id.swipeRefreshLayout);
        profile1 = this.findViewById(R.id.profile1);
        profile2 = this.findViewById(R.id.profile2);
        isMessenOnlineImage = this.findViewById(R.id.isOnline);
        names = this.findViewById(R.id.names);
        edittext_chatbox = this.findViewById(R.id.edittext_chatbox);
        mMessageRecycler = this.findViewById(R.id.reyclerview_message_list);

        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }

        });

        this.findViewById(R.id.logout).setOnTouchListener(touchListener);
        this.findViewById(R.id.back).setOnTouchListener(touchListener);

        int index = Integer.getInteger(config.wallpaper,-1);
        if(index > -1 && index < 5)
            mMessageRecycler.setBackgroundResource(Helper.backgrounds[index]);

        Image_zoom_init();

    }
    private void Image_zoom_init(){
        shortAnimationDuration = 500;



    }
    public void zoomImageFromThumb(final View thumbView, Bitmap imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = findViewById(R.id.expanded_image);
        expandedImageView.setImageBitmap(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);
//        container.setVisibility(View.GONE);
        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
//                        container.setVisibility(View.VISIBLE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
//                        container.setVisibility(View.VISIBLE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
        realm = DB.getDB();
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_messege);
        gFilePart = new GFilePart(config,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        load_findViewByid();
        mMessageRecycler.setLayoutManager(linearLayoutManager);


        if(config.conversationId != null) {
            linearLayoutManager.setStackFromEnd(true);
            Conversation conversation = DB.getConversation(config.conversationId);
            realm.beginTransaction();
            conversation.isread = true;
            realm.commitTransaction();
            subscribe_conversation();
        }
        else {
            mMessageRecycler.setAdapter(new MessageListAdapter(this,new ArrayList<ConversationMessage>()));
        }
        header_profile_change();

        if (shouldAskPermissions()) {
            askPermissions();
        }
    }
    private void subscribe_conversation(){
        RealmResults<ConversationMessage> d =
                realm.where(ConversationMessage.class).
                        equalTo("conversationId",config.conversationId).findAll();
        d.addChangeListener(new RealmChangeListener<RealmResults<ConversationMessage>>() {
            @Override
            public void onChange(RealmResults<ConversationMessage> conversationMessages) {
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        subscription();
                    }
                });

            }
        });
        mMessageRecycler.setAdapter(new MessageListAdapter(this,d));
        erxesRequest.getMessages(config.conversationId);
    }
    public void Click_back(View v){
        finish();
    }
    public void logout(View v){
        realm.beginTransaction();
        realm.delete(ConversationMessage.class);
        realm.delete(User.class);
        realm.commitTransaction();
        config.Logout();
        finish();
    }
    public void send_message(View view) {
        if(!config.isNetworkConnected()) {
            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if(!edittext_chatbox.getText().toString().equalsIgnoreCase("")) {
            if (config.conversationId != null) {
                erxesRequest.InsertMessage(edittext_chatbox.getText().toString(), config.conversationId, gFilePart.get());
            } else {
                erxesRequest.InsertNewMessage(edittext_chatbox.getText().toString(), gFilePart.get());
            }
            edittext_chatbox.setText("");
        }
    };
    public void refreshItems() {
        if(config.conversationId != null)
            erxesRequest.getMessages(config.conversationId);
        else
            swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        erxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        erxesRequest.add(this);
        erxesRequest.getSupporters();
    }

    //Android 4.4 (API level 19)
    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_PICK );
//        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        chooseFile.setAction(Intent.ACTION_GET_CONTENT);
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, 444);
        upload_group.setClickable(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        gFilePart.ActivityResult(requestCode,resultCode,resultData);
        upload_group.setClickable(true);
    }





    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


    private View.OnTouchListener touchListener =  new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(R.drawable.action_background);
                    }
                });
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(0);
                        if(v.getId() == R.id.logout)
                            logout(null);
                        else if(v.getId() == R.id.back)
                            Click_back(null);
                    }
                });
            }
            return true;
        }
    };



}
