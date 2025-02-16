package dngsoftware.spoolid;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Filament.class}, version = 1, exportSchema = false)
public abstract class filamentDB extends RoomDatabase {
    public abstract MatDB matDB();
}