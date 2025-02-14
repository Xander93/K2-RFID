package dngsoftware.spoolid;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import androidx.room.Update;
import java.util.List;

@Entity(tableName = "filament_table")
public class Filament {

        @SuppressWarnings("UnusedDeclaration")
        @PrimaryKey(autoGenerate = true)
        public int dbKey;

        @SuppressWarnings("UnusedDeclaration")
        @ColumnInfo(name = "filament_position")
        public int position;

        @SuppressWarnings("UnusedDeclaration")
        @ColumnInfo(name = "filament_name")
        public String filamentName;

        @SuppressWarnings("UnusedDeclaration")
        @ColumnInfo(name = "filament_id")
        public String filamentID;

        @SuppressWarnings("UnusedDeclaration")
        @ColumnInfo(name = "filament_vendor")
        public String filamentVendor;

        @SuppressWarnings("UnusedDeclaration")
        @ColumnInfo(name = "filament_param")
        public String filamentParam;


        @Dao
        public interface MatDB {

                @Database(entities = {Filament.class}, version = 1, exportSchema = false)
                abstract class filamentDB extends RoomDatabase {
                        abstract MatDB matDB();
                }

                @SuppressWarnings("UnusedDeclaration")
                @Insert
                void addItem(Filament item);

                @SuppressWarnings("UnusedDeclaration")
                @Update
                void updateItem(Filament item);

                @SuppressWarnings("UnusedDeclaration")
                @Query("UPDATE filament_table SET filament_position =:pos WHERE filament_id =:filamentID")
                void updatePosition(int pos, String filamentID);

                @SuppressWarnings("UnusedDeclaration")
                @Delete
                void deleteItem(Filament item);

                @SuppressWarnings("UnusedDeclaration")
                @Query("SELECT COUNT(dbKey) FROM filament_table")
                int getItemCount();

                @SuppressWarnings("UnusedDeclaration")
                @Query("SELECT * FROM filament_table ORDER BY filament_position ASC")
                List<Filament> getAllItems();

                @SuppressWarnings("UnusedDeclaration")
                @Query("DELETE FROM filament_table")
                void deleteAll();

                @SuppressWarnings("UnusedDeclaration")
                @Query("SELECT * FROM filament_table  WHERE filament_vendor = :filamentVendor ORDER BY filament_position ASC")
                List<Filament> getFilamentsByVendor(String filamentVendor);

                @SuppressWarnings("UnusedDeclaration")
                @Query("SELECT * FROM filament_table WHERE filament_id = :filamentID")
                Filament getFilamentById(String filamentID);

                @SuppressWarnings("UnusedDeclaration")
                @Query("SELECT * FROM filament_table WHERE filament_name = :filamentName")
                Filament getFilamentByName(String filamentName);

        }

}