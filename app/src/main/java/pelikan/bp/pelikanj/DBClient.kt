package pelikan.bp.pelikanj

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import pelikan.bp.pelikanj.viewModels.*


class DBClient(context: Context?) :
    SQLiteOpenHelper(context, DBName, null, 3) {


    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {

        sqLiteDatabase.execSQL(
            "create table UserData" +
                    "(id integer primary key autoincrement, language text, token text, profilePicture text)"
        )
        sqLiteDatabase.execSQL(
            "create table Institutions" +
                    "(id integer primary key autoincrement, address text, createdAt text, image text, institutionId integer, " +
                    "latitude double, longitude double, name text, description text)"
        )
        sqLiteDatabase.execSQL(
            "create table Buildings" +
                    "(id integer primary key autoincrement, buildingId integer, name text, description text, " +
                    "institutionId integer, createdAt text)"
        )
        sqLiteDatabase.execSQL(
            "create table Rooms" +
                    "(id integer primary key autoincrement, roomId integer, name text, description text, " +
                    "buildingId integer, createdAt text)"
        )
        sqLiteDatabase.execSQL(
            "create table Showcases" +
                    "(id integer primary key autoincrement, showcaseId integer, name text, description text, " +
                    "roomId integer, createdAt text)"
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS UserData")
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Institutions")
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Buildings")
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Rooms")
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Showcases")
        onCreate(sqLiteDatabase)
    }

    fun insertDefaultData(language: String?, token: String?, profilePicture: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("language", language)
        contentValues.put("token", token)
        contentValues.put("profilePicture", profilePicture)
        db.insert(USERDATATABLENAME, null, contentValues)
        db.close()
        return true
    }

    fun updateLanguage(language: String){
        val db = this.writableDatabase
        val values = ContentValues()
        val res = db.rawQuery("select * from UserData",null)
        res.moveToFirst()
        values.put("language",language)
        values.put("token",res.getString(res.getColumnIndexOrThrow(TOKEN)))
        values.put("profilePicture",res.getString(res.getColumnIndexOrThrow(PROFILEPICTURE)))
        db.update(USERDATATABLENAME,values,null,null)

        res.close()
    }

    fun updateToken(token: String){
        val db = this.writableDatabase
        val values = ContentValues()
        val res = db.rawQuery("select * from UserData",null)
        res.moveToFirst()
        values.put("language",res.getString(res.getColumnIndexOrThrow(LANGUAGE)))
        values.put("token",token)
        values.put("profilePicture",res.getString(res.getColumnIndexOrThrow(PROFILEPICTURE)))
        db.update(USERDATATABLENAME,values,null,null)

        res.close()
    }

    fun updateProfilePicture(profilePicture: String){
        val db = this.writableDatabase
        val values = ContentValues()
        val res = db.rawQuery("select * from UserData",null)
        res.moveToFirst()
        values.put("language",res.getString(res.getColumnIndexOrThrow(LANGUAGE)))
        values.put("token",res.getString(res.getColumnIndexOrThrow(TOKEN)))
        values.put("profilePicture",profilePicture)
        db.update(USERDATATABLENAME,values,null,null)

        res.close()
    }

    fun getAllUserData(): DatabaseModel?{
        val model: DatabaseModel
        val db = this.readableDatabase
        val res = db.rawQuery("select * from UserData", null)
        res.moveToFirst()

        if (res.count == 0){
            return null
        }

        val lang = (res.getString(res.getColumnIndexOrThrow(LANGUAGE)))
        val token = (res.getString(res.getColumnIndexOrThrow(TOKEN)))
        val profilePicture = (res.getString(res.getColumnIndexOrThrow(PROFILEPICTURE)))
        model = DatabaseModel(lang, token, profilePicture)

        res.close()
        db.close()
        return model
    }

    fun insertAllInstitutions(institutions: ArrayList<InstitutionsModelItem>): Boolean {
        val db = this.writableDatabase
        db.delete(INSTITUTIONSTABLENAME,null,null)
        val values = ContentValues()
        for (institution: InstitutionsModelItem in institutions){
            values.put("address",institution.address)
            values.put("createdAt",institution.createdAt)
            values.put("image",institution.image)
            values.put("institutionId",institution.institutionId)
            values.put("latitude",institution.latitude)
            values.put("longitude",institution.longitude)
            values.put("name",institution.name)
            values.put("description", institution.description)
            db.insert(INSTITUTIONSTABLENAME,null,values)
        }
        db.close()
        return true
    }

    fun getAllInstitutions(): ArrayList<InstitutionsModelItem> {
        val db = this.readableDatabase
        val res = db.rawQuery("select * from Institutions", null)
        val institutions = ArrayList<InstitutionsModelItem>()
        if (res.moveToFirst()) {
            do {
                institutions.add(
                    InstitutionsModelItem(
                        res.getString(1),
                        res.getString(2),
                        res.getString(3),
                        res.getInt(4),
                        res.getDouble(5),
                        res.getDouble(6),
                        res.getString(7),
                        res.getString(8)
                    )
                )
            } while (res.moveToNext())
        }
        res.close()
        db.close()
        return institutions
    }

    fun insertAllBuildings(buildings: ArrayList<Building>){
        val db = this.writableDatabase
        db.delete(BUILDINGSTABLENAME,null,null)
        val values = ContentValues()
        for (institution: Building in buildings){
            values.put("buildingId",institution.buildingId)
            values.put("createdAt",institution.createdAt)
            values.put("institutionId",institution.institutionId)
            values.put("name",institution.name)
            values.put("description", institution.description)
            db.insert(BUILDINGSTABLENAME,null,values)
        }
        db.close()
    }

    fun getAllBuildings(): ArrayList<Building>{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from Buildings", null)
        val buildings = ArrayList<Building>()
        if (res.moveToFirst()) {
            do {
                buildings.add(
                    Building(
                        res.getInt(1),
                        res.getString(2),
                        res.getString(3),
                        res.getInt(4),
                        res.getString(5)
                    )
                )
            } while (res.moveToNext())
        }
        res.close()
        db.close()
        return buildings
    }

    fun insertAllRooms(rooms: ArrayList<Room>){
        val db = this.writableDatabase
        db.delete(ROOMSTABLENAME,null,null)
        val values = ContentValues()
        for (institution: Room in rooms){
            values.put("buildingId",institution.buildingId)
            values.put("createdAt",institution.createdAt)
            values.put("roomId",institution.roomId)
            values.put("name",institution.name)
            values.put("description", institution.description)
            db.insert(ROOMSTABLENAME,null,values)
        }
        db.close()
    }

    fun getAllRooms(): ArrayList<Room>{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from Rooms", null)
        val rooms = ArrayList<Room>()
        if (res.moveToFirst()) {
            do {
                rooms.add(
                    Room(
                        res.getInt(1),
                        res.getString(2),
                        res.getString(3),
                        res.getInt(4),
                        res.getString(5)
                    )
                )
            } while (res.moveToNext())
        }
        res.close()
        db.close()
        return rooms
    }

    fun insertAllShowcases(showcases: ArrayList<Showcase>){
        val db = this.writableDatabase
        db.delete(SHOWCASESTABLENAME,null,null)
        val values = ContentValues()
        for (institution: Showcase in showcases){
            values.put("roomId",institution.roomId)
            values.put("createdAt",institution.createdAt)
            values.put("roomId",institution.roomId)
            values.put("name",institution.name)
            values.put("description", institution.description)
            db.insert(SHOWCASESTABLENAME,null,values)
        }
        db.close()
    }

    fun getAllShowcases(): ArrayList<Showcase>{
        val db = this.readableDatabase
        val res = db.rawQuery("select * from Showcases", null)
        val showcases = ArrayList<Showcase>()
        if (res.moveToFirst()) {
            do {
                showcases.add(
                    Showcase(
                        res.getInt(1),
                        res.getString(2),
                        res.getString(3),
                        res.getInt(4),
                        res.getString(5)
                    )
                )
            } while (res.moveToNext())
        }
        res.close()
        db.close()
        return showcases
    }

    companion object {
        const val DBName = "MyDatabase.db"
        const val USERDATATABLENAME = "UserData"
        const val INSTITUTIONSTABLENAME = "Institutions"
        const val BUILDINGSTABLENAME = "Buildings"
        const val ROOMSTABLENAME = "Rooms"
        const val SHOWCASESTABLENAME = "Showcases"
        const val LANGUAGE = "language"
        const val TOKEN = "token"
        const val PROFILEPICTURE = "profilePicture"
    }
}