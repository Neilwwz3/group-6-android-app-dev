package hk.hkucs.group_6_android_app_dev.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import hk.hkucs.group_6_android_app_dev.data.db.dao.MediaDao;
import hk.hkucs.group_6_android_app_dev.data.db.dao.WatchRecordDao;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;

/**
 * The app's only database. Pages never use it directly; they go through
 * {@link hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository}.
 */
@Database(entities = {MediaItem.class, WatchRecord.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NAME = "movietracker.db";
    private static volatile AppDatabase instance;

    public abstract MediaDao mediaDao();

    public abstract WatchRecordDao watchRecordDao();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    // Sample data can be reloaded at any time, so a schema change during
                    // development may drop the tables instead of migrating them.
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, NAME)
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }
}
