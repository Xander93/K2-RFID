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

        public static void populateDatabase(MatDB db) {

                Filament item;
                item = new Filament();
                item.position = 1;
                item.filamentID = "01001";
                item.filamentName = "Hyper PLA";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 2;
                item.filamentID = "02001";
                item.filamentName = "Hyper PLA-CF";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 3;
                item.filamentID = "06002";
                item.filamentName = "Hyper PETG";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 4;
                item.filamentID = "03001";
                item.filamentName = "Hyper ABS";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 5;
                item.filamentID = "04001";
                item.filamentName = "CR-PLA";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 6;
                item.filamentID = "05001";
                item.filamentName = "CR-Silk";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 7;
                item.filamentID = "06001";
                item.filamentName = "CR-PETG";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 8;
                item.filamentID = "07001";
                item.filamentName = "CR-ABS";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 9;
                item.filamentID = "00001";
                item.filamentName = "Generic PLA";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 10;
                item.filamentID = "00002";
                item.filamentName = "Generic PLA-Silk";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 11;
                item.filamentID = "00003";
                item.filamentName = "Generic PETG";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 12;
                item.filamentID = "00004";
                item.filamentName = "Generic ABS";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 13;
                item.filamentID = "00005";
                item.filamentName = "Generic TPU";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 14;
                item.filamentID = "00006";
                item.filamentName = "Generic PLA-CF";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 15;
                item.filamentID = "00007";
                item.filamentName = "Generic ASA";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 16;
                item.filamentID = "08001";
                item.filamentName = "Ender-PLA";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 17;
                item.filamentID = "09001";
                item.filamentName = "EN-PLA+";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 18;
                item.filamentID = "10001";
                item.filamentName = "HP-TPU";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 19;
                item.filamentID = "11001";
                item.filamentName = "CR-Nylon";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 20;
                item.filamentID = "13001";
                item.filamentName = "CR-PLA Carbon";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 21;
                item.filamentID = "14001";
                item.filamentName = "CR-PLA Matte";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 22;
                item.filamentID = "15001";
                item.filamentName = "CR-PLA Fluo";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 23;
                item.filamentID = "16001";
                item.filamentName = "CR-TPU";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 24;
                item.filamentID = "17001";
                item.filamentName = "CR-Wood";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 25;
                item.filamentID = "18001";
                item.filamentName = "HP Ultra PLA";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 26;
                item.filamentID = "19001";
                item.filamentName = "HP-ASA";
                item.filamentVendor = "Creality";
                db.addItem(item);

                item = new Filament();
                item.position = 27;
                item.filamentID = "00008";
                item.filamentName = "Generic PA";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 28;
                item.filamentID = "00009";
                item.filamentName = "Generic PA-CF";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 29;
                item.filamentID = "00010";
                item.filamentName = "Generic BVOH";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 30;
                item.filamentID = "00011";
                item.filamentName = "Generic PVA";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 31;
                item.filamentID = "00012";
                item.filamentName = "Generic HIPS";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 32;
                item.filamentID = "00013";
                item.filamentName = "Generic PET-CF";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 33;
                item.filamentID = "00014";
                item.filamentName = "Generic PETG-CF";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 34;
                item.filamentID = "00015";
                item.filamentName = "Generic PA6-CF";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 35;
                item.filamentID = "00016";
                item.filamentName = "Generic PAHT-CF";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 36;
                item.filamentID = "00017";
                item.filamentName = "Generic PPS";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 37;
                item.filamentID = "00018";
                item.filamentName = "Generic PPS-CF";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 38;
                item.filamentID = "00019";
                item.filamentName = "Generic PP";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 39;
                item.filamentID = "00020";
                item.filamentName = "Generic PET";
                item.filamentVendor = "Generic";
                db.addItem(item);

                item = new Filament();
                item.position = 40;
                item.filamentID = "00021";
                item.filamentName = "Generic PC";
                item.filamentVendor = "Generic";
                db.addItem(item);

        }


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
                @Query("SELECT COUNT(dbkey) FROM filament_table")
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

                @SuppressWarnings("UnusedDeclaration")
                @Query("SELECT * FROM filament_table WHERE filament_name = :filamentVendor")
                Filament getFilamentByVendor(String filamentVendor);
        }

}