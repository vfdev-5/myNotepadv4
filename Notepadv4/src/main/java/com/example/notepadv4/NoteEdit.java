/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.notepadv4;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NoteEdit extends Activity {

    private static final String TAG=Notepadv4.class.getName();

    private static final int REMOVE_ID = Menu.FIRST;
    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId = null;
    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.note_edit);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        // get row id when it is sent with intent
        // case when activity is called from parent activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
            populateFields();
        }

    }

    protected void exitActivity() {
        Log.i(TAG, "exitActivity()");
        navigateUpTo(new Intent(this, Notepadv4.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add(0, REMOVE_ID, 0, R.string.remove);
        item.setIcon(R.drawable.ic_menu_delete);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            saveModifications();
            exitActivity();
            return true;
        } else if (id == REMOVE_ID) {
            removeNote();
            exitActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        mDbHelper.close();
        super.onDestroy();
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
        }
    }

    /** Save instance state */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);

        // Store temporary modifications in the outState
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
        outState.putString(NotesDbAdapter.KEY_TITLE, mTitleText.getText().toString());
        outState.putString(NotesDbAdapter.KEY_BODY, mBodyText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        Log.i(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedState);
        // Restore row id from saved state
        if (savedState != null) {
            mRowId = (Long) savedState.getSerializable(NotesDbAdapter.KEY_ROWID);
            mTitleText.setText((String) savedState.getString(NotesDbAdapter.KEY_TITLE));
            mBodyText.setText((String) savedState.getString(NotesDbAdapter.KEY_BODY));
            return;
        }
        mRowId = null;
    }

    /** Handles Back button
     * Save modification */
    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        saveModifications();
        super.onBackPressed();
    }

    private void saveModifications() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body);
        }
    }

    protected void removeNote() {
        Log.i(TAG, "onRemoveClicked()");
        // remove current item from DB
        if (mRowId != null) {
            mDbHelper.deleteNote(mRowId);
        }
    }

    public void onRemoveClicked(View view) {
        removeNote();
        // exit the activity
        exitActivity();
    }



}
