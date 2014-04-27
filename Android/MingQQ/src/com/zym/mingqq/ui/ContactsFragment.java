package com.zym.mingqq.ui;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.zym.mingqq.R;
import com.zym.mingqq.AppData;
import com.zym.mingqq.Utils;
import com.zym.mingqq.qqclient.QQClient;
import com.zym.mingqq.qqclient.protocol.protocoldata.BuddyInfo;
import com.zym.mingqq.qqclient.protocol.protocoldata.BuddyList;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQCallBackMsg;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ExpandableListView.OnChildClickListener;

public class ContactsFragment extends Fragment 
	implements OnClickListener, OnChildClickListener {
	private Button m_btnBuddyTeam;
	private Button m_btnAllBuddy;
	private ImageButton m_btnGroup;
	private LinearLayout m_headerBar;
	private PullToRefreshExpandableListView mListView = null;
    private BuddyListAdapter m_blistAdapter = null;
    private QQClient m_QQClient;
    private BuddyList m_buddyList;	
	    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contacts,
				null);
		mListView = (PullToRefreshExpandableListView)
				view.findViewById(R.id.expandableListView);
        m_headerBar = (LinearLayout)inflater.inflate(
        		R.layout.buddy_list_header, null);
		mListView.getRefreshableView().addHeaderView(m_headerBar);
		return view;
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
        m_buddyList = m_QQClient.getBuddyList();

		m_btnBuddyTeam = (Button)getActivity().findViewById(R.id.contacts_btnBuddyTeam);
		m_btnAllBuddy = (Button)getActivity().findViewById(R.id.contacts_btnAllBuddy);
		mListView = (PullToRefreshExpandableListView)getActivity().findViewById(R.id.expandableListView);
		m_btnGroup = (ImageButton)getActivity().findViewById(R.id.contacts_btnGroup);
				
		m_btnBuddyTeam.setOnClickListener(this);
		m_btnAllBuddy.setOnClickListener(this);
		m_btnGroup.setOnClickListener(this);

        ExpandableListView actualListView = mListView.getRefreshableView();
        actualListView.setGroupIndicator(null);
        m_blistAdapter = new BuddyListAdapter(getActivity(), m_buddyList);
        actualListView.setAdapter(m_blistAdapter);
        actualListView.setDescendantFocusability(
        		ExpandableListView.FOCUS_AFTER_DESCENDANTS);
        actualListView.setOnChildClickListener(this);
        
        mListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
        mListView.getLoadingLayoutProxy().setReleaseLabel("释放立即刷新");
        mListView.getLoadingLayoutProxy().setRefreshingLabel("正在刷新...");
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel("");

        mListView.setOnRefreshListener(new OnRefreshListener<ExpandableListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
				// Do work to refresh the list here.
				new GetDataTask().execute();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.contacts_btnBuddyTeam:
			m_btnBuddyTeam.setBackgroundResource(R.drawable.skin_header_tab_left_pressed);
			m_btnAllBuddy.setBackgroundResource(R.drawable.skin_header_tab_right_normal);
			m_btnBuddyTeam.setTextColor(getResources().getColor(R.color.white));
			m_btnAllBuddy.setTextColor(getResources().getColor(R.color.blue));
			break;
		case R.id.contacts_btnAllBuddy:
			m_btnBuddyTeam.setBackgroundResource(R.drawable.skin_header_tab_left_normal);
			m_btnAllBuddy.setBackgroundResource(R.drawable.skin_header_tab_right_pressed);
			m_btnBuddyTeam.setTextColor(getResources().getColor(R.color.blue));
			m_btnAllBuddy.setTextColor(getResources().getColor(R.color.white));
			break;
		case R.id.contacts_btnGroup:
			m_QQClient.setNullCallBackHandler(null);
			startActivity(new Intent(getActivity(), GroupActivity.class));
			break;
		default:
			break;
		}
	}
	
    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
    	BuddyInfo buddyInfo = m_buddyList.getBuddy(groupPosition, childPosition);
    	if (null == buddyInfo)
    		return false;

		Intent intent = new Intent(getActivity(), ChatActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("type", ChatActivity.IS_BUDDY);
		bundle.putInt("useruin", m_QQClient.getUserInfo().m_nQQUin);
        bundle.putString("username", m_QQClient.getUserInfo().m_strNickName);
        bundle.putInt("groupcode", 0);
        bundle.putInt("groupid", 0);
        bundle.putInt("groupnum", 0);
        bundle.putInt("qquin", buddyInfo.m_nQQUin);
    	bundle.putInt("qqnum", buddyInfo.m_nQQNum);
    	if (!Utils.isEmptyStr(buddyInfo.m_strMarkName))
    		bundle.putString("buddyname", buddyInfo.m_strMarkName);
    	else
    		bundle.putString("buddyname", buddyInfo.m_strNickName);
        intent.putExtras(bundle);
		startActivity(intent);
    	
        return true;
    }

	public void onTabChange() {
		
	}
	
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case QQCallBackMsg.UPDATE_BUDDY_LIST:	// 更新好友列表
		       m_blistAdapter.notifyDataSetChanged();
			break;
		case QQCallBackMsg.UPDATE_BUDDY_NUMBER:	// 更新好友号码
			m_blistAdapter.notifyDataSetChanged();
			break;
		case QQCallBackMsg.UPDATE_BUDDY_HEADPIC:// 更新好友头像
			m_blistAdapter.notifyDataSetChanged();
			break;
		case QQCallBackMsg.UPDATE_BUDDY_SIGN:	// 更新好友签名
			m_blistAdapter.notifyDataSetChanged();
			break;
		case QQCallBackMsg.STATUS_CHANGE_MSG:
			m_blistAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
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
			mListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}
}
