package com.zym.mingqq.ui;

import com.zym.mingqq.HomeWatcher;
import com.zym.mingqq.HomeWatcher.OnHomePressedListener;
import com.zym.mingqq.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.zym.mingqq.AppData;
import com.zym.mingqq.qqclient.QQClient;
import com.zym.mingqq.qqclient.protocol.protocoldata.GroupInfo;
import com.zym.mingqq.qqclient.protocol.protocoldata.GroupList;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQCallBackMsg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class GroupActivity extends Activity 
	implements OnItemClickListener, OnHomePressedListener {
	private TextView m_txtBack;
	private ImageButton m_btnAdd;
	private LinearLayout m_searchBar;
	private PullToRefreshListView m_lvGroupList;
	private GroupListAdapter m_glistAdapter;
	private QQClient m_QQClient;
	private GroupList m_groupList;
	private HomeWatcher mHomeWatcher;

	private Handler m_Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case QQCallBackMsg.UPDATE_GROUP_LIST:
				m_glistAdapter.notifyDataSetChanged();
				break;
			case QQCallBackMsg.UPDATE_GROUP_NUMBER: {
				int nGroupCode = msg.arg1;
				ListView actualListView = m_lvGroupList.getRefreshableView();
				int nIndex = m_groupList.getGroupIndexByCode(nGroupCode);
				if (nIndex != -1) {
					if (nIndex >= actualListView.getFirstVisiblePosition() 
							&& nIndex <= actualListView.getLastVisiblePosition()) {
						m_glistAdapter.notifyDataSetChanged();
					}
				}
				break;	
			}
			case QQCallBackMsg.UPDATE_GROUP_HEADPIC: {
				int nGroupCode = msg.arg1;
				ListView actualListView = m_lvGroupList.getRefreshableView();
				int nIndex = m_groupList.getGroupIndexByCode(nGroupCode);
				if (nIndex != -1) {
					if (nIndex >= actualListView.getFirstVisiblePosition() 
							&& nIndex <= actualListView.getLastVisiblePosition()) {
						m_glistAdapter.notifyDataSetChanged();
					}
				}
				break;
			}
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_QQClient.setCallBackHandler(m_Handler);
		AppData.getAppData().cancelNotify(1);
		mHomeWatcher = new HomeWatcher(this);
		mHomeWatcher.setOnHomePressedListener(this);
		mHomeWatcher.startWatch();
	}
	
	@Override
    protected void onStop(){  
        super.onStop();
        m_QQClient.setNullCallBackHandler(m_Handler);
        mHomeWatcher.setOnHomePressedListener(null);
		mHomeWatcher.stopWatch();
    }
	
	@Override
    protected void onDestroy(){  
        super.onDestroy();
    }
	
	private void initView() {
		m_QQClient = AppData.getAppData().getQQClient();
		m_QQClient.setCallBackHandler(m_Handler);
		m_groupList = m_QQClient.getGroupList();
		
		m_txtBack = (TextView)findViewById(R.id.group_txtBack);
		m_btnAdd = (ImageButton)findViewById(R.id.group_btnAdd);
		m_lvGroupList = (PullToRefreshListView)findViewById(R.id.group_lvGList);
		
		m_searchBar = (LinearLayout)((LayoutInflater)getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.searchbar, null);
		ListView actualListView = m_lvGroupList.getRefreshableView();
		actualListView.addHeaderView(m_searchBar);

		m_lvGroupList.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				new GetDataTask().execute();
			}
		});

		m_lvGroupList.getLoadingLayoutProxy().setPullLabel("下拉刷新");
		m_lvGroupList.getLoadingLayoutProxy().setReleaseLabel("释放立即刷新");
		m_lvGroupList.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
		m_lvGroupList.getLoadingLayoutProxy().setLastUpdatedLabel("");
		
		m_glistAdapter = new GroupListAdapter(this, m_groupList);
		actualListView.setAdapter(m_glistAdapter);
		actualListView.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, 
			View view, int position, long id) {
		// TODO Auto-generated method stub
		ListView actualListView = m_lvGroupList.getRefreshableView();
		int nHeaderCnt = actualListView.getHeaderViewsCount();
		int nPos = position - nHeaderCnt;
		GroupInfo groupInfo = m_groupList.getGroup(nPos);
		if (null == groupInfo)
			return;
		
		Intent intent = new Intent(this, ChatActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("type", ChatActivity.IS_GROUP);
		bundle.putInt("useruin", m_QQClient.getUserInfo().m_nQQUin);
        bundle.putString("username", m_QQClient.getUserInfo().m_strNickName);
        bundle.putInt("groupcode", groupInfo.m_nGroupCode);
        bundle.putInt("groupid", groupInfo.m_nGroupId);
    	bundle.putInt("groupnum", groupInfo.m_nGroupNumber);
    	bundle.putString("groupname", groupInfo.m_strName);
    	bundle.putInt("qquin", 0);
        bundle.putInt("qqnum", 0);
        bundle.putString("buddyname", "");
        intent.putExtras(bundle);
		startActivity(intent);
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

	private class GetDataTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			try {
				Thread.sleep(2*1000);
			} catch (InterruptedException e) {
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			m_lvGroupList.onRefreshComplete();

			super.onPostExecute(result);
		}
	}
}
