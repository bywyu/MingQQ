package com.zym.mingqq;

import java.util.ArrayList;

import com.zym.mingqq.AppData;
import com.zym.mingqq.qqclient.QQClient;
import com.zym.mingqq.qqclient.protocol.protocoldata.BuddyInfo;
import com.zym.mingqq.qqclient.protocol.protocoldata.BuddyList;
import com.zym.mingqq.qqclient.protocol.protocoldata.BuddyMessage;
import com.zym.mingqq.qqclient.protocol.protocoldata.Content;
import com.zym.mingqq.qqclient.protocol.protocoldata.ContentType;
import com.zym.mingqq.qqclient.protocol.protocoldata.GroupInfo;
import com.zym.mingqq.qqclient.protocol.protocoldata.GroupList;
import com.zym.mingqq.qqclient.protocol.protocoldata.GroupMessage;
import com.zym.mingqq.qqclient.protocol.protocoldata.QQCallBackMsg;
import com.zym.mingqq.qqclient.protocol.protocoldata.SessMessage;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class QQService extends Service {
	private QQClient m_QQClient;
	private LoginAccountList m_accountList;
	private FaceList m_faceList;				// 表情列表
	private String m_strAppPath;
	private boolean m_bInit;
	private int m_nNewMsgCnt;
	
	private Handler m_handlerProxy = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			m_QQClient.handleProxyMsg(msg);
			if (AppData.getAppData().isShowNotify()) {
				switch (msg.what) {
				case QQCallBackMsg.BUDDY_MSG: {			// 好友消息
					BuddyMessage buddyMsg = (BuddyMessage)msg.obj;
					if (buddyMsg != null) {
						m_nNewMsgCnt++;
						String strMsg = formatContent(buddyMsg.m_arrContent);
						BuddyList buddyList = m_QQClient.getBuddyList();
						BuddyInfo buddyInfo = buddyList.getBuddy(buddyMsg.m_nFromUin);
						if (buddyInfo != null) {
							if (!Utils.isEmptyStr(buddyInfo.m_strMarkName))
								strMsg = buddyInfo.m_strMarkName + ":" + strMsg;
							else
								strMsg = buddyInfo.m_strNickName + ":" + strMsg;
						}
						String strTitle = getString(R.string.app_name);
						String strText = getString(R.string.newmsg_1);
						strText += m_nNewMsgCnt;
						strText += getString(R.string.newmsg_2);
						AppData.getAppData().showNotify(1, 
								QQService.this, strMsg, strTitle, strText);
					}
					break;
				}
				case QQCallBackMsg.GROUP_MSG: {		// 群消息					
					GroupMessage groupMsg = (GroupMessage)msg.obj;
					if (groupMsg != null) {
						m_nNewMsgCnt++;
						String strMsg = formatContent(groupMsg.m_arrContent);
						int nGroupCode = msg.arg1;
						int nQQUin = groupMsg.m_nSendUin;
						GroupList groupList = m_QQClient.getGroupList();
						GroupInfo groupInfo = groupList.getGroupByCode(nGroupCode);
						if (groupInfo != null) {
							BuddyInfo buddyInfo = groupInfo.getMemberByUin(nQQUin);
							if (buddyInfo != null) {
								if (!Utils.isEmptyStr(buddyInfo.m_strGroupCard))
									strMsg = buddyInfo.m_strGroupCard + "(" + groupInfo.m_strName + "):" + strMsg;
								else
									strMsg = buddyInfo.m_strNickName + "(" + groupInfo.m_strName + "):" + strMsg;
							}
						}
						String strTitle = getString(R.string.app_name);
						String strText = getString(R.string.newmsg_1);
						strText += m_nNewMsgCnt;
						strText += getString(R.string.newmsg_2);
						AppData.getAppData().showNotify(1, 
								QQService.this, strMsg, strTitle, strText);	
					}
					break;
				}
				case QQCallBackMsg.SESS_MSG: {			// 群成员消息
					SessMessage sessMsg = (SessMessage)msg.obj;
					if (sessMsg != null) {
						m_nNewMsgCnt++;
						String strMsg = formatContent(sessMsg.m_arrContent);
						int nGroupCode = msg.arg1;
						int nQQUin = msg.arg2;
						GroupList groupList = m_QQClient.getGroupList();
						BuddyInfo buddyInfo = groupList.getGroupMemberByCode(nGroupCode, nQQUin);
						if (buddyInfo != null) {
							if (!Utils.isEmptyStr(buddyInfo.m_strGroupCard))
								strMsg = buddyInfo.m_strGroupCard + ":" + strMsg;
							else
								strMsg = buddyInfo.m_strNickName + ":" + strMsg;
						}
						
						String strTitle = getString(R.string.app_name);
						String strText = getString(R.string.newmsg_1);
						strText += m_nNewMsgCnt;
						strText += getString(R.string.newmsg_2);
						AppData.getAppData().showNotify(1, 
								QQService.this, strMsg, strTitle, strText);
					}
					break;	
				}
				}
			} else {
				m_nNewMsgCnt = 0;
			}
			
			if (QQCallBackMsg.LOGOUT_RESULT == msg.what) {
				QQService.this.stopSelf();
			}
		}
	};
	
    @Override  
    public IBinder onBind(Intent arg0) {
        return null;
    }  
  
    @Override  
    public void onCreate() {
        m_QQClient = AppData.getAppData().getQQClient();
        m_accountList = AppData.getAppData().getLoginAccountList();
        m_faceList = AppData.getAppData().getFaceList();
        
        if (FileUtils.hasSDCard()) {
        	m_strAppPath = FileUtils.getSDCardDir() + "MingQQ/";
        } else {
        	m_strAppPath = FileUtils.getAppFilesDir(this);
        }
        AppData.getAppData().setAppPath(m_strAppPath);
                
        String strFileName = m_strAppPath + "LoginAccountList.dat"; 
        m_accountList.loadFile(strFileName);
		
        m_faceList.setDelBtnPicResId(R.drawable.delete_button);
        m_faceList.loadConfigFile(this, "FaceConfig.dat");
		
        String strUserFolder = m_strAppPath + "Users/";
        m_QQClient.setUserFolder(strUserFolder);        
        m_QQClient.setProxyHandler(m_handlerProxy);
        m_bInit = m_QQClient.init();
        if (!m_bInit)
        	m_QQClient.uninit();
    }
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {
        Handler handler = AppData.getAppData().getServiceHandler();
        if (null == handler)
        	return START_STICKY;
                
        if (!m_QQClient.isOffline()) {
        	handler.sendEmptyMessage(2);	// 已经登录
        } else if (m_bInit) {
        	handler.sendEmptyMessage(1);	// 初始化成功
        } else {						
        	handler.sendEmptyMessage(0);	// 初始化失败
        }
        return START_STICKY;
    }
    
    @Override  
    public void onDestroy() {
    	m_QQClient.setProxyHandler(null);
    			
        m_QQClient.uninit();
    }
    
	// "/f["系统表情id"] /c["自定义表情文件名"] /o["字体名称，大小，颜色，加粗，倾斜，下划线"]"
	public String formatContent(ArrayList<Content> arrContent) {
		String strMsg = "";

		for (int i = 0; i < arrContent.size(); i++) {
			Content content = arrContent.get(i);
			if (null == content)
				continue;

			if (ContentType.CONTENT_TYPE_TEXT == content.m_nType) {
				String strText = new String(content.m_strText);
				strText = strText.replace("/", "//");
				strMsg += strText;
			} else if (ContentType.CONTENT_TYPE_FACE == content.m_nType) {
				FaceInfo faceInfo = m_faceList.getFaceInfoById(content.m_nFaceId);
				if (faceInfo != null) {
					strMsg += faceInfo.m_strTip;
				} else {
					strMsg += "[表情]";
				}
			} else if (ContentType.CONTENT_TYPE_CUSTOM_FACE == content.m_nType
					|| ContentType.CONTENT_TYPE_OFF_PIC == content.m_nType) {
				strMsg += "[图片]";
			}
		}

		return strMsg;
	}
}