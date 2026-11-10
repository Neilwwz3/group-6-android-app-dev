package hk.hkucs.group_6_android_app_dev.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;

/** A watch record together with the title, poster and type of its media item. */
public class RecordWithMedia {

    @Embedded
    public WatchRecord record;

    @ColumnInfo(name = "media_title")
    public String mediaTitle;

    @ColumnInfo(name = "media_poster")
    public String mediaPoster;

    @ColumnInfo(name = "media_type")
    public String mediaType;
}
