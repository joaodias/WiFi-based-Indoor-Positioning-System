package joaodias.thesis_02.Structures;

import android.graphics.Bitmap;

/**
 * Created by joaodias on 10/2/15.
 */
public class Map {
    //coming from the paint surface
    private int width;
    private int height;
    private Bitmap cleanMap;
    private Bitmap baseImage;
    private Bitmap curImage;
    private float touched_width;
    private float touched_height;
    private boolean isPoint = false;

    public static Map instance;

    public Map(){}

    public void setBaseImage(Bitmap image) {
        this.baseImage = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public float getTouched_height() {
        return touched_height;
    }

    public float getTouched_width() {
        return touched_width;
    }

    public void setTouched_height(float touched_height) {
        this.touched_height = touched_height;
    }

    public void setTouched_width(float touched_width) {
        this.touched_width = touched_width;
    }

    public Bitmap getBaseImage() {
        return baseImage;
    }

    public Bitmap getCurImage() {
        return curImage;
    }

    public void setCurImage(Bitmap curImage) {
        this.curImage = curImage;
    }

    public boolean isPoint() {
        return isPoint;
    }

    public void setIsPoint(boolean isPoint) {
        this.isPoint = isPoint;
    }

    public Bitmap getCleanMap() {
        return cleanMap;
    }

    public void setCleanMap(Bitmap cleanMap) {
        this.cleanMap = cleanMap;
    }

    public static synchronized Map getInstance(){
        if(instance==null){
            instance=new Map();
        }
        return instance;
    }
}
