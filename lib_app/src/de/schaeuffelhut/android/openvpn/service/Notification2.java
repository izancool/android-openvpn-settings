/*
 * This file is part of OpenVPN-Settings.
 *
 * Copyright © 2009-2012  Friedrich Schäuffelhut
 *
 * OpenVPN-Settings is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenVPN-Settings is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenVPN-Settings.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Report bugs or new features at: http://code.google.com/p/android-openvpn-settings/
 * Contact the author at:          android.openvpn@schaeuffelhut.de
 */

package de.schaeuffelhut.android.openvpn.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;
import de.schaeuffelhut.android.openvpn.Intents;

import java.io.File;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-11-01
 */
public class Notification2
{
    private final OpenVpnServiceImpl mContext; //TODO: convert to ApplicationContext
    private final File mConfigFile;
    private final int mNotificationId;
    private final NotificationManager mNotificationManager;
    private final Handler mUiThreadHandler;

    public Notification2(OpenVpnServiceImpl mContext, File mConfigFile, int mNotificationId)
    {
        this.mContext = mContext;
        this.mConfigFile = mConfigFile;
        this.mNotificationId = mNotificationId;
        this.mNotificationManager = (NotificationManager) mContext.getSystemService( Context.NOTIFICATION_SERVICE);
        this.mUiThreadHandler = new Handler();
    }


    public void daemonStateChangedToStartUp()
    {
        mContext.sendStickyBroadcast(
                Intents.daemonStateChanged(
                        mConfigFile.getAbsolutePath(),
                        Intents.DAEMON_STATE_STARTUP
                )
        );
    }

    void daemonStateChangedToEnabled()
    {
        mContext.sendStickyBroadcast(
                Intents.daemonStateChanged(
                        mConfigFile.getAbsolutePath(),
                        Intents.DAEMON_STATE_ENABLED
                )
        );
    }

    void daemonStateChangedToDisabled()
    {
        mContext.sendStickyBroadcast(
                Intents.daemonStateChanged(
                        mConfigFile.getAbsolutePath(),
                        Intents.DAEMON_STATE_DISABLED
                )
        );
    }

    void sendPassphraseRequired()
    {
        Notifications.sendPassphraseRequired( mNotificationId, mContext, mNotificationManager, mConfigFile );
    }

    void sendUsernamePasswordRequired()
    {
        Notifications.sendUsernamePasswordRequired( mNotificationId, mContext, mConfigFile, mNotificationManager );
    }

    void notifyConnected()
    {
        Notifications.notifyConnected( mNotificationId, mContext, mNotificationManager, mConfigFile );
    }

    void notifyDisconnected()
    {
        Notifications.notifyDisconnected( mNotificationId, mContext, mNotificationManager, mConfigFile, "Connecting" );
    }

    void notifyBytes(String smallInOutPerSecString)
    {
        Notifications.notifyBytes( mNotificationId, mContext, mNotificationManager, mConfigFile, smallInOutPerSecString );
    }

    void cancel()
    {
        Notifications.cancel( mNotificationId, mContext );
    }

    void networkStateChanged(int oldState, int newState)
    {
        networkStateChanged( oldState, newState, System.currentTimeMillis(), null, null, null, null, null, null );
    }

    //TODO: change method signature to take a Bundle instead of nullable parameters
    void networkStateChanged(int oldState, int newState, long time, String info0ExtraName, String info0ExtraValue, String info1ExtraName, String info1ExtraValue, String info2ExtraName, String info2ExtraValue)
    {
        Intent intent = Intents.networkStateChanged(
                mConfigFile.getAbsolutePath(),
                newState,
                oldState,
                time
        );

        if (info0ExtraName != null)
            intent.putExtra( info0ExtraName, info0ExtraValue );
        if (info1ExtraName != null)
            intent.putExtra( info1ExtraName, info1ExtraValue );
        if (info2ExtraName != null)
            intent.putExtra( info2ExtraName, info2ExtraValue );

        mContext.sendStickyBroadcast( intent );
    }

    void toastMessage(final String message)
    {
        mUiThreadHandler.post( new Runnable()
        {
            public void run()
            {
                Toast.makeText( mContext, message, Toast.LENGTH_LONG ).show();
            }
        } );
    }

    void sendShareTunModule()
    {
        Notifications.sendShareTunModule( mContext, mNotificationManager );
    }

    public static void cancelShareTunModule(Context context)
    {
        Notifications.cancelShareTunModule( context );
    }
}
