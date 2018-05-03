package com.dgsw.bamboo.data;

public class PostData {
    private int postIndex;
    private String content;
    private String writeDay;
    private String allowDay;

    public PostData(int postIndex, String content, String writeDay, String allowDay) {
        this.postIndex = postIndex;
        this.content = content;
        this.writeDay = writeDay;
        this.allowDay = allowDay;
    }

    public int getPostIndex() {
        return postIndex;
    }

    public void setPostIndex(int postIndex) {
        this.postIndex = postIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getWriteDay() {
        return writeDay;
    }

    public void setWriteDay(String writeDay) {
        this.writeDay = writeDay;
    }

    public String getAllowDay() {
        return allowDay;
    }

    public void setAllowDay(String allowDay) {
        this.allowDay = allowDay;
    }
}
