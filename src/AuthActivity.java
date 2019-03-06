package com.moaPlatform.moa.auth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.moaPlatform.moa.R;

import org.moa.auth.userauth.android.api.AndroidIDMngProcess;
import org.moa.auth.userauth.android.api.MemberInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AuthActivity extends AppCompatActivity {

    private AndroidIDMngProcess androidIDMngProcess;
    private ServerSideAuth serverSideAuth;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        androidIDMngProcess = new AndroidIDMngProcess(this);
        serverSideAuth = new ServerSideAuth(this);

        initPinBtnListener();
        initFingerprintBtnListener();
        initWalletBtnListener();
    }

    private void initPinBtnListener() {
        Button createNonMemberPin = findViewById(R.id.btn_non_member_pin);
        Button loginNonMemberPin = findViewById(R.id.btn_non_member_login);
        Button registerPin = findViewById(R.id.btn_register_pin);
        Button loginPin = findViewById(R.id.btn_login_pin);

        createNonMemberPin.setOnClickListener(v -> registerNonMemberPin());
        loginNonMemberPin.setOnClickListener(v -> loginNonMemberPin());
        registerPin.setOnClickListener(v -> registerMemberPin());
        loginPin.setOnClickListener(v -> loginMemberPin());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initFingerprintBtnListener() {
        Button registerFingerprint = findViewById(R.id.btn_register_fingerprint);
        Button loginFingerprint = findViewById(R.id.btn_login_fingerprint);

        registerFingerprint.setOnClickListener(v -> prepareRegisterMemberFingerprint());
        loginFingerprint.setOnClickListener(v -> prepareLoginMemberFingerprint());
    }

    /************************************************************************************************************************************
	 * Method Name : initWalletBtnListener()
     * params : 없음
     * return : 없음 	 
	 * 기능 설명 : Moa Wallet을 생성 및 조회를 위한 버튼 이벤트 처리하는 메소드 
	 ***********************************************************************************************************************************/
    private void initWalletBtnListener() {
        Button createWallet = findViewById(R.id.btn_create_wallet);				// 지갑 생성 버튼 Binding
        Button showWallet = findViewById(R.id.btn_show_wallet); 				// 지갑 조회 버튼 Binding

        createWallet.setOnClickListener(v -> createWallet()); 						// 지갑 생성 버튼 클릭 이벤트 등록
        showWallet.setOnClickListener(v -> showWallet()); 						// 지갑 조회 버튼 클릭 이벤트 등록
    }

    private void registerNonMemberPin() {
        // Step 1. App 구동 시 androidIDManager.dat 파일 존재 여부 체크
        boolean isFirstRun = !androidIDMngProcess.existControlInfoFile();

        // Step 2. READ_PHONE_STATE 권한 체크
        TedPermission.with(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        // Step 3. androidIDManager.dat 파일이 없는 경우 (App 최초 구동 시)
                        if (isFirstRun)
                            // Step 4. Unique ID를 생성하여 서버에 전송
                            serverSideAuth.registerNonMemberPIN(getUniqueDeviceID(), null);
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(AuthActivity.this, getResources().getString(R.string.msg_permission_denied) + "\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage(getResources().getString(R.string.msg_detail_permission_denied))
                .setPermissions(Manifest.permission.READ_PHONE_STATE)
                .check();
    }

    private void loginNonMemberPin() {
        // Step 1. 비회원 ID 얻기
        if (!androidIDMngProcess.existControlInfoFile())
            return;

        String base64NonMemberID = androidIDMngProcess.getMemberInfo(MemberInfo.Get.MEMBER_ID.getType());

        // Step 2. 비회원 로그인 요청
        serverSideAuth.loginNonMemberPIN(base64NonMemberID, null);
    }

    private void registerMemberPin() {
        // Step 1. ID Check
        EditText registerPinID = findViewById(R.id.et_register_pin_id);
        String pinID = registerPinID.getText().toString();

        // Step 2. PIN 값 전달
        EditText registerPinPW = findViewById(R.id.et_register_pin);
        String pinPW = registerPinPW.getText().toString();

        // Step 3. PIN 등록
        serverSideAuth.registerMemberPIN(pinID, pinPW);
    }

    private void loginMemberPin() {
        // Step 1. androidIDManager.dat 파일 체크
        boolean existControlInfoFile = androidIDMngProcess.existControlInfoFile();
        if (!existControlInfoFile)
            return;

        // Step 2. ID 조회 및 PW 입력
        EditText etLoginID = findViewById(R.id.et_register_pin_id);
        String pinID = androidIDMngProcess.getMemberInfo(MemberInfo.Get.MEMBER_ID.getType());
        etLoginID.setText(pinID);
        EditText loginPinPW = findViewById(R.id.et_register_pin);
        String pinPW = loginPinPW.getText().toString();

        // Step 3. 로그인
        serverSideAuth.loginMemberPIN(pinID, pinPW);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void prepareRegisterMemberFingerprint() {
        // Step 1. 지문 지원 관련 OS 버전 체크
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(this, "해당 기기는 지문 인식 기능을 지원하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 2. 지문 등록 가능 여부 체크
        serverSideAuth.checkRegisterFingerprint();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRegisterFingerprint(String base64AuthToken) {
        // Step 3. 지문 등록 요청
        serverSideAuth.registerMemberFingerprint(base64AuthToken);
    }

    private void prepareLoginMemberFingerprint() {
        // Step 1. 지문 지원 관련 OS 버전 체크
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(this, "해당 기기는 지문 인식 기능을 지원하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 2. 지문 로그인 가능 여부 체크
        serverSideAuth.checkLoginFingerprint();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onLoginFingerprint(String nonceOTP, String base64AuthToken) {
        // Step 3. 지문 로그인 요청
        serverSideAuth.loginMemberFingerprint(nonceOTP, base64AuthToken);
    }
	
	/************************************************************************************************************************************
	 * Method Name : private void createWallet()
     * params : 없음
     * return : 없음 	 
	 * 기능 설명 : 안저하게 Moa Wallet을 발급하기 위하여 Moa Crypto Core Module, 패스워드 기반 키 유도 매커니즘, 
	 * TEE 보안영역 및 Crypto Algorithm을 이용하여 Wallet용 키 쌍을 생성하고 Wallet용 공개키로 주소를 생성하며, 
	 * Wallet 용 개인키를 암호화 메커니즘을 이용하여 암호화한 후 안전하게 저장하는 메소드 
	 ***********************************************************************************************************************************/
    private void createWallet() {
        EditText et_password = findViewById(R.id.et_wallet_password); 				// 지갑 생성에 필요한 비밀번호 Component Binding
        String password = et_password.getText().toString(); 								// 패스워드 입력 시, 입력값 조회
        if (androidIDMngProcess.existWalletFile()) { 										// 지갑을 생성하기 전에 지갑 관련 파일이 존재 하는지 체크
            Toast.makeText(this, "지갑이 이미 생성되었습니다.", Toast.LENGTH_SHORT).show(); 	// 존재하는 경우, 지갑 생성하지 않음
        } else { // 지갑을 생성하지 않은 경우
            androidIDMngProcess.generateWalletInfo(password); 						// 지갑 생성
            Toast.makeText(this, "지갑 생성 성공", Toast.LENGTH_SHORT).show(); 	// 지갑 생성 완료 알림
        }
    }

	/************************************************************************************************************************************* 
	 * Method Name : private void createWallet()
     * params : 없음
     * return : 없음 	 
	 * 기능 설명 : Wallet 정보의 생성 유무 확인용 메소드
	 ************************************************************************************************************************************/
    private void showWallet() {
        if (!androidIDMngProcess.existWalletFile()) 										// 지갑 관련 파일이 존재하는지 체크
            return; 																				// 존재하지 않는 경우 보여주지 않음

        TextView tv_wallet = findViewById(R.id.tv_wallet_data); 							// 지갑 데이터를 보여줄 Component Binding
        String walletContent = androidIDMngProcess.getWalletContent(); 				// 지갑 데이터 조회
        tv_wallet.setText(walletContent); 														// 조회한 지갑 데이터를 보여줌
    }

}
