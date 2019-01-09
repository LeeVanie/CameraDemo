package com.idcardlibs.camerademo;


public class WrapCameraSize implements Comparable<WrapCameraSize> {  
    private int width;//宽  
    private int height;//高  
    private int d;//宽的差的绝对值和高的差的绝对值之和  
  
    public int getHeight() {  
        return height;  
    }  
  
    public void setHeight(int height) {  
        this.height = height;  
    }  
  
    public int getWidth() {  
        return width;  
    }  
  
    public void setWidth(int width) {  
        this.width = width;  
    }  
  
    public int getD() {  
        return d;  
    }  
  
    public void setD(int d) {  
        this.d = d;  
    }  
  
  
    @Override  
    public int compareTo(WrapCameraSize another) {  
        if (this.d > another.d) {  
            return 1;  
        } else if (this.d < another.d) {  
            return -1;  
        }  
        return 0;  
    }  
}  