package com.example.mynfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;

    private TextView uidText; // UID를 표시할 텍스트뷰
    private TextView authStatusText; // 인증 상태를 표시할 텍스트뷰
    private TextView blockDataText; // 블록 데이터를 표시할 텍스트뷰
    private TextView cardNumberText; // 카드 번호를 표시할 텍스트뷰

    private Handler handler = new Handler(); // 태그 해제 후 초기화할 핸들러
    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {
            // 5초 후 UID, Auth Status, Block Data, Card Number 텍스트를 초기 상태로 변경
            uidText.setText("UID: ");
            authStatusText.setText("Auth Status: ");
            blockDataText.setText("Block Data: ");
            cardNumberText.setText("Card Number: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // NFC 어댑터 초기화
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // 레이아웃 설정
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);

        // UID 텍스트뷰 초기화 (빈 문자열로 설정)
        uidText = new TextView(this);
        uidText.setText("UID: ");
        uidText.setTextSize(24);
        uidText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        uidText.setGravity(Gravity.CENTER);

        // Auth Status 텍스트뷰 초기화 (빈 문자열로 설정)
        authStatusText = new TextView(this);
        authStatusText.setText("Auth Status: ");
        authStatusText.setTextSize(24);
        authStatusText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        authStatusText.setGravity(Gravity.CENTER);

        // Block Data 텍스트뷰 초기화 (빈 문자열로 설정)
        blockDataText = new TextView(this);
        blockDataText.setText("Block Data: ");
        blockDataText.setTextSize(24);
        blockDataText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        blockDataText.setGravity(Gravity.CENTER);

        // Card Number 텍스트뷰 초기화 (빈 문자열로 설정)
        cardNumberText = new TextView(this);
        cardNumberText.setText("Card Number: ");
        cardNumberText.setTextSize(24);
        cardNumberText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        cardNumberText.setGravity(Gravity.CENTER);

        // 레이아웃에 텍스트뷰 추가
        layout.addView(uidText);
        layout.addView(authStatusText);
        layout.addView(blockDataText);
        layout.addView(cardNumberText);

        // 레이아웃을 액티비티에 설정
        setContentView(layout);

        // NFC 지원 여부 확인
        if (nfcAdapter == null) {
            uidText.setText("NFC가 지원되지 않습니다.");
            return;
        }

        // NFC 인텐트 설정
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        intentFiltersArray = new IntentFilter[] {ndef};
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // NFC 태그를 받았을 때 UID를 추출하여 텍스트뷰에 표시
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                // UID 추출
                byte[] uid = tag.getId();
                StringBuilder uidHex = new StringBuilder();
                for (byte b : uid) {
                    uidHex.append(String.format("%02X", b));
                }
                uidText.setText("UID: " + uidHex.toString());

                // MIFARE Classic 카드의 특정 블록 읽기
                MifareClassic mifareClassic = MifareClassic.get(tag);
                try {
                    mifareClassic.connect();
                    // KeyA 설정
                    byte[] keyA = {(byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5};
                    boolean auth = mifareClassic.authenticateSectorWithKeyA(15, keyA); // 16번째 섹터, KeyA로 인증
                    if (auth) {
                        authStatusText.setText("Auth Status: true");
                        int blockIndex = mifareClassic.sectorToBlock(15) + 0; // 16번째 섹터의 첫 번째 블록, index는 0부터 시작
                        byte[] data = mifareClassic.readBlock(60); // 60번째 블록 읽기

                        // 블록 데이터를 16진수 문자열로 변환하여 전체 데이터 표시
                        StringBuilder blockDataHex = new StringBuilder();
                        for (byte b : data) {
                            blockDataHex.append(String.format("%02X ", b));
                        }
                        blockDataText.setText("Block Data: " + blockDataHex.toString());

                        // 카드 번호 추출 (6바이트를 ASCII로 변환)
                        byte[] cardNumberBytes = Arrays.copyOfRange(data, 4, 10);
                        String cardNumber = new String(cardNumberBytes, StandardCharsets.US_ASCII).trim(); // ASCII로 변환하여 트림
                        cardNumberText.setText("Card Number: " + cardNumber);
                    } else {
                        authStatusText.setText("Auth Status: false");
                        blockDataText.setText("Block Data: Authentication Failed");
                        cardNumberText.setText("Card Number: Authentication Failed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    blockDataText.setText("Block Data: Error reading block");
                    cardNumberText.setText("Card Number: Error reading block");
                } finally {
                    try {
                        mifareClassic.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // 기존 초기화 요청을 취소하고, 5초 후 초기화 요청
                handler.removeCallbacks(resetRunnable);
                handler.postDelayed(resetRunnable, 5000); // 5초 후에 텍스트 초기화
            }
        }
    }
}
