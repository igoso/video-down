package com.igoso.me.xdf.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * m3u8 object
 * created by igoso at 2018/10/21
 **/

@Getter
@Setter
@NoArgsConstructor
public class Media {
    /**
     * 创建一个ID用于后期使用
     */
    private String id;

    /**
     * HLS媒体描述文件
     */
    private String m3u8Context;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 分段视频列表
     */
    private List<String> mediaUris = new ArrayList<>();

    /**
     * 直接记录媒体文件数目，方便查询  等于 mediaUris.size()
     */
    private int uriSize;

    /**
     * 加密/解密key地址
     */
    private String keyUri;

    /**
     * 解密后的16进制key值
     */
    private String keyHex;

    /**
     * 用于解密的初始向量16进制字符值
     */
    private String ivHex;

    /**
     * 加密方法
     */
    private String method;

    /**
     * 提取解密后的信息保存文件 dts.txt
     * 标题 key iv
     */
    private String decryptKeyFile;


    //m3u8原始文件保存位置
    private String m3u8FileLocation;
    //解密vodkey原始文件保存位置
    private String keyBinFileLocation;


    /**
     * 判断是否可以解密
     * @return boolean
     */
    public boolean isValidAes() {
        return this.method != null && this.method.contains("AES");
    }

    public int getUriSize() {
        return mediaUris.size();
    }

    @Override
    public String toString() {
        return "Media{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", keyHex='" + keyHex + '\'' +
                ", ivHex='" + ivHex + '\'' +
                ", method='" + method + '\'' +
                ", uriSize='" + uriSize + '\'' +
                '}';
    }
}
