package com.zym.mingqq.ui;

import com.zym.mingqq.AppData;
import com.zym.mingqq.LoginAccountList;
import com.zym.mingqq.R;
import com.zym.mingqq.qqclient.QQClient;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQCallBackMsg;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQLoginResultCode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class VerifyCodeActivity extends Activity 
	implements OnClickListener, TextWatcher {
	private TextView m_txtCancel;
	private Button m_btnFinish;
	private ImageView m_imgVC;
	private ProgressBar m_prgLogining;
	private EditText m_edtVC;
	private QQClient m_QQClient;

	private Handler m_Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			m_prgLogining.setVisibility(View.GONE);
			switch (msg.what) {
			case QQCallBackMsg.LOGIN_RESULT:
				if (msg.arg1 == QQLoginResultCode.SUCCESS) {	// 登录成功
					LoginAccountList accountList = AppData.getAppData().getLoginAccountList();
			    	int nPos = accountList.add(m_QQClient.getQQNum(), 
							m_QQClient.getQQPwd(), m_QQClient.getLoginStatus(), true, true);
			    	accountList.setLastLoginUser(nPos);
			    	
			    	String strAppPath = AppData.getAppData().getAppPath();
			    	String strFileName = strAppPath + "LoginAccountList.dat"; 
			    	accountList.saveFile(strFileName);

					m_QQClient.setNullCallBackHandler(m_Handler);
					startActivity(new Intent(VerifyCodeActivity.this, MainActivity.class));
					finish();
				} else if (msg.arg1 == QQLoginResultCode.FAILED) {	// 登录失败
					Toast.makeText(getBaseContext(), 
							R.string.login_failed, Toast.LENGTH_LONG).show();
					m_QQClient.setNullCallBackHandler(m_Handler);
					startActivity(new Intent(VerifyCodeActivity.this, LoginActivity.class));
					finish();
				} else if (msg.arg1 == QQLoginResultCode.PASSWORD_ERROR) {	// 密码错误
					Toast.makeText(getBaseContext(), 
							R.string.id_or_pwd_err, Toast.LENGTH_LONG).show();
					m_QQClient.setNullCallBackHandler(m_Handler);
					startActivity(new Intent(VerifyCodeActivity.this, LoginActivity.class));
					finish();
				} else if (msg.arg1 == QQLoginResultCode.NEED_VERIFY_CODE) {	// 需要输入验证码
					byte[] bytData = m_QQClient.getVerifyCodePic();
					Bitmap bmp = BitmapFactory.decodeByteArray(bytData, 0, bytData.length);
					m_imgVC.setImageBitmap(bmp);
				} else if (msg.arg1 == QQLoginResultCode.VERIFY_CODE_ERROR) {	// 验证码错误
					Toast.makeText(getBaseContext(), 
							R.string.vc_err, Toast.LENGTH_LONG).show();
					byte[] bytData = m_QQClient.getVerifyCodePic();
					Bitmap bmp = BitmapFactory.decodeByteArray(bytData, 0, bytData.length);
					m_imgVC.setImageBitmap(bmp);
				} 
				
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verifycode);
		initView();
	}
	
	private void initView() {
		m_QQClient = AppData.getAppData().getQQClient();
		m_QQClient.setCallBackHandler(m_Handler);
		
		m_txtCancel = (TextView)findViewById(R.id.vc_txtCancel);
		m_btnFinish = (Button)findViewById(R.id.vc_btnFinish);
		m_imgVC = (ImageView)findViewById(R.id.vc_imgVC);
		m_prgLogining = (ProgressBar)findViewById(R.id.vc_prgLogining);
		m_edtVC = (EditText)findViewById(R.id.vc_edtVC);
		
		m_txtCancel.setOnClickListener(this);
		m_btnFinish.setOnClickListener(this);
		m_edtVC.addTextChangedListener(this);
		
		byte[] bytData = m_QQClient.getVerifyCodePic();
		Bitmap bmp = BitmapFactory.decodeByteArray(bytData, 0, bytData.length);
		m_imgVC.setImageBitmap(bmp);
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		if (m_edtVC.getText().length() >= 4)
			m_btnFinish.setEnabled(true);
		else
			m_btnFinish.setEnabled(false);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.vc_txtCancel:	// “取消”
			m_QQClient.setCallBackHandler(null);
			startActivity(new Intent(VerifyCodeActivity.this, LoginActivity.class));
			finish();
			break;
			
		case R.id.vc_btnFinish:	// “完成”按钮
			String strVC = m_edtVC.getText().toString();
			if (strVC.length() < 4)
				return;
			m_prgLogining.setVisibility(View.VISIBLE);
			m_QQClient.setVerifyCode(strVC);
			m_QQClient.login();
			break;
		}
	}
}
