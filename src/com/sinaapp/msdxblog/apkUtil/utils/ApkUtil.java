/*
 * @(#)ApkUtil.java		       version: 0.2.1 
 * Date:2012-1-9
 *
 * Copyright (c) 2011 CFuture09, Institute of Software, 
 * Guangdong Ocean University, Zhanjiang, GuangDong, China.
 * All rights reserved.
 */
package com.sinaapp.msdxblog.apkUtil.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sinaapp.msdxblog.apkUtil.entity.ApkInfo;
import com.sinaapp.msdxblog.apkUtil.entity.ImpliedFeature;

/**
 * apk工具类。封装了获取Apk信息的方法。
 * 
 * @author CFuture.Geek_Soledad(66704238@51uc.com)
 * 
 *         <p>
 *         <b>version description</b><br />
 *         V0.2.1 修改程序名字为从路径中获得。
 *         </p>
 */
public class ApkUtil {
    public static final String VERSION_CODE = "versionCode";
    public static final String VERSION_NAME = "versionName";
    public static final String SDK_VERSION = "sdkVersion";
    public static final String TARGET_SDK_VERSION = "targetSdkVersion";
    public static final String USES_PERMISSION = "uses-permission";
    public static final String APPLICATION_LABEL = "application-label";
    public static final String APPLICATION_ICON = "application-icon";
    public static final String USES_FEATURE = "uses-feature";
    public static final String USES_IMPLIED_FEATURE = "uses-implied-feature";
    public static final String SUPPORTS_SCREENS = "supports-screens";
    public static final String SUPPORTS_ANY_DENSITY = "supports-any-density";
    public static final String DENSITIES = "densities";
    public static final String PACKAGE = "package";
    public static final String APPLICATION = "application:";

    private ProcessBuilder mBuilder;
    private static final String SPLIT_REGEX = "(: )|(=')|(' )|'";
    private static final String FEATURE_SPLIT_REGEX = "(:')|(',')|'";
    /**
     * aapt所在的目录。
     */
    private String mAaptPath = "lib/aapt";

    public ApkUtil() {
        mBuilder = new ProcessBuilder();
        mBuilder.redirectErrorStream(true);
    }

    /**
     * 返回一个apk程序的信息。
     * 
     * @param apkPath
     *            apk的路径。
     * @return apkInfo 一个Apk的信息。
     */
    public ApkInfo getApkInfo(String apkPath) throws Exception {
        Process process = mBuilder.command(mAaptPath, "d", "badging", apkPath).start();
        InputStream is = null;
        is = process.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf8"));
        String tmp = br.readLine();
        try {
            if (tmp == null || !tmp.startsWith("package")) {
                throw new Exception("参数不正确，无法正常解析APK包。输出结果为:" + tmp + "...");
            }
            ApkInfo apkInfo = new ApkInfo();
            do {
                setApkInfoProperty(apkInfo, tmp);
            } while ((tmp = br.readLine()) != null);
            return apkInfo;
        } catch (Exception e) {
            throw e;
        } finally {
            process.destroy();
            closeIO(is);
            closeIO(br);
        }
    }

    /**
     * 保存apk的图标。
     * 
     * @param apkPath
     *            apk的路径
     * @param apkInfo
     *            apkInfo
     * @param file
     *            保存的路径
     * @return 是否保存成功
     */
    public boolean saveIcon(String apkPath, ApkInfo apkInfo, File file) {
        if (apkInfo == null) {
            throw new IllegalArgumentException("the apkInfo is null");
        }
        if (apkInfo.getApplicationIcon() == null) {
            return false;
        }
        ZipInputStream zis = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            ZipFile zipFile = new ZipFile(apkPath);
            zis = new ZipInputStream(new FileInputStream(apkPath));
            ZipEntry icon = zipFile.getEntry(apkInfo.getApplicationIcon());
            is = new BufferedInputStream(zipFile.getInputStream(icon));
            os = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = is.read(buf)) != -1) {
                os.write(buf, 0, length);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 设置APK的属性信息。
     * 
     * @param apkInfo
     * @param source
     */
    private void setApkInfoProperty(ApkInfo apkInfo, String source) {
        if (source.startsWith(PACKAGE)) {
            splitPackageInfo(apkInfo, source);
        } else if (source.startsWith(SDK_VERSION)) {
            apkInfo.setSdkVersion(getPropertyInQuote(source));
        } else if (source.startsWith(TARGET_SDK_VERSION)) {
            apkInfo.setTargetSdkVersion(getPropertyInQuote(source));
        } else if (source.startsWith(USES_PERMISSION)) {
            apkInfo.addToUsesPermissions(getPropertyInQuote(source));
        } else if (source.startsWith(APPLICATION_LABEL)) {
            apkInfo.setApplicationLable(getPropertyInQuote(source));
        } else if (source.startsWith(APPLICATION_ICON)) {
            apkInfo.addToApplicationIcons(getKeyBeforeColon(source), getPropertyInQuote(source));
        } else if (source.startsWith(APPLICATION)) {
            String[] rs = source.split("( icon=')|'");
            apkInfo.setApplicationIcon(rs[rs.length - 1]);
        } else if (source.startsWith(USES_FEATURE)) {
            apkInfo.addToFeatures(getPropertyInQuote(source));
        } else if (source.startsWith(USES_IMPLIED_FEATURE)) {
            apkInfo.addToImpliedFeatures(getFeature(source));
        } else {
            // System.out.println(source);
        }
    }

    private ImpliedFeature getFeature(String source) {
        String[] result = source.split(FEATURE_SPLIT_REGEX);
        ImpliedFeature impliedFeature = new ImpliedFeature(result[1], result[2]);
        return impliedFeature;
    }

    /**
     * 返回出格式为name: 'value'中的value内容。
     * 
     * @param source
     * @return
     */
    private String getPropertyInQuote(String source) {
        return source.substring(source.indexOf("'") + 1, source.length() - 1);
    }

    /**
     * 返回冒号前的属性名称
     * 
     * @param source
     * @return
     */
    private String getKeyBeforeColon(String source) {
        return source.substring(0, source.indexOf(':'));
    }

    /**
     * 分离出包名、版本等信息。
     * 
     * @param apkInfo
     * @param packageSource
     */
    private void splitPackageInfo(ApkInfo apkInfo, String packageSource) {
        String[] packageInfo = packageSource.split(SPLIT_REGEX);
        apkInfo.setPackageName(packageInfo[2]);
        apkInfo.setVersionCode(packageInfo[4]);
        apkInfo.setVersionName(packageInfo[6]);
    }

    /**
     * 释放资源。
     * 
     * @param c
     *            将关闭的资源
     */
    private final void closeIO(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            String demo = "E:/androidApk/2012/05/百宝工具箱/1.0/signed/Toolbox-360.apk";
            if (args.length > 0) {
                demo = args[0];
            }
            ApkUtil apkUtil = new ApkUtil();
            ApkInfo apkInfo = apkUtil.getApkInfo(demo);
            apkUtil.saveIcon(demo, apkInfo, new File("e:/icon.png"));
            System.out.println(apkInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getmAaptPath() {
        return mAaptPath;
    }

    public void setmAaptPath(String mAaptPath) {
        this.mAaptPath = mAaptPath;
    }

}
