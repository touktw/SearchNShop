package co.esclub.searchnshop.model;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import io.realm.RealmObject;

/**
 * Created by tae.kim on 06/06/2017.
 */

public class ShopItem extends RealmObject {
    public ShopItem() {

    }

    public ShopItem(@NotNull String title,
                    @NotNull String link,
                    @NotNull String image,
                    @NotNull String mallName, int position) {
        this.title = title;
        this.link = link;
        this.image = image;
        this.mallName = mallName;
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMallName() {
        return mallName;
    }

    public void setMallName(String mallName) {
        this.mallName = mallName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @SerializedName("title")
    private String title;
    @SerializedName("link")
    private String link;
    @SerializedName("image")
    private String image;
    @SerializedName("mallName")
    private String mallName;
    private int position;
}
