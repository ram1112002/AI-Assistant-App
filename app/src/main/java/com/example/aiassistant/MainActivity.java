package com.example.aiassistant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.icu.text.Transliterator;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.Image;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.telephony.mbms.MbmsErrors;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;


import com.google.type.LatLng;
import com.scwang.wave.MultiWaveHeader;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {


    MultiWaveHeader waveHeader;
    private static final int REQUEST_AUDIO_PERMISSION = 1;
    private static final int REQUEST_READ_CONTACTS = 2;
    private static final int REQUEST_CALL_PHONE = 3;
    private static final int VOICE_DATA_CHECK = 4;
    String mimeString = "vnd.android.cursor.item/vnd.com.whatsapp.voip.call";
    String googleTtsPackage = "com.google.android.tts", picoPackage = "com.svox.pico";

    ArrayList<String> result;
    private SpeechRecognizer recognizer;
    private TextView tv;
    private TextToSpeech tts;
    private GifImageView c;

    private RecyclerView chatsRV;
    private final String BOT_KEY = "bot";
    private final String USER_KEY = "user";
    private ArrayList<ChatsModel> chatsModelsArrayList;
    private Adapter adapter;
    com.felipecsl.gifimageview.library.GifImageView abx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        android.graphics.drawable.Drawable background = MainActivity.this.getResources().getDrawable(R.drawable.gradient);
        getWindow().setBackgroundDrawable(background);

        c = findViewById(R.id.imageView6);
        c.setBackgroundResource(R.drawable.gif_ai_female);

        waveHeader = findViewById(R.id.wave1);
        waveHeader.setVelocity(1);
        waveHeader.setProgress(1);
        waveHeader.isRunning();
        waveHeader.setGradientAngle(45);
        waveHeader.setWaveHeight(40);
        waveHeader.setStartColor(Color.MAGENTA);
        waveHeader.setCloseColor(Color.YELLOW);

        chatsRV = findViewById(R.id.idRvChats);
        chatsModelsArrayList = new ArrayList<>();
        adapter = new Adapter(chatsModelsArrayList, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRV.setLayoutManager(manager);
        chatsRV.setAdapter(adapter);


        ImageButton b = (ImageButton) findViewById(R.id.button);


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.CALL_PHONE,
                    }, REQUEST_CALL_PHONE);

                }
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.RECORD_AUDIO
                    }, REQUEST_AUDIO_PERMISSION);
                } else {

                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

                    recognizer.startListening(intent);
                }
            }
        });


        FetchNameForWhatsapp("whatsapp video call to avinash 2");

        initializeTextToSpeech();
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                ((GifDrawable) c.getDrawable()).start();

            }

            @Override
            public void onDone(String utteranceId) {
                ((GifDrawable) c.getDrawable()).stop();

            }

            @Override
            public void onError(String utteranceId) {

            }
        });


        //        if (tts.isSpeaking() == true){
//            ((GifDrawable)c.getDrawable()).start();
//
//        } else if (tts.isSpeaking() == false){
//            ((GifDrawable)c.getDrawable()).stop();
//
//        }
        initializeResults();


    }


    private void getMessage(String ma) {
        chatsModelsArrayList.add(new ChatsModel(ma, USER_KEY));
        adapter.notifyDataSetChanged();
        chatsRV.smoothScrollToPosition(adapter.getItemCount());


    }

    private void getBotMessage(String bt) {

        chatsModelsArrayList.add(new ChatsModel(bt, BOT_KEY));
        adapter.notifyDataSetChanged();
        chatsRV.smoothScrollToPosition(adapter.getItemCount());

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(MainActivity.this, "First Allow Permission", Toast.LENGTH_SHORT).show();
                    System.exit(0);

                }
        }
    }


    private void initializeTextToSpeech() {

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {

                String a = tts.getVoice().getName();
                if (a.equals("en-US-SMTm00")) {
                    GifImageView iv = (GifImageView) findViewById(R.id.imageView6);
                    iv.setImageResource(R.drawable.male_ai);
                } else {
                    GifImageView iv = (GifImageView) findViewById(R.id.imageView6);
                    iv.setImageResource(R.drawable.gif_ai_female);
                }

                if (tts.getEngines().size() == 0) {
                    Toast.makeText(MainActivity.this, "Text To Speech Engine is not available on Your Device", Toast.LENGTH_SHORT).show();
                } else {
                    String s = Wish();
                    speak(s + "Hi I Am your Ai Assistant...");


                }
                if (tts.isSpeaking() == false) {
                    ((GifDrawable) c.getDrawable()).stop();

                }

            }


        });
    }

    public void speak(String msg) {
        tts.speak(msg, TextToSpeech.QUEUE_ADD, null, "null");
//        ((GifDrawable)c.getDrawable()).start();


    }


    @Override
    protected void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();

        }
        super.onDestroy();
    }

    private void initializeResults() {

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle results) {

                    result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    Toast.makeText(MainActivity.this, "" + result.get(0), Toast.LENGTH_SHORT).show();
                    getMessage(result.get(0));
//                    tv.setText(result.get(0));
                    response(result.get(0));

                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }

    }


    private void response(String msg) {

        String msgs = msg.toLowerCase();

        if (msgs.indexOf("hi") != -1 || msgs.indexOf("hello") != -1 || msgs.indexOf("hey!") != -1) {
            if (msgs.indexOf("assistant") != -1) {
                speak("Hello Sir! How may I help you");

                getBotMessage("Hello Sir! How may I help you");
            }
        }
        if (msgs.indexOf("how") != -1){
            if(msgs.indexOf("are") != -1){
                if(msgs.indexOf("you") != -1){
                    speak("I am fine sir");

                    getBotMessage("I am fine sir");
                }
            }
        }


        if (msgs.indexOf("time") != -1) {
            if (msgs.indexOf("now") != -1 || msg.indexOf("current") != -1) {
                Date date = new Date();
                String time = DateUtils.formatDateTime(this, date.getTime(), DateUtils.FORMAT_SHOW_TIME);
                speak("currently time is " + time);
                getBotMessage("currently time is " + time);

            }
        }
        if (msgs.indexOf("today") != -1) {
            if (msgs.indexOf("date") != -1) {
                String date = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

                speak("today's date is " + date);
                getBotMessage("today's date is " + date);

            }
        }
        if (msgs.indexOf("open") != -1) {
            if (msgs.indexOf("google") != -1) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
                startActivity(intent);

            }
            if (msgs.indexOf("whatsapp") != -1) {

                Intent intent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                if (intent != null) {
                    startActivity(intent);


                } else {
                    final String appPackageName = "com.whatsapp"; // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    speak("sir please install whatsapp");
                    getBotMessage("sir please install whatsapp");

                }
            }
            if (msgs.indexOf("youtube") != -1) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                if (intent != null) {
                    startActivity(intent);

                } else {
                    final String appPackageName = "com.google.android.youtube"; // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    speak("sir please install youtube");
                    getBotMessage("sir please install youtube");
                }
            }
            if (msgs.indexOf("facebook") != -1) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                if (intent != null) {
                    startActivity(intent);

                } else {
                    final String appPackageName = "com.facebook.katana"; // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    speak("sir please install facebook");
                    getBotMessage("sir please install facebook");
                }
            }
            if (msgs.indexOf("snapchat") != -1) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.snapchat.android");
                if (intent != null) {
                    startActivity(intent);

                } else {
                    final String appPackageName = "com.snapchat.android"; // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    speak("sir please install snapchat");
                    getBotMessage("sir please install snapchat");
                }
            }
            if (msgs.indexOf("instagram") != -1) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                if (intent != null) {
                    startActivity(intent);

                } else {
                    final String appPackageName = "com.instagram.android"; // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    speak("sir please install instagram");
                    getBotMessage("sir please install instagram");
                }
            }
            if (msgs.indexOf("telegram") != -1) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("org.telegram.messenger");
                if (intent != null) {
                    startActivity(intent);
                } else {
                    final String appPackageName = "org.telegram.messenger"; // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
            }
            if (msgs.indexOf(FetchappName(msg)) != -1){



                    String a = getPackNameByAppName(FetchappName(msg));
                    Intent intent = getPackageManager().getLaunchIntentForPackage(a);
                    if (intent != null) {
                        startActivity(intent);
                        speak("opening " +  FetchappName(msg)) ;
                        getBotMessage("opening " +  FetchappName(msg));
                    }
                else {
                    speak("Sir " +FetchappName(msgs) + " not installed on your Phone ");
                    getBotMessage("Sir " +FetchappName(msgs) + " not installed on your Phone ");
                }
            }

        }


        if (msgs.indexOf("call") == 0) {


            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.READ_CONTACTS,
                }, REQUEST_READ_CONTACTS);

            } else {


                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + getPhone(msgs)));//change the number
                startActivity(callIntent);
                speak("calling " + FetchName(msgs));
                getBotMessage("calling " + FetchName(msgs));
            }

        }
        if (msgs.indexOf("whatsapp") == 0) {


            String phoneNumberWithCountryCode = getPhoneWhatsapp(msgs);

            String message = FetchMessageForWhatsapp(msgs);


//            String x = phoneNumberWithCountryCode.substring(0, 3);
            String x = phoneNumberWithCountryCode.length() > "+91".length() ? phoneNumberWithCountryCode.substring(0, 3) : "";


            if (x.equals("+91")) {

            } else {
                phoneNumberWithCountryCode = +91 + phoneNumberWithCountryCode;
            }

            if (msgs.indexOf("text") != -1) {

                startActivity(
                        new Intent(Intent.ACTION_VIEW,
                                Uri.parse(
                                        String.format("https://api.whatsapp.com/send?phone=%s&text=%s", phoneNumberWithCountryCode, message)
                                )
                        )
                );
                speak("Sending text to" + FetchNameForWhatsapp(msgs));
                getBotMessage("Sending text to " + FetchNameForWhatsapp(msgs));

            }


        }

        if (msgs.indexOf("whatsapp") == 0) {


            if (msgs.indexOf("voice call to") != -1) {
                long id = 0;
                String mimeType = null;


                String[] projection = new String[]{ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.MIMETYPE};


                ContentResolver resolver = getApplication().getContentResolver();
                Cursor cursor = resolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        projection, null, null,
                        ContactsContract.Contacts.DISPLAY_NAME);

//Now read data from cursor like

                while (cursor.moveToNext()) {

                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME));
                    String xyz = displayName.toLowerCase();
                    long _id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Data._ID));
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE));

                    Log.d("Data", _id + " " + displayName + " " + mimeType);

                    String a = FetchNameForWhatsapp(msgs);
                    if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.voip.call")) {

                        if (a.equals(xyz)) {
                            id = _id;
                            break;
                        }
                    }
                }
                if (id == 0) {
                    speak("Sir please check the name");
                    getBotMessage("Sir please check the name");
                }
                if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.voip.call")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);


                    intent.setDataAndType(Uri.parse("content://com.android.contacts/data/" + id),
                            "vnd.android.cursor.item/vnd.com.whatsapp.voip.call");
                    intent.setPackage("com.whatsapp");

                    startActivity(intent);
                    speak("Connecting voice call to " + FetchNameForWhatsapp(msgs));
                    getBotMessage("Connecting voice call to " + FetchNameForWhatsapp(msgs));
                }
            }
            if (msgs.indexOf("video call to") != -1) {
                long id = 0;
                String mimeType = null;


                String[] projection = new String[]{ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.MIMETYPE};


                ContentResolver resolver = getApplication().getContentResolver();
                Cursor cursor = resolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        projection, null, null,
                        ContactsContract.Contacts.DISPLAY_NAME);

//Now read data from cursor like

                while (cursor.moveToNext()) {

                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME));
                    String xyz = displayName.toLowerCase();
                    long _id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Data._ID));
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE));

                    Log.d("Data", _id + " " + displayName + " " + mimeType);

                    String a = FetchNameForWhatsapp(msgs);

                    if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.video.call")) {
                        if (a.equals(xyz)) {
                            id = _id;
                            break;
                        }
                    }

                }
                if (id == 0) {
                    speak("Sir please check the name");
                    getBotMessage("Sir please check the name");

                }
                if (mimeType.equals("vnd.android.cursor.item/vnd.com.whatsapp.video.call")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);


                    intent.setDataAndType(Uri.parse("content://com.android.contacts/data/" + id),
                            "vnd.android.cursor.item/vnd.com.whatsapp.video.call");
                    intent.setPackage("com.whatsapp");

                    startActivity(intent);
                    speak("Connecting video call to " + FetchNameForWhatsapp(msgs));
                    getBotMessage("Connecting video call to "  + FetchNameForWhatsapp(msgs));
                }
            }
        }


        if (msgs.indexOf("change") != -1) {
            if (msgs.indexOf("voice to") != -1) {

                String a = tts.getVoice().getName();


                if (a.equals("en-US-SMTf00")) {
                    if (msgs.indexOf("male") != -1) {


                        ((GifDrawable) c.getDrawable()).stop();

                        c.setImageResource(R.drawable.male_ai);

                        ttsVoiceChange("en-US-SMTm00");
                        getBotMessage("Assistant Changed to male");

                    }

                } else if (a.equals("en-US-SMTm00")) {
                    if (msgs.indexOf("female") != -1) {


                        ((GifDrawable) c.getDrawable()).stop();


                        c.setImageResource(R.drawable.gif_ai_female);

                        ttsVoiceChange("en-US-SMTf00");
                        getBotMessage("Assistant Changed to female");

                    }
                }

            }
        }

        if (msgs.indexOf("google") != -1) {
            if (msgs.indexOf("search") != -1) {

                final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.setPackage("com.google.android.googlequicksearchbox");
                intent.putExtra(SearchManager.QUERY, FetchGoogleSearch(msgs));
                startActivity(intent);
                speak("Searching on Google");
                getBotMessage("Searching on Google");
            }
        }

        if(msgs.indexOf("find") != -1 || msgs.indexOf("get") != -1){
            if (msgs.indexOf("location") != -1){
                Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(FetchLocation(msgs)));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                speak("Finding location");
                getBotMessage("Finding location");
            }

            if (msgs.indexOf("route") != -1){
                String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+GetLat(FetchLocation1(msgs))+","+GetLon(FetchLocation1(msgs))+"&daddr="+GetLat(FetchLocation2(msgs))+","+GetLon(FetchLocation2(msgs));
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(Intent.createChooser(intent, "Select an application"));
                speak("Finding route");
                getBotMessage("Finding route");
            }
        }

        if (msgs.indexOf("youtube") != -1){
            if (msgs.indexOf("search") != -1){

                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                intent.putExtra(SearchManager.QUERY, FetchYoutubeSearch(msgs));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                speak("Searching on Youtube");
                getBotMessage("Searching on Youtube");
            }
        }

        if (msgs.indexOf("play") != -1){
            if (msgs.indexOf("music") != -1 || msgs.indexOf("song") != -1){
                Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);


                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*");
                intent.putExtra(SearchManager.QUERY, FetchMusic(msgs));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

//                Intent intent = new Intent(MediaStore.Inte);
//                intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,
//                        MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
//                intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE,FetchMusic(msgs));
//                intent.putExtra(SearchManager.QUERY,FetchMusic(msgs));
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intent);
                }
            }

        final ArrayList<Integer> days  = new ArrayList<>();
        String[] data = Alarmday(msgs).split(" ");
        for (int i = 0; i<data.length; i++){
            String cd = data[i];
            if ( cd.equals("monday")){
                days.add(Calendar.MONDAY);
            }else if (cd.equals("tuesday")){
                days.add(Calendar.TUESDAY);

            }else if (cd.equals("wednesday")){
                days.add(Calendar.WEDNESDAY);

            }else if (cd.equals("thursday")){
                days.add(Calendar.THURSDAY);

            }else if (cd.equals("friday")){
                days.add(Calendar.FRIDAY);

            }else if (cd.equals("saturday")){
                days.add(Calendar.SATURDAY);

            }else if (cd.equals("sunday")){
                days.add(Calendar.SUNDAY);

            }
        }



        if (msgs.indexOf("set") != -1){
            if (msgs.indexOf("alarm") != -1){
                String a = FetchAlarmTime(msgs).substring(0,2);
                String b = FetchAlarmTime(msgs).substring(3,5);
                Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
                i.putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm by AI Assistant");
                i.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(a));
                i.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(b));
                i.putExtra(AlarmClock.EXTRA_DAYS, days);

                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(i);
                }
                speak("Alarm set for" + FetchAlarmTime(msgs));
                getBotMessage("Alarm set for" + FetchAlarmTime(msgs));

            }
        }

        if (msgs.indexOf("show") != -1){
            if (msgs.indexOf("tutorials") != -1){

                speak("Sir here are some tutorials to help you operate this app");
                getBotMessage("Tutorial:\n 1. For Calling: call (name)\n 2. Whatsapp Voice call: whatsapp voice call to (name)\n 3. Whatsapp Video call: whatsapp video call to (name)\n 4. Whatsapp Text: whatsapp (name) text (message)\n 5. Location Search: find/get location (name location)\n 6. Route Search: find/get route from (name location 1) to (name location 2)\n 7. Google Search: google search (query)\n 8. Youtube Search: youtube search (query)\n 9. Change Speech: change voice to (male/female)\n 10. Play Music: play music (name song)\n 11. Open Apps: open app (app name) ");

            }
        }


//        else {
//            speak("Sir i am not able to understand  what you want to say");
//            getBotMessage("Sir i am not able to understand  what you want to say");
//        }
        }





    public void ttsVoiceChange(String voiceName) {

        tts = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {


                    @Override
                    public void onInit(int status) {
                        ((GifDrawable) c.getDrawable()).stop();

                        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                ((GifDrawable) c.getDrawable()).start();

                            }

                            @Override
                            public void onDone(String utteranceId) {
                                ((GifDrawable) c.getDrawable()).stop();

                            }

                            @Override
                            public void onError(String utteranceId) {

                            }
                        });


                        for (Voice tmpVoice : tts.getVoices()) {
                            if (tmpVoice.getName().equals(voiceName)) {
                                tts.setVoice(tmpVoice);

                            }

                        }


                    }

                });
    }

    public double GetLat(String loc){
        double lat = 0;
        if(Geocoder.isPresent()){
            try {
                ;
                Geocoder gc = new Geocoder(this);
                List<Address> addresses= gc.getFromLocationName(loc, 5); // get the found Address Objects

                for(Address a : addresses){
                    if(a.hasLatitude()){
                        lat = a.getLatitude();

                    }
                }
            } catch (IOException e) {
                // handle the exception
            }
        }
        return  lat;
    }
    public double GetLon(String loc){
        double lon = 0;
        if(Geocoder.isPresent()){
            try {
                Geocoder gc = new Geocoder(this);
                List<Address> addresses= gc.getFromLocationName(loc, 5); // get the found Address Objects

                for(Address a : addresses){
                    if(a.hasLongitude()){
                        lon = a.getLongitude();

                    }
                }
            } catch (IOException e) {
                // handle the exception
            }
        }
        return  lon ;
    }


    public String getPhone(String msgs) {
        String pho = null;

        Cursor phones = MainActivity.this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        phones.moveToFirst();
        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String xyz = name.toLowerCase();


            Log.d("Name:", name);

            String phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String ph = phoneNumber.toLowerCase();
            Log.d("phoneNumber:", phoneNumber);
            if (FetchName(msgs).equals(xyz)) {
                pho = ph;
                break;
            }


        }
        if (pho == "") {
            speak("Sir please Check the Name.....The Name should be exact you have saved in your Phone");
            pho = "";

        }
        return pho;
    }

    public String getPhoneWhatsapp(String msgs) {
        String pho = null;

        Cursor phones = MainActivity.this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        phones.moveToFirst();
        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String xyz = name.toLowerCase();


            Log.d("Name:", name);

            String phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String ph = phoneNumber.toLowerCase();
            Log.d("phoneNumber:", phoneNumber);
            if (FetchNameForWhatsapp(msgs).equals(xyz)) {
                pho = ph;
                break;
            }


        }
        if (pho.equals(null)) {
            speak("Sir please Check the Name.....The Name should be exact you have saved in your Phone");
            pho = "";

        }
        return pho;
    }


    private static String Wish() {
        String s = "";
        Calendar c = Calendar.getInstance();
        int time = c.get(Calendar.HOUR_OF_DAY);

        if (time >= 0 && time < 12) {
            s = "Good Morning Sir";
        } else if (time >= 12 && time < 16) {
            s = "Good Afternoon Sir";
        } else if (time >= 16 && time < 22) {
            s = "Good evening Sir";
        } else {
            s = "Sir its too late you should rest....";
        }
        return s;
    }

    public String FetchName(String msg) {

        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("call")) {
                if (data[(i + 1)].equals("to")) {
                    flag = false;

                } else {
                    flag = true;

                }
            } else if (d.equals("and") || d.equals(".")) {
                flag = false;
            } else if (data[(i - 1)].equals("call")) {
                if (d.equals("to")) {
                    flag = true;
                }
            }
            if (flag) {
                if (!d.equals("call") && !d.equals("to")) {
                    name = name.concat(" " + d);
                }

            }
        }


        Log.d("Name : ", name);
        name = name.toLowerCase().trim();
        return name;
    }

    public String FetchNameForWhatsapp(String msg) {

        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("whatsapp")) {
                flag = true;


            } else if (d.equals("and") || d.equals(".")) {
                flag = false;
            } else if (data[(i - 1)].equals("video") || data[(i - 1)].equals("voice") && data[(i - 2)].equals("whatsapp")) {
                if (d.equals("to")) {
                    flag = true;
                }
            } else if (d.equals("text")) {
                break;
            }
            if (flag) {
                if (!d.equals("whatsapp") && !d.equals("text") && !d.equals("call") && !d.equals("to") && !d.equals("voice") && !d.equals("video")) {
                    name = name.concat(" " + d);

                }
            }

        }
        Log.d("FetchNameForWhatsapp : ", name);

        name = name.toLowerCase().trim();
        return name;
    }

    public String FetchNameForWhatsapp2(String msg) {

        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("whatsapp")) {
                flag = true;


            } else if (d.equals("and") || d.equals(".")) {
                flag = false;
            } else if (data[2] == "to") {
                data[2].replace("to", "2");
            } else if (d.equals("text")) {
                break;
            }
            if (flag) {
                if (!d.equals("whatsapp") && !d.equals("text") && !d.equals("to")) {
                    name = name.concat(" " + d);

                }
            }


        }
        Log.d("FetchNameForWhatsapp : ", name);
        name = name.toLowerCase().trim();
        return name;
    }

    public String FetchMessageForWhatsapp(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("whatsapp")) {

                flag = true;


            }
            if (flag) {
                if (!d.equals("whatsapp") && !d.equals("text") && !d.equals(FetchNameForWhatsapp(msg))) {
                    name = name.concat(" " + d);

                }
            }


        }

        name = name.replace(FetchNameForWhatsapp(msg), "");
        name = name.toLowerCase().trim();
        Log.d("MessageWhatsapp : ", name);
        return name;
    }

    public String FetchappName(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("open")) {

                flag = true;


            }
            if (flag) {
                if (!d.equals("open")) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.toLowerCase().trim();
        Log.d("appName : ", name);
        return name;
    }

    public String FetchGoogleSearch(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("google")) {

                flag = true;


            }
            if (flag) {
                if (!d.equals("google") && !d.equals("search")) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.toLowerCase().trim();
        Log.d("Search : ", name);
        return name;
    }
    public String FetchYoutubeSearch(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("youtube")) {

                flag = true;


            }
            if (flag) {
                if (!d.equals("youtube") && !d.equals("search")) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.toLowerCase().trim();
        Log.d("Search : ", name);
        return name;
    }

    public String FetchLocation(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("find")) {

                flag = true;


            }
            if (flag) {
                if (!d.equals("find") && !d.equals("location") && !d.equals("of")) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.toLowerCase().trim();
        Log.d("Search : ", name);
        return name;
    }

    public String FetchMusic(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("play")) {

                flag = true;


            }
            if (flag) {
                if (!d.equals("play") && !d.equals("music") && !d.equals("song")) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.toLowerCase().trim();
        Log.d("Search : ", name);
        return name;
    }

    public String FetchLocation1(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("find") || d.equals("get")) {

                flag = true;


            }else if (d.equals("to")){
                break;
            }
            if (flag) {
                if (!d.equals("find") && !d.equals("direction") && !d.equals("from") && !d.equals("to") && !d.equals("get")) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.toLowerCase().trim();
        Log.d("Location1 : ", name);
        return name;
    }

    public String FetchLocation2(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("find") || d.equals("get")) {

                flag = true;


            }
            if (flag) {
                if (!d.equals("find") && !d.equals("direction") && !d.equals("from") && !d.equals("to") && !d.equals(FetchLocation1(msg)) && !d.equals("get")) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.replace(FetchLocation1(msg), "");
        name = name.toLowerCase().trim();
        Log.d("Location1 : ", name);
        return name;
    }

    public String FetchAlarmTime(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("set")) {

                flag = true;


            }else if (d.equals("on")){
                break;
            }
            if (flag) {
                if (!d.equals("alarm") && !d.equals("set") && !d.equals("at") && !d.equals("on") ) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.toLowerCase().trim();
        Log.d("AlarmTime : ", name);
        return name;
    }

    public String Alarmday(String msg) {
        msg = msg.toLowerCase();
        String name = "";
        boolean flag = false;
        String[] data = msg.split(" ");
        for (int i = 0; i < data.length; i++) {
            String d = data[i];
            if (d.equals("set")) {

                flag = true;


            }
            if (flag) {
                if (!d.equals("alarm") && !d.equals("set") && !d.equals("at") && !d.equals("on") && !d.equals(FetchAlarmTime(msg)) && !d.equals("and")) {
                    name = name.concat(" " + d);

                }
            }
        }
        name = name.replace(FetchAlarmTime(msg), "");
        name = name.toLowerCase().trim();
        Log.d("AlarmDays : ", name);
        return name;
    }


    public String getPackNameByAppName(String name) {

        PackageManager pm = getApplicationContext().getPackageManager();
        List<ApplicationInfo> l = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        String packName = "";
        for (ApplicationInfo ai : l) {
            String n = (String)pm.getApplicationLabel(ai);
            n = n.toLowerCase().trim();
            if (n.equals(name) || name.equals(n)){
                packName = ai.packageName;
            }
            Log.d("appName:",n );
            Log.d("pakName:",ai.packageName );

        }

        return packName;
    }

}