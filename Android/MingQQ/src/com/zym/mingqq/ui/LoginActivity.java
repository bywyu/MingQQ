package com.zym.mingqq.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zym.mingqq.AppData;
import com.zym.mingqq.LoginAccountList;
import com.zym.mingqq.R;
import com.zym.mingqq.qqclient.QQClient;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQCallBackMsg;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQLoginResultCode;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQStatus;

public class LoginActivity extends Activity implements OnClickListener {
	private Animation my_Translate;		// 位移动画
	private Animation my_Rotate;		// 旋转动画
	private LinearLayout rl;
	private ImageView m_imgArrow;
	private ImageView m_imgAvatar;
	private EditText m_edtNum;
	private EditText m_edtPwd;
	private Button m_btnLogin;
	private Dialog m_dlgLogining;
	private QQClient m_QQClient;

	private Handler m_Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case QQCallBackMsg.LOGIN_RESULT:
				closeLoginingDlg();
				if (msg.arg1 == QQLoginResultCode.SUCCESS) {	// 登录成功
					LoginAccountList accountList = AppData.getAppData().getLoginAccountList();
			    	int nPos = accountList.add(m_QQClient.getQQNum(), 
							m_QQClient.getQQPwd(), m_QQClient.getLoginStatus(), true, true);
			    	accountList.setLastLoginUser(nPos);
			    	
			    	String strAppPath = AppData.getAppData().getAppPath();
			    	String strFileName = strAppPath + "LoginAccountList.dat"; 
			    	accountList.saveFile(strFileName);

					m_QQClient.setNullCallBackHandler(null);
					startActivity(new Intent(LoginActivity.this, MainActivity.class));
					finish();
				} else if (msg.arg1 == QQLoginResultCode.FAILED) {	// 登录失败
					Toast.makeText(getBaseContext(), 
							R.string.login_failed, Toast.LENGTH_LONG).show();
				} else if (msg.arg1 == QQLoginResultCode.PASSWORD_ERROR) {	// 密码错误
					Toast.makeText(getBaseContext(), 
							R.string.id_or_pwd_err, Toast.LENGTH_LONG).show();
				} else if (msg.arg1 == QQLoginResultCode.NEED_VERIFY_CODE
						|| msg.arg1 == QQLoginResultCode.VERIFY_CODE_ERROR) {	// 需要输入验证码
					m_QQClient.setNullCallBackHandler(null);
					startActivity(new Intent(LoginActivity.this, VerifyCodeActivity.class));
					finish();
				} 
				break;
				
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
		anim();
		rl.startAnimation(my_Translate);	// 载人时的动画
		
		Intent intent = getIntent();
        Bundle bundle = intent.getExtras();  
        if (bundle != null) {
            String strQQNum = bundle.getString("qq_num");
            String strQQPwd = bundle.getString("qq_pwd");
            m_edtNum.setText(strQQNum);
            m_edtPwd.setText(strQQPwd);
        }
	}

	@Override
    protected void onDestroy(){  
        super.onDestroy();
        m_QQClient.setNullCallBackHandler(m_Handler);
    }
	
	private void initView() {
		m_QQClient = AppData.getAppData().getQQClient();
		m_QQClient.setCallBackHandler(m_Handler);
		
		rl = (LinearLayout)findViewById(R.id.rl);
		m_imgArrow = (ImageView)findViewById(R.id.login_imgDropdownArrow);
		m_imgAvatar = (ImageView)findViewById(R.id.login_imgAvatar);
		m_edtNum = (EditText)findViewById(R.id.login_edtNum);
		m_edtPwd = (EditText)findViewById(R.id.login_edtPwd);
		m_btnLogin = (Button)findViewById(R.id.login_btnLogin);
		
		m_imgArrow.setOnClickListener(this);
		m_btnLogin.setOnClickListener(this);
				
		initLoginingDlg();
	}

	private void anim() {
		my_Translate = AnimationUtils.loadAnimation(this, R.anim.my_translate);
		my_Rotate = AnimationUtils.loadAnimation(this, R.anim.my_rotate);
	}

	private int getScreenWidth(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	private int getScreenHeight(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}
	
	private void initLoginingDlg() {
		m_dlgLogining = new Dialog(this, R.style.dialog);
		m_dlgLogining.setContentView(R.layout.loginingdlg);
		
		Window win = m_dlgLogining.getWindow();
		WindowManager.LayoutParams params = win.getAttributes();
		
		int cxScreen = getScreenWidth(this);
		int cyScreen = getScreenHeight(this);
		
		int cy = (int)getResources().getDimension(R.dimen.cyloginingdlg);
		int lrMargin = (int)getResources().getDimension(R.dimen.loginingdlg_lr_margin);
		int tMargin = (int)getResources().getDimension(R.dimen.loginingdlg_t_margin);
		
		params.x = -(cxScreen-lrMargin*2)/2;
		params.y = (-(cyScreen-cy)/2)+tMargin;
		params.width = cxScreen;
		params.height = cy;
		
		m_dlgLogining.setCanceledOnTouchOutside(true);	//设置点击Dialog外部任意区域关闭Dialog
		//m_dlgLogining.setCancelable(false);		// 设置为false，按返回键不能退出
	}
	
	private void showLoginingDlg() {
		if (m_dlgLogining != null)
			m_dlgLogining.show();
	}
	
	private void closeLoginingDlg() {
		if (m_dlgLogining != null && m_dlgLogining.isShowing())
			m_dlgLogining.dismiss();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_imgDropdownArrow:
			m_imgArrow.startAnimation(my_Rotate);
			break;
			
		case R.id.login_btnLogin:	// “登录”按钮
			String strQQNum = m_edtNum.getText().toString();
			String strQQPwd = m_edtPwd.getText().toString();
			
			if (null == strQQNum || strQQNum.length() <= 0) {
				Toast.makeText(getBaseContext(), 
						R.string.enter_id, Toast.LENGTH_LONG).show();
				return;
			}
			
			if (null == strQQPwd || strQQPwd.length() <= 0) {
				Toast.makeText(getBaseContext(), 
						R.string.enter_pwd, Toast.LENGTH_LONG).show();
				return;
			}
			
			if (strQQNum.length() > 15) {
				Toast.makeText(getBaseContext(), 
						R.string.enter_id_toolong, Toast.LENGTH_LONG).show();
				return;
			}
			
			m_QQClient.setUser(strQQNum, strQQPwd);
			m_QQClient.setLoginStatus(QQStatus.ONLINE);
			m_QQClient.login();
			
			showLoginingDlg();
			
			break;
		}
	}
}
