package com.newmedia.erxeslibrary.ui.conversations;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.Messengerdata;
import com.newmedia.erxeslibrary.configuration.MessengerdataIntegration;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ListenerService;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.ui.conversations.adapter.SupportAdapter;
import com.newmedia.erxeslibrary.ui.conversations.adapter.TabAdapter;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;

import java.util.List;

public class ConversationListActivity extends AppCompatActivity  implements ErxesObserver {

    static private String TAG="ConversationListActivity";
    static public boolean chat_is_going = false;

    private RecyclerView supporterView;
    private TextView greetingTitle,greetingMessage,date;
    private ViewPager viewpager;
    private ViewGroup info_header,container;
    private Config config;
    private ErxesRequest erxesRequest;
    private DataManager dataManager;
    private ImageView fb,tw,yt;
    private MessengerdataIntegration messengerdataIntegration;
    @Override
    public void notify(final int  returnType, final String conversationId, String message) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType){
                    case ReturnType.Subscription:
                     case ReturnType.Getconversation:
                         Log.d(TAG,"here changed");
//                        recyclerView.getAdapter().notifyDataSetChanged();
                        break;
                     case ReturnType.INTEGRATION_CHANGED:
                         changeIntegration();
//                        info_header.setBackgroundColor(config.colorCode);
//                        addnew_conversation.getBackground().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);
                        break;
                    case ReturnType.CONNECTIONFAILED:
                        break;
                    case ReturnType.SERVERERROR:
                        break;

                        default:break;
                };


            }
        });

    }
    @Override
    protected void onPause() {
        super.onPause();

        erxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        if(config.customerId == null) {
            this.finish();
            Intent a = new Intent(ConversationListActivity.this,ErxesActivity.class);
            startActivity(a);
            return;
        }
        erxesRequest.add(this);
        config.conversationId = null;
        erxesRequest.getIntegration();
        dataManager.setData("chat_is_going",true);
        info_header.setBackgroundColor(config.colorCode);
        chat_is_going = true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        supporterView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chat_is_going =false;
        dataManager.setData("chat_is_going",false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        setContentView(R.layout.activity_conversation);
        viewpager = findViewById(R.id.viewpager);
        info_header = findViewById(R.id.info_header);
        container = findViewById(R.id.container);
        greetingTitle = findViewById(R.id.greetingTitle);
        greetingMessage = findViewById(R.id.greetingMessage);
        fb = findViewById(R.id.fb);
        tw = findViewById(R.id.tw);
        yt = findViewById(R.id.yt);
        this.findViewById(R.id.logout).setOnTouchListener(touchListener);
        date = findViewById(R.id.date);
        supporterView = findViewById(R.id.supporters);

        dataManager = DataManager.getInstance(this);


        supporterView.setAdapter(new SupportAdapter(this));
        LinearLayoutManager supManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        supporterView.setLayoutManager(supManager);
        Helper.display_configure(this,container,"#66000000");

        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(),this);
        TabLayout tabLayout = findViewById(R.id.tabs);
        viewpager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewpager);


        Intent intent2 = new Intent(this, ListenerService.class);
        startService(intent2);
        erxesRequest.getFAQ();


        fb.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        tw.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        yt.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        this.findViewById(R.id.fbcontainer).setOnTouchListener(touchListener);
        this.findViewById(R.id.twcontainer).setOnTouchListener(touchListener);
        this.findViewById(R.id.ytcontainer).setOnTouchListener(touchListener);

        messengerdataIntegration = dataManager.getMessengerIntegration();
        changeIntegration();
                erxesRequest.getConversations();

    }
    private void changeIntegration(){
        Messengerdata.Messages a = dataManager.getMessenger().messages;
        MessengerdataIntegration.Messages b  = dataManager.getMessengerIntegration().getMessages(config.language);
        if(b !=null ) {
            greetingTitle.setText(b.greetings.title);
            greetingMessage.setText(b.greetings.message);
        }
        else if(a.greetings!=null){
            if(a.greetings.title!=null)
                greetingTitle.setText(a.greetings.title);
            if(a.greetings.message!=null)
                greetingMessage.setText(a.greetings.message);
        }
        if(dataManager.getMessengerIntegration().knowledgeBaseTopicId!=null){

        }
        if(messengerdataIntegration.links.get("facebook")==null || messengerdataIntegration.links.get("facebook").isEmpty()){
            this.findViewById(R.id.fbcontainer).setVisibility(View.GONE);
        }
        if(messengerdataIntegration.links.get("twitter")==null||messengerdataIntegration.links.get("twitter").isEmpty()){
            this.findViewById(R.id.twcontainer).setVisibility(View.GONE);
        }
        if(messengerdataIntegration.links.get("youtube")==null||messengerdataIntegration.links.get("youtube").isEmpty()){
            this.findViewById(R.id.ytcontainer).setVisibility(View.GONE);
        }
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(config.colorCode);
        date.setText(config.now());
    }
    public void start_new_conversation(View v){
        config.conversationId = null;
        Intent a = new Intent(this,MessageActivity.class);
        startActivity(a);
    }

    public void logout(View v){
        finish();
    }
    private View.OnTouchListener touchListener =  new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                ConversationListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(R.drawable.action_background);
                    }
                });
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                ConversationListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(0);
                        if(v.getId() == R.id.logout) {
                            logout(null);
                        }
                        else if(v.getId() == R.id.fbcontainer){
                            if(!messengerdataIntegration.links.get("facebook").isEmpty()){
                                ConversationListActivity.this.startActivity(newFacebookIntent(getPackageManager(),messengerdataIntegration.links.get("facebook")));
                            }
                        }
                        else if(v.getId() == R.id.twcontainer){
                            if(!messengerdataIntegration.links.get("twitter").isEmpty()){
                                startActivity(
                                       new  Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(messengerdataIntegration.links.get("twitter"))
                                        )
                                );
                            }
                        }
                        else if(v.getId() == R.id.ytcontainer){
                            if(!messengerdataIntegration.links.get("youtube").isEmpty()){
                                youtube(messengerdataIntegration.links.get("youtube"));
                            }
                        }
                    }
                });
            }

            return true;
        }
    };

    public  Intent newFacebookIntent() {
        try {
            ConversationListActivity.this.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/375427995960157"));
        } catch ( Exception e) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/ZGMDaily"));
        }
    }

    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }
    private void youtube(String url){
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
        );
        startActivity(intent);
    }
}
