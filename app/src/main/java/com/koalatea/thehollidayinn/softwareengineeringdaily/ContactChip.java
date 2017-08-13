package com.koalatea.thehollidayinn.softwareengineeringdaily;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

/**
 * Created by krh12 on 7/9/2017.
 */

public class ContactChip implements ChipInterface {
    String label = ".Net";
    String info = "test";
    Integer id = 1200;


    public ContactChip (String label, String info, Integer id) {
        this.label = label;
        this.info = info;
        this.id = id;
    }

    @Override
    public Object getId() {
        return this.id;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getInfo() {
        return this.info;
    }
}