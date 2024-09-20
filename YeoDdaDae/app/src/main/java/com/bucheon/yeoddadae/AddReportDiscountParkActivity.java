package com.bucheon.yeoddadae;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FieldValue;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.poi_item.TMapPOIItem;
import com.skt.tmap.engine.navigation.SDKManager;

import java.util.ArrayList;
import java.util.HashMap;

public class AddReportDiscountParkActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    String loginId;
    String poiId;
    double poiLat;
    double poiLon;
    String poiPhone;
    SearchParkAdapter spa = new SearchParkAdapter();
    double nowLat;
    double nowLon;
    boolean isSearching = false;
    AlertDialog loadingAlert;
    boolean isLoadingFirstCalled = false;
    TMapPoint nowPoint;
    TMapGpsManager gpsManager;

    ImageButton addReportDiscountParkBackBtn;
    EditText addReportDiscountParkAddressContentEditTxt;
    EditText addReportDiscountParkConditionContentEditTxt;
    EditText addReportDiscountParkBenefitContentEditTxt;
    ImageButton reportBtn;
    ConstraintLayout findLocationConstLayout;
    ImageButton findBackBtn;
    EditText searchContentEditTxt;
    ImageButton searchTxtClearBtn;
    ImageButton searchBtn;
    ListView searchResultListView;
    TextView searchNoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report_discount_park);

        addReportDiscountParkBackBtn = findViewById(R.id.addReportDiscountParkBackBtn);
        addReportDiscountParkAddressContentEditTxt = findViewById(R.id.addReportDiscountParkAddressContentEditTxt);
        addReportDiscountParkConditionContentEditTxt = findViewById(R.id.addReportDiscountParkConditionContentEditTxt);
        addReportDiscountParkBenefitContentEditTxt = findViewById(R.id.addReportDiscountParkBenefitContentEditTxt);
        reportBtn = findViewById(R.id.reportBtn);
        findLocationConstLayout = findViewById(R.id.findLocationConstLayout);
        findBackBtn = findViewById(R.id.findBackBtn);
        searchContentEditTxt = findViewById(R.id.searchContentEditTxt);
        searchTxtClearBtn = findViewById(R.id.searchTxtClearBtn);
        searchBtn = findViewById(R.id.searchBtn);
        searchResultListView = findViewById(R.id.searchResultListView);
        searchNoTxt = findViewById(R.id.searchNoTxt);

        Intent inIntent = getIntent();
        loginId = inIntent.getStringExtra("loginId");

        Location currentLocation = SDKManager.getInstance().getCurrentPosition();
        nowLat = currentLocation.getLatitude();
        nowLon = currentLocation.getLongitude();
        nowPoint = new TMapPoint(nowLat, nowLon);

        gpsManager = new TMapGpsManager(this);
        gpsManager.setMinTime(500); // ms단위
        gpsManager.setMinDistance(1); // m단위
        gpsManager.setProvider(gpsManager.GPS_PROVIDER);
        gpsManager.OpenGps();
        // gpsManager.setProvider(gpsManager.NETWORK_PROVIDER);
        // gpsManager.OpenGps();

        addReportDiscountParkBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addReportDiscountParkAddressContentEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findLocationConstLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        searchContentEditTxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    searchBtn.callOnClick();
                }

                return false;
            }
        });

        searchContentEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchContentEditTxt.getText().toString().isEmpty()) {
                    searchTxtClearBtn.setVisibility(View.GONE);
                }
                else {
                    searchTxtClearBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        searchTxtClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchContentEditTxt.getText().toString().isEmpty()) {
                    searchContentEditTxt.setText("");
                }
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSearching) {
                    return;
                }

                isSearching = true;
                searchBtn.setEnabled(false);

                loadingStart();

                if (!replaceNewlinesAndTrim(searchContentEditTxt).isEmpty()) {
                    if (spa != null) {
                        spa.clearPark();
                        spa.clearHistory();
                    }

                    TMapData tMapData = new TMapData();
                    tMapData.findAllPOI(replaceNewlinesAndTrim(searchContentEditTxt), new TMapData.FindAllPOIListenerCallback() {
                        @Override
                        public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                            if (arrayList != null) {
                                for (TMapPOIItem item : arrayList) {
                                    TMapPolyLine tpolyline = new TMapPolyLine();
                                    tpolyline.addLinePoint(nowPoint);
                                    tpolyline.addLinePoint(new TMapPoint(Double.parseDouble(item.frontLat), Double.parseDouble(item.frontLon)));
                                    double distance = tpolyline.getDistance() / 1000; // km단위
                                    if (item.firstNo.equals("0") && item.secondNo.equals("0")) {
                                        spa.addPark(new ParkItem(4, item.name, Double.toString(distance), null, item.telNo, null, -1, item.frontLat, item.frontLon, item.id, null));
                                    }
                                    else {
                                        if (item.name.contains("주차")) {
                                            if (item.name.contains("공영")) {
                                                spa.addPark(new ParkItem(2, item.name, Double.toString(distance), null, item.telNo, null, -1, item.frontLat, item.frontLon, item.id, null));
                                            }
                                            else {
                                                spa.addPark(new ParkItem(1, item.name, Double.toString(distance), null, item.telNo, null, -1, item.frontLat, item.frontLon, item.id, null));
                                            }
                                        }
                                        else {
                                            spa.addPark(new ParkItem(5, item.name, Double.toString(distance), null, item.telNo, null, -1, item.frontLat, item.frontLon, item.id, null));
                                        }
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        spa.duplicationRemove();
                                        searchResultListView.setAdapter(spa);

                                        if (spa.getCount() == 0) {
                                            searchResultListView.setVisibility(View.GONE);
                                            searchNoTxt.setVisibility(View.VISIBLE);
                                        }
                                        else {
                                            searchResultListView.setVisibility(View.VISIBLE);
                                            searchNoTxt.setVisibility(View.GONE);
                                        }
                                        searchBtn.setEnabled(true);
                                        isSearching = false;
                                        loadingStop();
                                    }
                                });
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        searchResultListView.setAdapter(spa);

                                        if (spa.getCount() == 0) {
                                            searchResultListView.setVisibility(View.GONE);
                                            searchNoTxt.setVisibility(View.VISIBLE);
                                        }
                                        else {
                                            searchResultListView.setVisibility(View.VISIBLE);
                                            searchNoTxt.setVisibility(View.GONE);
                                        }
                                        searchBtn.setEnabled(true);
                                        isSearching = false;
                                        loadingStop();
                                    }
                                });
                            }

                        }
                    });
                }
                else {
                    searchBtn.setEnabled(true);
                    isSearching = false;
                    loadingStop();
                }
            }
        });

        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParkItem pi = (ParkItem) spa.getItem(position);
                poiId = pi.getPoiId();
                poiLat = pi.getLat();
                poiLon = pi.getLon();
                poiPhone = pi.getPhone();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addReportDiscountParkAddressContentEditTxt.setText(pi.getName());
                        findLocationConstLayout.setVisibility(View.GONE);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(searchContentEditTxt.getWindowToken(), 0);
                        }
                    }
                });
            }
        });

        findBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findLocationConstLayout.setVisibility(View.GONE);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(searchContentEditTxt.getWindowToken(), 0);
                        }
                    }
                });
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parkName = replaceNewlinesAndTrim(addReportDiscountParkAddressContentEditTxt);
                String condition = replaceNewlinesAndTrim(addReportDiscountParkConditionContentEditTxt);
                String discount = replaceNewlinesAndTrim(addReportDiscountParkBenefitContentEditTxt);

                int discountInt;

                if (poiId == null || poiLat == 0 || poiLon == 0 || parkName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "위치를 찾으세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (condition.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "조건을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (discount.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "혜택을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    discountInt = Integer.parseInt(discount);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "혜택을 숫자로 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> hm = new HashMap<>();
                hm.put("reporterId", loginId);
                hm.put("poiID", poiId);
                hm.put("poiLat", poiLat);
                hm.put("poiLon", poiLon);
                hm.put("poiPhone", poiPhone);
                hm.put("parkName", parkName);
                hm.put("parkCondition", condition);
                hm.put("parkDiscount", discountInt);
                hm.put("ratePerfectCount", 0);
                hm.put("rateMistakeCount", 0);
                hm.put("rateWrongCount", 0);
                hm.put("isCancelled", false);
                hm.put("cancelReason", null);
                hm.put("isApproval", false);
                hm.put("upTime", FieldValue.serverTimestamp());

                FirestoreDatabase fd = new FirestoreDatabase();
                fd.insertData("reportDiscountPark", hm, new OnFirestoreDataLoadedListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getApplicationContext(), "무료/할인 주차장 제보 완료되었습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onDataLoadError(String errorMessage) {
                        Log.d(TAG, errorMessage);
                        Toast.makeText(getApplicationContext(), "오류 발생", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onLocationChange(Location location) {
        nowLat = location.getLatitude();
        Log.d(ContentValues.TAG, "onLocationChange 호출됨 : lat(경도) = " + nowLat);

        nowLon = location.getLongitude();
        Log.d(ContentValues.TAG, "onLocationChange 호출됨 : lon(위도) = " + nowLon);

        nowPoint = new TMapPoint(nowLat, nowLon);
    }

    public void loadingStart() {
        Log.d(TAG, "로딩 시작");

        if (loadingAlert != null && loadingAlert.isShowing()) {
            return; // 이미 보여지고 있다면 함수 종료
        }

        if (!isLoadingFirstCalled) { // 로딩 AlertDialog 지정
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("로딩 중").setCancelable(false).setNegativeButton("액티비티 종료", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            loadingAlert = builder.create();
            isLoadingFirstCalled = true;
        }

        if (!isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingAlert.show();
                    loadingAlert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(AddReportDiscountParkActivity.this, R.color.disable));
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        }
    }

    public void loadingStop() {
        Log.d(TAG, "로딩 끝");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingAlert != null && loadingAlert.isShowing()) {
                    loadingAlert.dismiss();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
    }

    String replaceNewlinesAndTrim(EditText et) {
        return et.getText().toString().replaceAll("\\n", " ").trim();
    }
}
