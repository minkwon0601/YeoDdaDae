package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirestoreDatabase fd = new FirestoreDatabase();

        ImageButton backBtn = (ImageButton) findViewById(R.id.registerBackBtn);
        EditText idTxt = (EditText) findViewById(R.id.registerIdTxt);
        EditText pwTxt = (EditText) findViewById(R.id.registerPwTxt);
        ImageButton registerBtn = (ImageButton) findViewById(R.id.registerBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idTxt.getText().toString();
                String pw = pwTxt.getText().toString();

                if (id.equals("")) {
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (pw.equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (id.length() <= 5 || id.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "ID는 6~20자이어야 합니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (pw.length() <= 5 || pw.length() >= 21) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 6~20자이어야 합니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    fd.duplicationCheck("account", "id", id,  new OnFirestoreDataLoadedListener() {
                        @Override
                        public void onDataLoaded(Object isNotDuplication) {
                            if ((Boolean) isNotDuplication == true) {
                                Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                                
                                HashMap<String, Object> newAccount = new HashMap<String, Object>();
                                newAccount.put("id", id);
                                newAccount.put("pw", pw);
                                newAccount.put("isAdmin", false);
                                newAccount.put("ydPoint", 0);
                                newAccount.put("registerTime", FieldValue.serverTimestamp()); // Use Firestore server timestamp

                                fd.insertData("account", newAccount, new OnFirestoreDataLoadedListener() {
                                    @Override
                                    public void onDataLoaded(Object data) {
                                        Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onDataLoadError(String errorMessage) {
                                        Log.d(TAG, errorMessage);
                                        Toast.makeText(getApplicationContext(), "회원 문서 추가 중 오류 발생", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                            else {
                                Toast.makeText(getApplicationContext(), "중복된 ID가 있습니다", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onDataLoadError(String errorMessage) {
                            Log.d(TAG, errorMessage);
                            Toast.makeText(getApplicationContext(), "아이디 중복 검사 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
