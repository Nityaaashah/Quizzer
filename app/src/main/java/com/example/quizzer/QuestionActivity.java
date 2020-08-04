package com.example.quizzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    public static final String FILE_NAME = "QUIZZER";
    public static final String KEY_NAME = "QUESTION";

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference myRef = firebaseDatabase.getReference();

    private TextView question, no_indicator;
    private FloatingActionButton bookmark_btn;

    private LinearLayout optioncontainer;
    private Button shareBtn, nextBtn;
    private int count = 0;
    List<QuestionModel> list;
    private int position = 0;
    private int score = 0;
    private String category;
    private String setid;
    private Dialog loadingdialog;
    private Gson gson;
    private int matchedQuestionposition;

    private List<QuestionModel> bookmarklist;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        question = findViewById(R.id.questions);
        no_indicator = findViewById(R.id.no_indicator);
        bookmark_btn = findViewById(R.id.bookmark_btn);
        optioncontainer = findViewById(R.id.option_container);
        shareBtn = findViewById(R.id.share_btn);
        nextBtn = findViewById(R.id.next_btn);


        sharedPreferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();

        getBookmark();
        bookmark_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modelmatch()){
                    bookmarklist.remove(matchedQuestionposition);
                    bookmark_btn.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                }
                else{
                    bookmarklist.add(list.get(position));
                    bookmark_btn.setImageDrawable(getDrawable(R.drawable.bookmark));
                }
            }
        });

        setid = getIntent().getStringExtra("setid" );

        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corner));
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);


        list = new ArrayList<>();
        loadingdialog.show();
        myRef.child("SETS").child(setid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                {
                    String id = dataSnapshot1.getKey();
                    String question = dataSnapshot1.child("question").getValue().toString();
                    String a = dataSnapshot1.child("optionA").getValue().toString();
                    String b = dataSnapshot1.child("optionB").getValue().toString();
                    String c = dataSnapshot1.child("optionC").getValue().toString();
                    String d = dataSnapshot1.child("optionD").getValue().toString();
                    String correctAns = dataSnapshot1.child("correctAns").getValue().toString();
                    list.add(new QuestionModel(id,question,a,b,c,d,correctAns,setid));
                }
                if (list.size() > 0) {
                    for (int i = 0; i < 4; i++) {
                        optioncontainer.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkAnswer((Button) v);
                            }
                        });
                    }
                    playanim(question, 0, list.get(position).getQuestion());
                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            count = 0;
                            nextBtn.setEnabled(false);
                            enabledoption(true);
                            nextBtn.setAlpha(0.7f);
                            position++;
                            if (position == list.size()) {//send the user to scrore activity
                                Intent scoreintent = new Intent(QuestionActivity.this, ScoreActivity.class);
                                scoreintent.putExtra("score", score);
                                scoreintent.putExtra("total", list.size());
                                startActivity(scoreintent);
                                finish();
                                return;
                            }
                            count = 0;
                            playanim(question, 0, list.get(position).getQuestion());
                        }
                    });

                        shareBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String body = list.get(position).getQuestion() + "\n" +
                                              list.get(position).getA() +  "\n" +
                                              list.get(position).getB() +  "\n" +
                                              list.get(position).getC() + "\n" +
                                              list.get(position).getC();
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("plain/text");
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Quizzer Challange");
                                shareIntent.putExtra(Intent.EXTRA_TEXT,body);
                                startActivity(Intent.createChooser(shareIntent,"Share Via"));
                            }
                        });
                } else {
                    finish();
                    Toast.makeText(QuestionActivity.this, "No Question", Toast.LENGTH_SHORT);
                }
                loadingdialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestionActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT);
                loadingdialog.dismiss();
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }

    private void playanim(final View view, final int value, final String data) {
        for (int i = 0; i < 4; i++) {
            optioncontainer.getChildAt(i).setBackgroundTintList( ColorStateList.valueOf(Color.parseColor("#989898")));
        }
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (value == 0 && count < 4) {
                    String option = "";
                    if (count == 0) {
                        option = list.get(position).getA();
                    } else if (count == 1) {
                        option = list.get(position).getB();
                    } else if (count == 2) {
                        option = list.get(position).getC();
                    } else if (count == 3) {
                        option = list.get(position).getD();
                    }
                    playanim(optioncontainer.getChildAt(count), 0, option);
                    count++;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //data changed//

                if (value == 0) {
                    try {
                        ((TextView) view).setText(data);
                        no_indicator.setText(position + 1 + "/" + list.size());
                        if(modelmatch()){
                            bookmark_btn.setImageDrawable(getDrawable(R.drawable.bookmark));
                        }
                        else{
                            bookmark_btn.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                        }

                    } catch (ClassCastException e) {
                        ((Button) view).setText(data);

                    }
                    view.setTag(data);
                    playanim(view, 1, data);
                }
                else{
                    enabledoption(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void checkAnswer(Button selectedoption) {
        enabledoption(false);
        nextBtn.setEnabled(true);
        nextBtn.setAlpha(1);
        if (selectedoption.getText().toString().equals(list.get(position).getAnswer())) {
            score++;
            //correct
            selectedoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3C40C6")));
        } else {
            //incorrect
            selectedoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
            //showing the correct option//
            Button correctoption = (Button) optioncontainer.findViewWithTag(list.get(position).getAnswer());
            correctoption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3C40C6")));
        }
    }

    private void enabledoption(boolean enable) {
        for (int i = 0; i < 4; i++) {
            optioncontainer.getChildAt(i).setEnabled(enable);
            if (enable) {
                optioncontainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#777E8B")));
            }
        }
    }

    private void getBookmark() {
        String json = sharedPreferences.getString(KEY_NAME, "");
        Type type = new TypeToken<List<QuestionModel>>() {
        }.getType();
        bookmarklist = gson.fromJson(json, type); //retrive from prefrence and store in the variable//
        if (bookmarklist == null) {
            bookmarklist = new ArrayList<>();
        }
    }

    //To match which given is bookmark(for storing ) and which is not//
    private Boolean modelmatch() {
        boolean matched = false;
        int i=0;
        for (QuestionModel model : bookmarklist) {
            if(model.getQuestion().equals(list.get(position).getQuestion())
             && model.getAnswer().equals(list.get(position).getAnswer())
             && model.getSet().equals(list.get(position).getSet()))
            {
                matched =true;
                matchedQuestionposition=i;

            }
            i++;
        }
        return matched;
    }


    private void storeBookmarks(){
        //Converting list to json//
        String Json =gson.toJson(bookmarklist);
        editor.putString(KEY_NAME,Json);
        editor.commit();
    }

}
