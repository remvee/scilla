/*
 * scilla
 *
 * Copyright (C) 2001  R.W. van 't Veer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package org.scilla;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The scilla configuration provider.
 *
 * @version $Revision: 1.5 $
 * @author R.W. van 't Veer
 */
public class ConfigProvider {
    static final Log log = LogFactory.getLog(ConfigProvider.class);

    /** list of configuration implementations */
    public static final String[] configImpls = { "org.scilla.ConfigPropertiesImpl" };

    static Config config = null;
    static
    {
        // try to load a configuration implementation
        for (int i = 0; i < configImpls.length; i++) {
            try {
                config = (Config) Class.forName(configImpls[i]).newInstance();
                break;
            } catch (Exception ex) {
		// ignore
            }
        }
        if (config == null) {
            log.fatal("can not get configuration instance");
        }
    }

    /**
     * Get current configuration object.
     * @return current configuration
     */
    public static Config get () {
        return config;
    }
}
