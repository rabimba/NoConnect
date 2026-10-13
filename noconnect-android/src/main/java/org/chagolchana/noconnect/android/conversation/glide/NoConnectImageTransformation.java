package org.chagolchana.noconnect.android.conversation.glide;

import android.graphics.Bitmap;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

public class NoConnectImageTransformation extends MultiTransformation<Bitmap> {

	public NoConnectImageTransformation(Radii r) {
		super(new CenterCrop(), new CustomCornersTransformation(r));
	}

}
