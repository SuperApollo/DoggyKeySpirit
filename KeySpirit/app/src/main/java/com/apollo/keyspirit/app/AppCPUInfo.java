/*
 * Copyright (C) 2012 www.amsoft.cn
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
package com.apollo.keyspirit.app;


public class AppCPUInfo {

    public String User;

    public String System;

    public String IOW;

    public String IRQ;

    public AppCPUInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AppCPUInfo(String user, String system, String iOW, String iRQ) {
        super();
        User = user;
        System = system;
        IOW = iOW;
        IRQ = iRQ;
    }

}
