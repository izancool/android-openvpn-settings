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
package de.schaeuffelhut.android.openvpn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.*;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import com.bugsense.trace.BugSenseHandler;
import de.schaeuffelhut.android.openvpn.service.OpenVpnService;
import de.schaeuffelhut.android.openvpn.setup.prerequisites.PrerequisitesActivity;
import de.schaeuffelhut.android.openvpn.setup.prerequisites.ProbePrerequisites;
import de.schaeuffelhut.android.openvpn.tun.ShareTunActivity;
import de.schaeuffelhut.android.openvpn.util.*;

import java.io.File;
import java.util.ArrayList;

public class OpenVpnSettings extends PreferenceActivity implements ServiceConnection
{
	final static String TAG = "OpenVPN-Settings";

	private static final int REQUEST_CODE_IMPORT_FILES = 1;
	private static final int REQUEST_CODE_EDIT_CONFIG = 2;
	private static final int REQUEST_CODE_EDIT_CONFIG_PREFERENCES = 3;
	private static final int REQUEST_CODE_ADVANCED_SETTINGS = 4;

	private static final int DIALOG_HELP = 1;
	private static final int DIALOG_PLEASE_RESTART = 2;
	private static final int DIALOG_FIX_DNS = 3;
	private static final int DIALOG_CONTACT_AUTHOR = 4;
	private static final int DIALOG_CHANGELOG = 5;
	private static final int DIALOG_PREREQUISITES = 6;


	ArrayList<DaemonEnabler> mDaemonEnablers = new ArrayList<DaemonEnabler>(4);
	OpenVpnService mOpenVpnService = null;

	private int mCurrentContentView;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate()" );

        setContentView( mCurrentContentView = AdUtil.getAdSupportedListView( getApplicationContext() ) );

    	if ( Configuration.BUG_SENSE_API_KEY != null  )
    		BugSenseHandler.setup( this, Configuration.BUG_SENSE_API_KEY );

    	addPreferencesFromResource( R.xml.openvpn_settings );

        //TODO: write OpenVpnEnabled, see WifiEnabler => start stop OpenVpnService
        {
        	CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_ENABLED );
        	pref.setSummary( "" );
        	pref.setChecked( mOpenVpnService != null );
//        	pref.setSelectable( false );
        	pref.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if ( newValue == null )
					{
						/*noop*/;
					}
					else if ( (Boolean)newValue )
					{
						startService( new Intent(OpenVpnSettings.this, OpenVpnService.class) );
						if ( !bindService( new Intent( OpenVpnSettings.this, OpenVpnService.class ), OpenVpnSettings.this, 0 ) )
				        {
							Log.w(TAG, "Could not bind to ControlShell" );
				        }
					}
					else
					{
						stopService( new Intent(OpenVpnSettings.this, OpenVpnService.class) );
					}
					return false;
				}
			});
        }

		registerForContextMenu( getListView() );
		initToggles();

		// hack, check if service should run, but is actually stopped.
		// this happens if OpenVPN-Settings is killed (e.g. due to a tight memory situation).
		// On next restart we will detect that the service should run but is actually stopped.
		// There is no clean way to determine if the service has been started.
		if ( Preferences.getOpenVpnEnabled(this) && !OpenVpnService.isServiceStarted() )
			startService( new Intent( this, OpenVpnService.class ) );

		if ( !bindService( new Intent( this, OpenVpnService.class ), this, 0 ) )
        {
			Log.w(TAG, "Could not bind to ControlShell" );
        }

// was never unregistered and leaked a reference
// is it really necessary?
//		registerReceiver( 
//				new BroadcastReceiver() {
//					@Override public void onReceive(Context context, Intent intent) 
//					{
//						if ( !OpenVpnSettings.this.bindService( new Intent( OpenVpnSettings.this, OpenVpnService.class ), OpenVpnSettings.this, 0 ) )
//				        {
//							Log.w(TAG, "Could not bind to ControlShell" );
//				        }
//					}
//				},
//				new IntentFilter( Intents.OPEN_VPN_SERVICE_STARTED )
//		);

		if ( Util.applicationWasUpdated( this ) )
			showDialog( DIALOG_CHANGELOG );
        else
            openPrerequisitesActivityIfNeeded();
    }

    private void openPrerequisitesActivityIfNeeded()
    {
        if (!IocContext.get().fulfilsPrerequisites())
        {
            new AsyncTask<Void, Void, ProbePrerequisites>()
            {
                @Override
                protected ProbePrerequisites doInBackground(Void... voids)
                {
                    return IocContext.get().probePrerequisites( getApplicationContext() );
                }

                @Override
                protected void onPostExecute(ProbePrerequisites probePrerequisites)
                {
                    super.onPostExecute( probePrerequisites );
                    if (!probePrerequisites.isSuccess())
                        startActivity( new Intent( getApplicationContext(), PrerequisitesActivity.class ) );
                }
            }.execute();
        }
    }


    private void initToggles() {
		initToggles( Preferences.getConfigDir( this, PreferenceManager.getDefaultSharedPreferences(this) ) );
	}

    static class ConfigFilePreference extends CheckBoxPreference
    {
    	final File mConfig;
    	final DaemonEnabler mDaemonEnabler;
		public ConfigFilePreference(Context context, OpenVpnService openVpnService, File config) {
			super(context);
			setKey( Preferences.KEY_CONFIG_ENABLED( config ) );
			setTitle( Preferences.getConfigName(context, config) );
			setSummary( "Select to turn on OpenVPN tunnel");
			mConfig = config;
			mDaemonEnabler = new DaemonEnabler( context, openVpnService, this, config );
		}

    }
    private void initToggles(File configDir)
	{
		for(DaemonEnabler daemonEnabler : mDaemonEnablers ) {
			daemonEnabler.pause();
			daemonEnabler.setOpenVpnService( null );
		}
		mDaemonEnablers.clear();
		PreferenceCategory configurations = (PreferenceCategory) findPreference(Preferences.KEY_OPENVPN_CONFIGURATIONS);
		configurations.removeAll();

		for ( File config : Preferences.configs(configDir) )
		{
			ConfigFilePreference pref = new ConfigFilePreference( this, mOpenVpnService, config );
			configurations.addPreference(pref);
			mDaemonEnablers.add( pref.mDaemonEnabler );
		}

		if ( configurations.getPreferenceCount() == 0 ){
			EditTextPreference pref = new EditTextPreference(this);
			pref.setEnabled(false);
			pref.setPersistent(false);
			pref.setTitle( "No configuration found." );
			pref.setSummary( "Please copy your *.conf, *.ovpn, certificates, etc to\n" + Preferences.getExternalStorage(PreferenceManager.getDefaultSharedPreferences(this)) );
			configurations.addPreference( pref );
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch ( requestCode ) {
		case REQUEST_CODE_IMPORT_FILES: {
			CheckBoxPreference pref = (CheckBoxPreference)findPreference( Preferences.KEY_OPENVPN_USE_INTERNAL_STORAGE );
			switch ( resultCode ) {
			case ImportFiles.RESULT_OK:
				pref.setChecked( true );
				break;
			case ImportFiles.RESULT_CANCELED:
				pref.setChecked( false );
				break;
			default:
				throw new UnexpectedSwitchValueException( resultCode );
			}
		} break;

		case REQUEST_CODE_EDIT_CONFIG: {
			String filename = data == null ? null : data.getStringExtra( EditConfig.EXTRA_FILENAME );
			if ( filename != null && mOpenVpnService != null && mOpenVpnService.isDaemonStarted( new File(filename)) )
				showDialog( DIALOG_PLEASE_RESTART );
		} break;

		case REQUEST_CODE_EDIT_CONFIG_PREFERENCES: {
			String filename = data == null ? null : data.getStringExtra( EditConfig.EXTRA_FILENAME );
			if ( filename != null )
			{
				File config = new File(filename);
				if ( mOpenVpnService != null && mOpenVpnService.isDaemonStarted(config) )
					showDialog( DIALOG_PLEASE_RESTART );

				// refresh ConfigFilePreference
				PreferenceCategory configurations = (PreferenceCategory) findPreference(Preferences.KEY_OPENVPN_CONFIGURATIONS);
				ConfigFilePreference pref = (ConfigFilePreference)configurations.findPreference( Preferences.KEY_CONFIG_ENABLED( config ) );
				pref.setTitle( Preferences.getConfigName(getApplicationContext(), config) );
			}
		} break;

		case REQUEST_CODE_ADVANCED_SETTINGS: {
			// path to config might only be changed if no tunnel is up
			if ( mCurrentContentView != AdUtil.getAdSupportedListView( getApplicationContext() ) )
				setContentView( mCurrentContentView = AdUtil.getAdSupportedListView( getApplicationContext() ) );
			if ( mOpenVpnService == null || !mOpenVpnService.hasDaemonsStarted() )
				initToggles();
		}

		default:
			Log.w( TAG, String.format( "unexpected onActivityResult(%d, %d, %s) ", requestCode, resultCode, data ) );
			break;
		}
	}

    @Override
    protected void onResume() {
		super.onResume();
    	Log.d(TAG, "onResume()" );

		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.resume();
	}

    @Override
    protected void onPause() {
    	super.onPause();
    	Log.d(TAG, "onPause()" );

		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.pause();
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.d(TAG, "onDestroy()" );
    	if ( mOpenVpnService != null )
    		unbindService( this );
    }

	/*
	 * Menu
	 */

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.settings_menu, menu);
	    return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem( R.id.settings_menu_share_tun ).setVisible( Preferences.isTunSharingEnabled( getApplicationContext() ) );


//	    menu.findItem( R.id.configs_options_startall ).setVisible( configs.length > 0 );
//	    menu.findItem( R.id.configs_options_restartall ).setVisible( mControlShell.hasDaemonsStarted() );
//	    menu.findItem( R.id.configs_options_stopall ).setVisible( mOpenVpnService != null && mOpenVpnService.hasDaemonsStarted() );
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		case R.id.settings_menu_refresh:
			initToggles();
			for(DaemonEnabler daemonEnabler : mDaemonEnablers )
				daemonEnabler.resume();
			return true;

		case R.id.settings_menu_advanced: {
			Intent intent = new Intent(this, AdvancedSettings.class );
			intent.putExtra(AdvancedSettings.HAS_DAEMONS_STARTED, mOpenVpnService == null ? false : mOpenVpnService.hasDaemonsStarted() );
			startActivityForResult( intent, REQUEST_CODE_ADVANCED_SETTINGS );
			return true; }

		case R.id.settings_menu_fix_dns: {
			showDialog( DIALOG_FIX_DNS );
			return true; }

		case R.id.settings_menu_contact_author: {
			showDialog( DIALOG_CONTACT_AUTHOR );
			return true; }

		case R.id.settings_menu_share_tun: {
			startActivity( new Intent(this, ShareTunActivity.class ) );
			return true; }

		case R.id.settings_menu_help:
			showDialog( DIALOG_HELP );
			return true;

        case R.id.settings_menu_prerequisites: {
            startActivity( new Intent( this, PrerequisitesActivity.class ) );
            return true; }

//		case R.id.configs_options_import:
//			Intent intent = new Intent( getApplicationContext(), ImportFiles.class );
//			startActivityForResult(intent, REQUEST_CODE_IMPORT_FILES);
//			return true;
//		case R.id.configs_options_refresh:
//			initToggles();
//			return true;
//		case R.id.configs_options_startall:
//			return true;
//		case R.id.configs_options_restartall:
//			return true;
//		case R.id.configs_options_stopall:
//			return true;
//		case R.id.configs_options_settings:
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * Context Menu
	 */

	final static int CONTEXT_CONFIG_DISABLE = 1;
	final static int CONTEXT_CONFIG_ENABLE = 2;
	final static int CONTEXT_CONFIG_EDIT = 3;
	final static int CONTEXT_CONFIG_EDIT_PREFERENCES = 4;
	final static int CONTEXT_CONFIG_VIEW_LOG = 5;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		ConfigFilePreference configFilePref = getConfigPreferenceFromMenuInfo(menuInfo);
		if ( configFilePref != null )
		{
//			//Connect/Disconnect
//			if ( mOpenVpnService.isDaemonStarted( configFilePref.mConfig ) ){
//				menu.add( ContextMenu.NONE, CONTEXT_CONFIG_DISABLE, 1, "Disable" );
//			} else {
//				menu.add( ContextMenu.NONE, CONTEXT_CONFIG_ENABLE, 1, "Enable" );
//			}

			//Edit
			menu.add( ContextMenu.NONE, CONTEXT_CONFIG_EDIT, 2, "Edit Config File" );
			if ( Preferences.logFileFor( configFilePref.mConfig ).exists() )
				menu.add( ContextMenu.NONE, CONTEXT_CONFIG_VIEW_LOG, 2, "View Log File" );
			menu.add( ContextMenu.NONE, CONTEXT_CONFIG_EDIT_PREFERENCES, 2, "Preferences" );

			//Delete
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		ConfigFilePreference configFilePref = getConfigPreferenceFromMenuInfo(item.getMenuInfo());

		switch ( item.getItemId() ) {
		//TODO: think about it
//		case CONTEXT_CONFIG_ENABLE:
//			configFilePref.setEnabled(false);
//			mOpenVpnService.daemonStart( configFilePref.mConfig );
//			return true;
//		case CONTEXT_CONFIG_DISABLE:
//			configFilePref.setEnabled(false);
//			mOpenVpnService.daemonStop( configFilePref.mConfig );
//			return true;
		case CONTEXT_CONFIG_EDIT: {
			Intent i = new Intent(this, EditConfig.class);
			i.putExtra( EditConfig.EXTRA_FILENAME, configFilePref.mConfig.getAbsolutePath() );
	        startActivityForResult(i, REQUEST_CODE_EDIT_CONFIG );
		} return true;
		case CONTEXT_CONFIG_VIEW_LOG: {
			Intent i = new Intent(this, ViewLogFile.class);
			i.putExtra( EditConfig.EXTRA_FILENAME, configFilePref.mConfig.getAbsolutePath() );
	        startActivity( i );
		} return true;
		case CONTEXT_CONFIG_EDIT_PREFERENCES: {
			Intent i = new Intent(this, EditConfigPreferences.class);
			i.putExtra( EditConfigPreferences.EXTRA_FILENAME, configFilePref.mConfig.getAbsolutePath() );
	        startActivityForResult(i, REQUEST_CODE_EDIT_CONFIG_PREFERENCES );
		} return true;

		default:
			return false;
//			throw new UnexpectedSwitchValueException( item.getItemId() );
		}
	}

    private ConfigFilePreference getConfigPreferenceFromMenuInfo(ContextMenuInfo menuInfo) {
        if ((menuInfo == null) || !(menuInfo instanceof AdapterContextMenuInfo)) {
            return null;
        }

        AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        Object item = getPreferenceScreen().getRootAdapter().getItem( adapterMenuInfo.position );
        if ( item instanceof ConfigFilePreference )
        	return (ConfigFilePreference)item;
        else
        	return null;
    }


	/*
	 * Dialogs
	 */

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		switch(id) {
		case DIALOG_HELP:
			dialog = HtmlDialog.makeHelpDialog(this);
			break;
		case DIALOG_CHANGELOG:
			dialog = HtmlDialog.makeChangeLogDialog(this);
            dialog.setOnDismissListener( new DialogInterface.OnDismissListener()
            {
                public void onDismiss(DialogInterface dialogInterface)
                {
                    openPrerequisitesActivityIfNeeded();
                }
            });
			break;
		case DIALOG_PLEASE_RESTART:
			dialog = new AlertDialog.Builder( this ).setIcon(android.R.drawable.ic_dialog_info).
			setTitle( "Restart Required" ).setMessage( "The tunnel is currently active. For changes to take effect you must disable and then reenable the tunnel." ).setNeutralButton("OK", null).create();
			break;
		case DIALOG_FIX_DNS: {
			dialog = new AlertDialog.Builder( this )
			.setIcon( android.R.drawable.ic_dialog_info )
			.setTitle( R.string.fix_dns_dialog_title )
			.setMessage( R.string.fix_dns_dialog_message )
			.setPositiveButton( "Reset DNS", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    if (Preconditions.check( OpenVpnSettings.this ))
                        DnsUtil.setDns1( "8.8.8.8" );
                }
            } ).setNegativeButton( "Cancel", null )
			.create();
//			.setNeutralButton("OK", null).create();
		} break;
		case DIALOG_CONTACT_AUTHOR:{
			final String[] subjects = new String[]{ "Feature Request", "Bug report", "Feedback" };
			dialog = new AlertDialog.Builder(this)
			.setTitle( "Write Mail to Author" )
			.setSingleChoiceItems(
					subjects,
					-1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
							emailIntent .setType("plain/text");
							emailIntent .putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"android.openvpn@schaeuffelhut.de"});
							emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, subjects[which]);
//							emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, "Dear Friedrich,\n");
							startActivity(Intent.createChooser(emailIntent, "Send mail..." ));
							((AlertDialog)dialog).getListView().clearChoices();
							dialog.dismiss();
						}
					}
			)
			.setNegativeButton("Cancel", null)
			.create();
		} break;
		default:
			throw new UnexpectedSwitchValueException(id);
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case DIALOG_FIX_DNS: {
			String dns1 = "???";
			try{ dns1 = DnsUtil.getDns1(); } catch(Exception e){};
			((AlertDialog)dialog).setMessage(
					String.format( getResources().getString( R.string.fix_dns_dialog_message, dns1 ) )
			);
		} break;
		}
	}


	public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
		Log.d( TAG, "Connected to OpenVpnService" );

		mOpenVpnService = ((OpenVpnService.ServiceBinder)serviceBinder).getService();

		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.setOpenVpnService( mOpenVpnService );

		CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_ENABLED );
		pref.setSummary( "Turn off OpenVPN" );
		pref.setChecked( mOpenVpnService != null );
	}

	public void onServiceDisconnected(ComponentName name) {
		Log.d( TAG, "Disconnected from OpenVpnService" );

		mOpenVpnService = null;

		for(DaemonEnabler daemonEnabler : mDaemonEnablers )
			daemonEnabler.setOpenVpnService( null );

		CheckBoxPreference pref = (CheckBoxPreference) findPreference( Preferences.KEY_OPENVPN_ENABLED );
		pref.setSummary( "Turn on OpenVPN" );
		pref.setChecked( mOpenVpnService != null );
	}
}
