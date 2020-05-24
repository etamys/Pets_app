package com.example.pets.PetContract;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.net.URI;


public class PetProvider extends ContentProvider {
    public static final String LOG_TAG =  PetProvider.class.getName();
    private static final int PETS = 100;
    private static final int PETS_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private PetDbHelper mDbHelper;
    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS+"/#",PETS_ID);
    }
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query( Uri uri,  String[] projection,  String selection,  String[] selectionArgs,  String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = null;
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                cursor =  db.query(PetContract.PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PETS_ID:
                selection = PetContract.PetEntry._ID+"=?";
                selectionArgs  = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor  = db.query(PetContract.PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                try {
                    throw new IllegalAccessException("can'nt query unknown URI "+uri);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public String getType( Uri uri) {
       final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert( Uri uri,  ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PETS:
                return insertPet(uri,values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri,ContentValues values)
    {
        //Sanity Checks for name
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        //sanity check for gender
        Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        //Check for weight
        Integer weight =  values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(PetContract.PetEntry.TABLE_NAME,null,values);
        if(id==-1)
        {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //notify a listner that the data has change for the pet content URI
        getContext().getContentResolver().notifyChange(uri,null);//null is optional

        //Return the new URI with the ID (of the newely inserted row ) append at the end
        return ContentUris.withAppendedId(uri,id);
    }
    @Override
    public int delete( Uri uri,  String selection,  String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                rowsDeleted = db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }


    @Override
    public int update( Uri uri,  ContentValues values,  String selection,  String[] selectionArgs) {
       final int match = sUriMatcher.match(uri);
       switch (match)
       {
           case PETS:
               return updatePet(uri, values, selection, selectionArgs);
           case PETS_ID:
               selection = PetContract.PetEntry._ID+"=?";
               selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
               return updatePet(uri, values, selection, selectionArgs);
           default:
               throw new IllegalArgumentException("Update is not supported for " + uri);
       }
    }
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)){
            String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name == null)
            {
                throw new IllegalArgumentException("Pets require  a pet name");
            }
        }
        if(values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)){
            Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetEntry.isValidGender(gender))
            {
                throw  new IllegalArgumentException("Pet requires valid gender");
            }
        }
        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }
        if (values.size() == 0)
        {
            return  0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(PetContract.PetEntry.TABLE_NAME,values,selection,selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }
}
