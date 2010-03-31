/**
 * Copyright 2009 Friedrich Schäuffelhut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package de.schaeuffelhut.android.openvpn;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import de.schaeuffelhut.android.openvpn.util.Util;

public final class Preferences {
	
	public static final String KEY_OPENVPN_SETTINGS_CATEGORY = "openvpn_settings_category";
	public static final String KEY_OPENVPN_ENABLED = "openvpn_enabled";
	public static final String KEY_OPENVPN_CONFIGURATIONS = "openvpn_configurations";
	public static final String KEY_OPENVPN_USE_INTERNAL_STORAGE = "openvpn_use_internal_storage";
	public static final String KEY_OPENVPN_EXTERNAL_STORAGE = "openvpn_external_storage";
	public static final String KEY_OPENVPN_PATH_TO_BINARY = "openvpn_path_to_binary";
	public static final String KEY_OPENVPN_PATH_TO_SU = "openvpn_path_to_su";
	public static final String KEY_OPENVPN_SU_ARGUMENTS = "openvpn_su_arguments";
	public static final String KEY_OPENVPN_DO_MODPROBE_TUN = "openvpn_do_modprobe_tun";

	public static final String KEY_NEXT_NOTIFICATION_ID = "openvpn_next_notification_id";

	public final static String KEY_CONFIG(String config){
		return String.format( "%s[%s]", KEY_OPENVPN_CONFIGURATIONS, config );
	}

	public final static String KEY_CONFIG_ENABLED(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".enabled";
	}
	public final static String KEY_CONFIG_INTENDED_STATE(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".intended_state";
	}
	public final static String KEY_CONFIG_MGMT_PORT(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".mgmt_port";
	}
	public final static String KEY_CONFIG_NOTIFICATION_ID(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".notification_id";
	}
	public final static String KEY_CONFIG_DNSCHANGE(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".dnschange";
	}
	public final static String KEY_CONFIG_DNS1(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".dns1";
	}
	public final static String KEY_VPN_DNS(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".vpndns1";
	}
	public final static String KEY_VPN_DNS_ENABLE(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".vpndns.enable";
	}
	public final static String KEY_SCRIPT_SECURITY_LEVEL(File config){
		return KEY_CONFIG(config.getAbsolutePath())+".script_security.level";
	}
	
	
	private Preferences() {
	}

	public static boolean getOpenVpnEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_OPENVPN_ENABLED, false );
	}

	public final static boolean getUseInternalStorage(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getBoolean( KEY_OPENVPN_USE_INTERNAL_STORAGE, false );	
	}

	public final static void setUseInternalStorage(Context context, boolean b)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putBoolean( KEY_OPENVPN_USE_INTERNAL_STORAGE, true );
		edit.commit();
	}

	public final static String getExternalStorage(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getString( Preferences.KEY_OPENVPN_EXTERNAL_STORAGE, "/sdcard/openvpn" );
	}

	public final static File getExternalStorageAsFile( SharedPreferences sharedPreferences)
	{
		return new File( getExternalStorage(sharedPreferences) );
	}
	

	public final static File getConfigDir(Context context, SharedPreferences sharedPreferences)
	{
		final File configDir;
		if ( getUseInternalStorage(sharedPreferences))
		{
			configDir = new File( context.getFilesDir(), "config.d" );
			if ( !configDir.exists() )
				configDir.mkdir();
		}
		else
		{
			configDir = getExternalStorageAsFile(sharedPreferences);
		}
		return configDir;
	}

	public final static String getPathToBinary(SharedPreferences sharedPreferences)
	{
		String path = sharedPreferences.getString( Preferences.KEY_OPENVPN_PATH_TO_BINARY, null );
		if ( path == null || "".equals( path ) )
		{
			path = null;
			for( File f : new File[]{ new File( "/system/xbin/openvpn" ), new File( "/system/bin/openvpn" ) } )
			{
				if ( f.exists() )
				{
					path = f.getAbsolutePath();
					break;
				}
			}
		}
		return path;
	}

	public static File getPathToBinaryAsFile(SharedPreferences sharedPreferences)
	{
		String path = getPathToBinary(sharedPreferences);
		return path == null ? null : new File( path );
	}

	public final static String getPathToSu(SharedPreferences sharedPreferences)
	{
		String path = sharedPreferences.getString( Preferences.KEY_OPENVPN_PATH_TO_SU, null );
		if ( path == null )
		{
			for( File f : new File[]{ new File( "/system/xbin/su" ), new File( "/system/bin/su" ), new File( "/sbin/su" ) } )
			{
				if ( f.exists() )
				{
					path = f.getAbsolutePath();
					break;
				}
			}
		}
		return path;
	}

	public static File getPathToSuAsFile(SharedPreferences sharedPreferences)
	{
		String path = getPathToSu(sharedPreferences);
		return path == null ? null : new File( path );
	}

	public final static String getSuArguments(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getString( Preferences.KEY_OPENVPN_SU_ARGUMENTS, "-s -x" );
	}

	public static void setMgmtPort(Context context, File configFile, int mgmtPort) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putInt( KEY_CONFIG_MGMT_PORT(configFile), mgmtPort );
		edit.commit();
	}
	
	public final static int getMgmtPort(Context context, File configFile)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getInt( Preferences.KEY_CONFIG_MGMT_PORT(configFile), -1 );
	}

	public final static int getNotificationId(Context context, File configFile)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int id = sharedPreferences.getInt( Preferences.KEY_CONFIG_NOTIFICATION_ID(configFile), -1 );
		if ( id == -1 ) {
			synchronized ( KEY_NEXT_NOTIFICATION_ID ) {
				id = sharedPreferences.getInt( KEY_NEXT_NOTIFICATION_ID, Notifications.FIRST_CONFIG_ID);
				Editor edit = sharedPreferences.edit();
				edit.putInt( Preferences.KEY_CONFIG_NOTIFICATION_ID(configFile), id );
				edit.putInt( KEY_NEXT_NOTIFICATION_ID, id + 1);
				edit.commit();
			}
		}
		return id;
	}
	
	public final static void setDns1(Context context, File configFile, int dnsChange, String dns)
	{
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		edit.putInt( Preferences.KEY_CONFIG_DNSCHANGE(configFile), dnsChange );
		edit.putString( Preferences.KEY_CONFIG_DNS1(configFile), dns );
		edit.commit();
	}
	public final static int getDnsChange(Context context, File configFile)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getInt( Preferences.KEY_CONFIG_DNSCHANGE(configFile), -1 );
	}
	public final static String getDns1(Context context, File configFile)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString( Preferences.KEY_CONFIG_DNS1(configFile), "" );
	}

	public static String getVpnDns(Context context, File configFile) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString( Preferences.KEY_VPN_DNS(configFile), "" );
	}
	public static boolean getVpnDnsEnabled(Context context, File configFile) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean( Preferences.KEY_VPN_DNS_ENABLE(configFile), false );
	}

	public static int getScriptSecurityLevel(Context context, File configFile) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt( sharedPreferences.getString( Preferences.KEY_SCRIPT_SECURITY_LEVEL(configFile), "1" ) );
	}

	
	public final static boolean getDoModprobeTun(SharedPreferences sharedPreferences) {
		return sharedPreferences.getBoolean( Preferences.KEY_OPENVPN_DO_MODPROBE_TUN, false);
	}

	public final static File[] configs(Context context)
	{
		return configs(getConfigDir( context, PreferenceManager.getDefaultSharedPreferences(context) ));
	}
	
	public final static File[] configs(File configDir)
	{
		File[] configFiles;
		if ( configDir == null )
			configFiles = new File[0];
		else
			configFiles = configDir.listFiles( new Util.FileExtensionFilter(".conf",".ovpn") );
	
		return configFiles == null ? new File[0] : configFiles;
	}
}
