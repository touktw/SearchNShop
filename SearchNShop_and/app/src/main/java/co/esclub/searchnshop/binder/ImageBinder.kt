package co.esclub.searchnshop.binder

import android.databinding.BindingAdapter
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Created by tae.kim on 16/07/2017.
 */


@BindingAdapter("imageUrl")
fun ImageView.loadImage(url: String?) {
    if(!url.isNullOrEmpty()) {
        Glide.with(this).load(url).into(this)
    }
}