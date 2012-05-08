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

package de.schaeuffelhut.android.openvpn.setup.prerequisites;

import android.net.Uri;
import de.schaeuffelhut.android.openvpn.R;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: fries
 * Date: 4/30/12
 * Time: 5:49 PM
 * To change this template use File | Settings | File Templates.
 */
class ProbeOpenVpn extends ProbeExecutable
{
    ProbeOpenVpn()
    {
        super( "OpenVPN binary",
                "The actual VPN program.",
                R.string.prerequisites_item_title_getOpenVpn,
                Uri.parse( "market://details?id=de.schaeuffelhut.android.openvpn.installer" ),
                new File("/system/xbin/openvpn"), new File("/system/bin/openvpn")
        );
    }
}
