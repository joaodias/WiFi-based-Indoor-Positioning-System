package joaodias.thesis_02.Structures;

import android.graphics.Bitmap;

/**
 * Created by joaodias on 10/4/15.
 */
public class MapNav {
    //coming from the paint surface
    private int width;
    private int height;

    //coming from the user
//    private int width_meters;
//    private int height_meters;
//    private Bitmap cleanMap;
    private Bitmap baseImage;
    private Bitmap curImage;
    private float previousX;
    private float previousY;
    private float orientation;
    private boolean isPoint = false;

    public static MapNav instance;

    public MapNav(){}
//
//    public int getWidth(){
//        return this.width;
//    }
//
//    public int getHeight(){
//        return this.height;
//    }
//
//    public void setHeight_meters(int height_meters) {
//        this.height_meters = height_meters;
//    }

    public void setBaseImage(Bitmap image) {
        this.baseImage = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

//    public void setWidth_meters(int width_meters) {
//        this.width_meters = width_meters;
//    }
//
//    public float getPreviousY() {
//        return previousY;
//    }
//
//    public float getPreviousX() {
//        return previousX;
//    }

    public void setPreviousY(float previousY) {
        this.previousY = previousY;
    }

    public void setPreviousX(float previousX) {
        this.previousX = previousX;
    }

    public Bitmap getBaseImage() {
        return baseImage;
    }

//    public Bitmap getCurImage() {
//        return curImage;
//    }

    public void setCurImage(Bitmap curImage) {
        this.curImage = curImage;
    }
//
//    public boolean isPoint() {
//        return isPoint;
//    }

    public void setIsPoint(boolean isPoint) {
        this.isPoint = isPoint;
    }
//
//    public Bitmap getCleanMap() {
//        return cleanMap;
//    }
//
//    public void setCleanMap(Bitmap cleanMap) {
//        this.cleanMap = cleanMap;
//    }

    public static synchronized MapNav getInstance(){
        if(instance==null){
            instance=new MapNav();
        }
        return instance;
    }
}