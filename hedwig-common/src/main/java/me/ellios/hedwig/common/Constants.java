/**
 * Copyright 1999-2011 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.ellios.hedwig.common;


import java.nio.charset.Charset;

/**
 * Constants.
 * Author: ellios
 * Date: 12-10-29 Time: 下午4:38
 */
public interface Constants {
    String DEFAULT_ENCODING = "UTF-8";
    Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);
    String BACKSPACE = " ";
    String EMPTY_STRING = "";
    String COMMA = ",";
    String EQUALITY = "=";
    String AMPERSAND = "&";
    String PATH_SEPARATOR = "/";
    String ZNODE_NAME_SEPARATOR = ":";
    String ZNODE_PATH_PREFIX = "/service";
    int REGISTRY_RETRY_INTERVAL = 5 * 1000; //5秒
    int DEFAULT_RPC_TIMEOUT = 3000;
    String DEFAULT_PB_SERVICE_PREFIX = "PB_";
    String DEFAULT_THRIFT_SERVICE_PREFIX = "T_";
}
