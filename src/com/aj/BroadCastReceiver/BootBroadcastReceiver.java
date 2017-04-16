package com.aj.BroadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aj.service.MsgService;
import com.aj.tools.SPUtils;

/**
 * 开机自启动的广播 接受到广播开启后台服务
 * Created by kevin on 15-8-14.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String action_boot="android.intent.action.BOOT_COMPLETED";
    String user,pwd;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)){
            user = (String) SPUtils.get(context, SPUtils.LOGIN_NAME, "", SPUtils.LOGIN_VALIDATE);
            pwd = (String) SPUtils.get(context, SPUtils.LOGIN_PASSWORD, "", SPUtils.LOGIN_VALIDATE);

            if (!user.isEmpty() && !pwd.isEmpty()) {
                Intent ootStartIntent = new Intent(context, MsgService.class);
                context.startService(ootStartIntent);
//
//                Intent longtermService = new Intent(context, LongTermService.class);
//                context.startService(longtermService);
            }
        }

    }

}