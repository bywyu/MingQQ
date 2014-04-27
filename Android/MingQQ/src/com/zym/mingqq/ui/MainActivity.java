package com.zym.mingqq.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.zym.mingqq.AppData;
import com.zym.mingqq.HomeWatcher;
import com.zym.mingqq.HomeWatcher.OnHomePressedListener;
import com.zym.mingqq.R;
import com.zym.mingqq.qqclient.QQClient;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQCallBackMsg;

public class MainActivity extends FragmentActivity 
	implements OnClickListener, OnHomePressedListener {
	
	private View m_layMsg;
	private View m_layContacts;
	private View m_layNews;
	private View m_laySetting;
	private ImageView m_imgMsg;
	private ImageView m_imgContacts;
	private ImageView m_imgNews;
	private ImageView m_imgSetting;
	private TextView m_txtUnreadMsgCnt;
	
	private FragmentManager m_fragmentMgr;
	private MsgFragment m_fragmentMsg;
	private ContactsFragment m_fragmentContacts;
	private NewsFragment m_fragmentNews;
	private SettingFragment m_fragmentSetting;

	private Dialog m_dlgExit1;
	private Dialog m_dlgExit2;
	
	private QQClient m_QQClient;
	private int m_nCurSelTab = 0;
	private HomeWatcher mHomeWatcher;
	
	private Handler m_Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case QQCallBackMsg.BUDDY_MSG:
			case QQCallBackMsg.GROUP_MSG:
			case QQCallBackMsg.SESS_MSG:
			case QQCallBackMsg.SYS_GROUP_MSG:
				updateUnreadMsgCount();
				break;
			}
			
			switch (m_nCurSelTab) {
			case 0:
				if (m_fragmentMsg != null)
					m_fragmentMsg.handleMessage(msg);
				break;

			case 1:
				if (m_fragmentContacts != null)
					m_fragmentContacts.handleMessage(msg);
				break;
				
			case 3:
				if (m_fragmentSetting != null)
					m_fragmentSetting.handleMessage(msg);
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_QQClient.setCallBackHandler(m_Handler);
		setCurSelTab(m_nCurSelTab);
		updateUnreadMsgCount();
		AppData.getAppData().cancelNotify(1);
		mHomeWatcher = new HomeWatcher(this);
		mHomeWatcher.setOnHomePressedListener(this);
		mHomeWatcher.startWatch();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		m_QQClient.setNullCallBackHandler(m_Handler);
		mHomeWatcher.setOnHomePressedListener(null);
		mHomeWatcher.stopWatch();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeExitDlg();
	}	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (m_dlgExit1 != null)
				m_dlgExit1.show();
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			String strTicker = getString(R.string.bgrun);
			String strTitle = getString(R.string.app_name);
			String strText = getString(R.string.nonewmsg);
			AppData.getAppData().showNotify(1, this, 
					strTicker, strTitle, strText);
			moveTaskToBack(true);	// true对任何Activity都适用
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
		
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_layMsg:
			setCurSelTab(0);
			break;
		case R.id.main_layContacts:
			setCurSelTab(1);
			break;
		case R.id.main_layNews:
			setCurSelTab(2);
			break;
		case R.id.main_laySetting:
			setCurSelTab(3);
			break;
		case R.id.exitdlg1_bntExit:
			if (m_dlgExit1 != null && m_dlgExit1.isShowing())
				m_dlgExit1.dismiss();
			if (m_dlgExit2 != null)
				m_dlgExit2.show();
			break;
		case R.id.exitdlg2_btnCancel: {
			CheckBox checkBox = (CheckBox)m_dlgExit2.findViewById(R.id.exitdlg2_cboStillRecvMsg);
			checkBox.setChecked(false);
			if (m_dlgExit2 != null && m_dlgExit2.isShowing())
				m_dlgExit2.dismiss();
			break;
		}
		case R.id.exitdlg2_btnOk: {
			CheckBox checkBox = (CheckBox)m_dlgExit2.findViewById(R.id.exitdlg2_cboStillRecvMsg);
			boolean bChecked = checkBox.isChecked();
			if (m_dlgExit2 != null && m_dlgExit2.isShowing())
				m_dlgExit2.dismiss();
			if (!bChecked) {
				AppData.getAppData().cancelNotify(1);
				m_QQClient.logout();
			} else {
				String strTicker = getString(R.string.bgrun);
				String strTitle = getString(R.string.app_name);
				String strText = getString(R.string.nonewmsg);
				AppData.getAppData().showNotify(1, this, 
						strTicker, strTitle, strText);
			}
			finish();
			break;
		}
		default:
			break;
		}
	}

	private void initView() {
		m_QQClient = AppData.getAppData().getQQClient();
		m_QQClient.setCallBackHandler(m_Handler);
		
		m_layMsg = findViewById(R.id.main_layMsg);
		m_layContacts = findViewById(R.id.main_layContacts);
		m_layNews = findViewById(R.id.main_layNews);
		m_laySetting = findViewById(R.id.main_laySetting);
		m_imgMsg = (ImageView)findViewById(R.id.main_imgMsg);
		m_imgContacts = (ImageView)findViewById(R.id.main_imgContacts);
		m_imgNews = (ImageView)findViewById(R.id.main_imgNews);
		m_imgSetting = (ImageView)findViewById(R.id.main_imgSetting);
		m_txtUnreadMsgCnt = (TextView)findViewById(R.id.main_txtUnreadMsgCnt);
		
		m_layMsg.setOnClickListener(this);
		m_layContacts.setOnClickListener(this);
		m_layNews.setOnClickListener(this);
		m_laySetting.setOnClickListener(this);
		
		initExitDlg();
		
		m_fragmentMgr = getSupportFragmentManager();
		setCurSelTab(0);
	}
	
	private void setCurSelTab(int nIndex) {
		clearSelState();
		
		FragmentTransaction transaction = m_fragmentMgr.beginTransaction();
		hideAllFragment(transaction);
		switch (nIndex) {
		case 0:
			m_imgMsg.setImageResource(R.drawable.skin_tab_icon_conversation_selected);
			if (null == m_fragmentMsg) {
				m_fragmentMsg = new MsgFragment();
				transaction.add(R.id.main_frmContent, m_fragmentMsg);
			} else {
				transaction.show(m_fragmentMsg);
			}
			m_fragmentMsg.onTabChange();
			break;
		case 1:
			m_imgContacts.setImageResource(R.drawable.skin_tab_icon_contact_selected);
			if (null == m_fragmentContacts) {
				m_fragmentContacts = new ContactsFragment();
				transaction.add(R.id.main_frmContent, m_fragmentContacts);
			} else {
				transaction.show(m_fragmentContacts);
			}
			m_fragmentContacts.onTabChange();
			break;
		case 2:
			m_imgNews.setImageResource(R.drawable.skin_tab_icon_plugin_selected);
			if (null == m_fragmentNews) {
				m_fragmentNews = new NewsFragment();
				transaction.add(R.id.main_frmContent, m_fragmentNews);
			} else {
				transaction.show(m_fragmentNews);
			}
			break;
		case 3:
		default:
			m_imgSetting.setImageResource(R.drawable.skin_tab_icon_setup_selected);
			if (null == m_fragmentSetting) {
				m_fragmentSetting = new SettingFragment();
				transaction.add(R.id.main_frmContent, m_fragmentSetting);
			} else {
				transaction.show(m_fragmentSetting);
			}
			m_fragmentSetting.onTabChange();
			nIndex = 3;
			break;
		}
		transaction.commit();
		m_nCurSelTab = nIndex;
	}

	private void clearSelState() {
		m_imgMsg.setImageResource(R.drawable.skin_tab_icon_conversation_normal);
		m_imgContacts.setImageResource(R.drawable.skin_tab_icon_contact_normal);
		m_imgNews.setImageResource(R.drawable.skin_tab_icon_plugin_normal);
		m_imgSetting.setImageResource(R.drawable.skin_tab_icon_setup_normal);
	}

	private void hideAllFragment(FragmentTransaction transaction) {
		if (m_fragmentMsg != null) {
			transaction.hide(m_fragmentMsg);
		}
		if (m_fragmentContacts != null) {
			transaction.hide(m_fragmentContacts);
		}
		if (m_fragmentNews != null) {
			transaction.hide(m_fragmentNews);
		}
		if (m_fragmentSetting != null) {
			transaction.hide(m_fragmentSetting);
		}
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
	
	private void initExitDlg() {
		m_dlgExit1 = new MyDialog(this, R.style.dialog, R.layout.exitdlg1);
		
		Button btnExit = (Button)m_dlgExit1.findViewById(R.id.exitdlg1_bntExit);
		btnExit.setOnClickListener(this);
		
		Window win = m_dlgExit1.getWindow();
		WindowManager.LayoutParams params = win.getAttributes();
		
		int cxScreen = getScreenWidth(this);
		int cyScreen = getScreenHeight(this);
		
		int cy = (int)getResources().getDimension(R.dimen.cyexitdlg);
		int nLRMargin = (int)getResources().getDimension(R.dimen.exitdlg_lr_margin);
		int nBMargin = (int)getResources().getDimension(R.dimen.exitdlg_b_margin);
		
		params.x = nLRMargin;
		params.y = (cyScreen-cy)/2-nBMargin;
		params.width = cxScreen;
		params.height = cy;
		
		m_dlgExit1.setCanceledOnTouchOutside(true);	// 设置点击Dialog外部任意区域关闭Dialog
		
		m_dlgExit2 = new Dialog(this, R.style.dialog);
		m_dlgExit2.setContentView(R.layout.exitdlg2);
		
		Button btnCancel = (Button)m_dlgExit2.findViewById(R.id.exitdlg2_btnCancel);
		Button btnOk = (Button)m_dlgExit2.findViewById(R.id.exitdlg2_btnOk);
		
		btnCancel.setOnClickListener(this);
		btnOk.setOnClickListener(this);

		m_dlgExit2.setCanceledOnTouchOutside(true);	// 设置点击Dialog外部任意区域关闭Dialog
	}
	
	private void closeExitDlg() {
		if (m_dlgExit1 != null && m_dlgExit1.isShowing())
			m_dlgExit1.dismiss();
		
		if (m_dlgExit2 != null && m_dlgExit2.isShowing())
			m_dlgExit2.dismiss();
	}
		
	// 更新未读消息总数标签
	private void updateUnreadMsgCount() {
		int nCount = m_QQClient.getMessageList().getUnreadMsgCount();
		if (nCount > 0) {
			String strText;
			if (nCount > 99)
				strText = "99+";
			else
				strText = String.valueOf(nCount);
			m_txtUnreadMsgCnt.setText(strText);
			m_txtUnreadMsgCnt.setVisibility(View.VISIBLE);
		} else {
			m_txtUnreadMsgCnt.setText("");
			m_txtUnreadMsgCnt.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onHomePressed() {
		String strTicker = getString(R.string.bgrun);
		String strTitle = getString(R.string.app_name);
		String strText = getString(R.string.nonewmsg);
		AppData.getAppData().showNotify(1, this, 
				strTicker, strTitle, strText);
	}

	@Override
	public void onHomeLongPressed() {
		// do nothing
	}
}
