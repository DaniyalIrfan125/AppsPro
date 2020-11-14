package com.daniyal.appspro.models;

import java.io.File;
import java.io.Serializable;

public class ImageFilesModel implements Serializable {

    public File getImage1() {
        return image1;
    }

    public void setImage1(File image1) {
        this.image1 = image1;
    }

    public File getImage2() {
        return image2;
    }

    public void setImage2(File image2) {
        this.image2 = image2;
    }

    public File getImage3() {
        return image3;
    }

    public void setImage3(File image3) {
        this.image3 = image3;
    }

    File image1, image2,image3;

}
