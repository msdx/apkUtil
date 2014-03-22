/*
 * @(#)Feature.java		       Project:androidUtil
 * Date:2012-11-7
 *
 * Copyright (c) 2011 CFuture09, Institute of Software, 
 * Guangdong Ocean University, Zhanjiang, GuangDong, China.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package com.sinaapp.msdxblog.apkUtil.entity;

/**
 * @author Geek_Soledad (66704238@51uc.com)
 */
public class ImpliedFeature {

    /**
     * 需要的设备特性名称。
     */
    private String feature;

    /**
     * 表明所需特性的内容。
     */
    private String implied;

    public ImpliedFeature() {
        super();
    }

    public ImpliedFeature(String feature, String implied) {
        super();
        this.feature = feature;
        this.implied = implied;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getImplied() {
        return implied;
    }

    public void setImplied(String implied) {
        this.implied = implied;
    }

    @Override
    public String toString() {
        return "Feature [feature=" + feature + ", implied=" + implied + "]";
    }
}
