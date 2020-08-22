/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

/**
 * Manages a local database for weather data.
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    public static final int DATABASE_VERSION = 3;
    public static final String CREATE_SQL_ENTRIES = "CREATE TABLE "
            + WeatherContract.WeatherEntry.TABLE_NAME + " ("
            + WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WeatherContract.WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, "
            + " UNIQUE (" + WeatherContract.WeatherEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS "
            + WeatherContract.WeatherEntry.TABLE_NAME;

    public WeatherDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_SQL_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_TABLE);
        onCreate(sqLiteDatabase);
    }
}