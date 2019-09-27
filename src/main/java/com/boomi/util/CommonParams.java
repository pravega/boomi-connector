/*
 * Copyright (c) 2017 Dell Inc. and Accenture, or its subsidiaries. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 */
package com.boomi.util;

import com.boomi.constants.PravegaConstants;

/**
 * 
 * @author kritika.b.verma
 *
 */
public class CommonParams {

	
    public static boolean  isPravegaStandalone() {
        return Boolean.parseBoolean(getDefaultParam(PravegaConstants.PRAVEGA_STANDALONE));
    }
/**
 * 
 * @param key
 * @return
 */
    private static String  getDefaultParam(String key)
    {
        String keyValue = null;
        if(key != null)
        {
           switch (key) {
                case "pravega_standalone":
                    keyValue = "true";
                    break;               
                default:
                    keyValue = null;
            }
        }
        return keyValue;
    }
}
