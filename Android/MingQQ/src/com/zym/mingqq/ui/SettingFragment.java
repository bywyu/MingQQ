package com.zym.mingqq.ui;

import java.util.ArrayList;
import java.util.List;

import com.zym.mingqq.AppData;
import com.zym.mingqq.LoginAccountInfo;
import com.zym.mingqq.LoginAccountList;
import com.zym.mingqq.R;
import com.zym.mingqq.Utils;
import com.zym.mingqq.qqclient.QQClient;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQCallBackMsg;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQStatus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class SettingFragment extends Fragment 
	implements OnItemClickListener, DialogInterface.OnClickListener {
	private ListView m_ListView;
	private SettingListAdapter m_ListAdapter;
	private QQClient m_QQClient;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setting,
				container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) { 
		super.onActivityCreated(savedInstanceState);
		initView();
	}
		
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
		
	private void initView() {
		m_QQClient = AppData.getAppData().getQQClient();
		
		m_ListView = (ListView)getActivity().findViewById(R.id.setting_listview);
		
    	String[] arrItemText = getResources().getStringArray(R.array.SettingListItemTextArray);
    	
    	List<SettingListItem> arrData = new ArrayList<SettingListItem>();
    	
        for (int i = 0; i < arrItemText.length; i++) {
        	SettingListItem data = new SettingListItem();
        	if (0 == i || i == arrItemText.length - 1) {
        		data.m_nType = SettingListItem.TYPE_MARGIN_S;
        	} else if (1 == i) {
        		data.m_nType = SettingListItem.TYPE_USERINFO;
        	} else {
        		data.m_nType = (!Utils.isEmptyStr(arrItemText[i]) ? 
        				SettingListItem.TYPE_CONTENT : SettingListItem.TYPE_MARGIN_M);
        	}
        	
        	data.m_strTitle = arrItemText[i];
            arrData.add(data);
        }
        
        m_ListAdapter = new SettingListAdapter(getActivity(), arrData);
        m_ListView.setAdapter(m_ListAdapter);
        
        m_ListView.setOnItemClickListener(this);
	}
	
	public void onTabChange() {
			
	}

	private void showLoginActivity() {
		getActivity().finish();
		
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("qq_num", m_QQClient.getQQNum());
		bundle.putString("qq_pwd", m_QQClient.getQQPwd());
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, 
			View view, int position, long id) {
		// TODO Auto-generated method stub
		int nHeaderCnt = m_ListView.getHeaderViewsCount();
		int nPos = position - nHeaderCnt;
		if (1 == nPos) {
			if (!m_QQClient.isOffline()) {
				if (m_QQClient.getStatus() != QQStatus.HIDDEN) {
					m_QQClient.changeStatus(QQStatus.HIDDEN);
					Toast.makeText(getActivity(), 
							R.string.hidden, Toast.LENGTH_LONG).show();
				}
				else {
					m_QQClient.changeStatus(QQStatus.ONLINE);
					Toast.makeText(getActivity(), 
							R.string.online, Toast.LENGTH_LONG).show();
				}
			}
		} else if (14 == nPos) {
			new AlertDialog.Builder(getActivity()) 
			.setTitle(R.string.confirm)
			.setMessage(R.string.exit_cur_account)
			.setPositiveButton(R.string.ok, this)
			.setNegativeButton(R.string.cancel, this)
			.show();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		switch (which) {
		case AlertDialog.BUTTON_POSITIVE:	// "确认"按钮
			LoginAccountList accountList = AppData.getAppData().getLoginAccountList();
			LoginAccountInfo accountInfo = accountList.getLastLoginAccountInfo();
			accountInfo.m_bAutoLogin = false;
	    	
			String strAppPath = AppData.getAppData().getAppPath();
	    	String strFileName = strAppPath + "LoginAccountList.dat"; 
	    	accountList.saveFile(strFileName);

			if (!m_QQClient.logout()) {
				showLoginActivity();
			}

			break;
		case AlertDialog.BUTTON_NEGATIVE:	// "取消"按钮
			dialog.cancel();
			break;
		default:
			break;
		}
	}
	
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case QQCallBackMsg.UPDATE_BUDDY_INFO:	// 更新好友信息
			m_ListAdapter.notifyDataSetChanged();
			break;
		case QQCallBackMsg.UPDATE_BUDDY_SIGN:	// 更新好友个性签名
			m_ListAdapter.notifyDataSetChanged();
			break;
		case QQCallBackMsg.UPDATE_BUDDY_HEADPIC:// 更新好友头像
			m_ListAdapter.notifyDataSetChanged();
			break;
		case QQCallBackMsg.CHANGE_STATUS_RESULT:// 改变状态返回
			m_ListAdapter.notifyDataSetChanged();
			break;
		case QQCallBackMsg.LOGOUT_RESULT:		// 注销返回
			showLoginActivity();
			break;
		default:
			break;
		}
	}
}
