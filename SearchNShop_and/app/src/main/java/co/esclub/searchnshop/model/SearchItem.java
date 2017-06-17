package co.esclub.searchnshop.model;

import android.os.Parcelable;
import android.util.Log;

import java.util.TreeMap;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tae.kim on 06/06/2017.
 */

public class SearchItem extends RealmObject {
    public static final String KEY_ID = "id";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getMallName() {
        return mallName;
    }

    public void setMallName(String mallName) {
        this.mallName = mallName;
    }

    public RealmList<ShopItem> getItems() {
        return items;
    }

    public void setLastSearchTime(long lastSearchTime) {
        this.lastSearchTime = lastSearchTime;
    }

    public long getLastSearchTime() {
        return lastSearchTime;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    @PrimaryKey
    private String id;
    private String keyWord;
    private String mallName;
    private long lastSearchTime = 0;

    @Ignore
    private boolean isSuccess = false;

    public TreeMap<Integer, ShopItem> getItemTreeMap() {
        return itemTreeMap;
    }

    public void setItemTreeMap(
            TreeMap<Integer, ShopItem> itemTreeMap) {
        this.itemTreeMap = itemTreeMap;
    }

    @Ignore
    TreeMap<Integer, ShopItem> itemTreeMap = new TreeMap<Integer, ShopItem>();

    RealmList<ShopItem> items = new RealmList<>();

    public SearchItem(String keyWord, String mallName) {
        this.keyWord = keyWord;
        this.mallName = mallName;
        this.id = makeID(keyWord, mallName);
    }

    public SearchItem() {

    }

    public void addItem(ShopItem item) {
        items.add(item);
    }

    public void clearItem() {
        items.clear();
    }

    public static String makeID(String keyWord, String mallName) {
        return keyWord + "_" + mallName;
    }

    public void dump() {
        Log.d("###", "KEY_WORD: " + keyWord);
        Log.d("###", "MALL_NAME: " + mallName);
        if (items.size() > 0) {
            for (ShopItem item : items) {
                Log.d("###", "====");
                Log.d("###", "TITLE: " + item.getTitle());
                Log.d("###", "====");
            }
        } else {
            Log.d("###", "Item size 0");
        }
    }
}
