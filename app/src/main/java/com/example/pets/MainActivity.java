package com.example.pets;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.pets.PetContract.PetContract;
import com.example.pets.PetContract.PetContract.PetEntry;
import com.example.pets.PetContract.PetDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /** Database helper that will provide us access to the database */
    private PetDbHelper mDbHelper;
    private static final int PET_LOADER = 0;
    /*** Adapter for ListView*/
    PetCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        //displayDatabaseInfo();
        ListView petListView = (ListView)findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        //Setup an Adapter to create a list item for each row of pet data in the Cursor
        //There is no pet data yet (Until the loader finishes) so pass in null for the cursor
        mCursorAdapter = new PetCursorAdapter(this,null);
        petListView.setAdapter(mCursorAdapter);

        //setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this,EditorActivity.class);
                Uri currentUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI,id);
                intent.setData(currentUri);
                startActivity(intent);
            }
        });
        // Kick off the loader
        getLoaderManager().initLoader(PET_LOADER, null,this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    @SuppressLint("SetTextI18n")
    private void displayDatabaseInfo() {

        //PetDbHelper mDbHelper = new PetDbHelper(this);

        // for reading database from the petDbHelper
        //Creating SQLiteDatabase object instance
        //SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection ={
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };
//        Cursor cursor = db.query(
//            PetEntry.TABLE_NAME,
//            projection,
//            null,
//            null,
//            null,
//            null,
//            null
//        );
        Cursor cursor = getContentResolver().query(PetContract.PetEntry.CONTENT_URI,projection,null,null,null);
        ListView petListView = (ListView)findViewById(R.id.list);
        PetCursorAdapter adapter = new PetCursorAdapter(this,cursor);
        petListView.setAdapter(adapter);
//
//        try {
//            // Display the number of rows in the Cursor (which reflects the number of rows in the
//            // pets table in the database).
//            displayView.setText("The pets table contains " + cursor.getCount()+" pets.\n\n");
//            displayView.append(PetEntry._ID + " - " +
//                    PetEntry.COLUMN_PET_NAME +"-"+PetEntry.COLUMN_PET_BREED+"-"+PetEntry.COLUMN_PET_GENDER+"-"+PetEntry.COLUMN_PET_GENDER+"\n");
//
//            // Figure out the index of each column
//            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
//            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
//            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
//            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
//            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
//
//            // Iterate through all the returned rows in the cursor
//            while (cursor.moveToNext()) {
//                // Use that index to extract the String or Int value of the word
//                // at the current row the cursor is on.
//                int currentID = cursor.getInt(idColumnIndex);
//                String currentName = cursor.getString(nameColumnIndex);
//                String breed = cursor.getString(breedColumnIndex);
//                String gender = cursor.getString(genderColumnIndex);
//                String weight = cursor.getString(weightColumnIndex);
//                // Display the values from each column of the current row in the cursor in the TextView
//                displayView.append(("\n" + currentID + " - " +
//                        currentName+" - "+breed+" - "+gender+" - "+weight));
//            }
//        } finally {
//            // Always close the cursor when you're done reading from it. This releases all its
//            // resources and makes it invalid.
//            cursor.close();
//        }
  }
    private void insertPet()
    {
        // Gets the data repository in write mode
        //SQLiteDatabase db =  mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,10);

        // Insert the new row, returning the primary key value of the new row
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id,  Bundle args) {

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PetEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
