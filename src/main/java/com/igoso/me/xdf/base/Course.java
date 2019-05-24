package com.igoso.me.xdf.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * created by igoso at 2018/10/20
 **/
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Course {
    //主题，最外层
    private String subject;

    //阶段或者类目
    private String section;

    //章节
    private String chapter;

    //分段标题
    private String segTitle;

    //老师
    private String teacher;

    //课时
    private String lessonTime;

    //视频标题
    private String title;

    //视频地址
    private String url;

    //视频长度 eg 34:00
    private String videoLength;

    //课程状态 可能还未上线，需要购买
    private String lessonStatus = "";


    public void appendStatus(String status) {
        this.lessonStatus = this.lessonStatus + "/" + status;
    }

    public String toLine() {
        StringBuilder builder = new StringBuilder();
        builder.append(wrap(this.subject)).append(",");
        builder.append(wrap(this.section)).append(",");
        builder.append(wrap(this.chapter)).append(",");
        builder.append(wrap(this.segTitle)).append(",");
        builder.append(wrap(this.teacher)).append(",");
        builder.append(wrap(this.title)).append(",");
        builder.append(wrap(this.url)).append(",");
        builder.append(wrap(this.lessonTime)).append(",");
        builder.append(wrap(this.videoLength)).append(",");
        builder.append(wrap(this.lessonStatus)).append("\n");

        return builder.toString();
    }

    private static String wrap(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

}
