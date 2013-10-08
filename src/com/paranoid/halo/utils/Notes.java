/*
 * Copyright 2013 ParanoidAndroid Project
 *
 * This file is part of HALO.
 *
 * HALO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HALO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HALO.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.paranoid.halo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.paranoid.halo.NotesActivity;
import com.paranoid.halo.R;

public class Notes {

    private static final int NOTE_LENGTH = 5; // You can have 100000 notes if you want ;)
    public static final String[] NOTES = new String[NOTE_LENGTH];
    private static final int[] NOTE_NOTIF_IDS = new int[NOTES.length];

    static {
        for(int i = 0; i < 5; i++) {
            NOTES[i] = "ext_note_" + i;
            NOTE_NOTIF_IDS[i] = 7325 + i; // this idd is just an identifier for the notification
        }
    }

    public static void setAllNotes(Context context) {
        for(String key : NOTES) {
            setNoteByKey(context, key);
        }
    }

    public static void setNoteByKey(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Note[] notes = new Note[NOTES.length];
        for(int i = 0; i < notes.length; i++) {
            notes[i] = new Note(NOTES[i],
                    sharedPreferences.getString(NOTES[i], null), NOTE_NOTIF_IDS[i]);
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for(Note note : notes) {
            if(key.equals(note.getId())) {
                String content = note.getContent();
                if(content == null || content.isEmpty()) {
                    notificationManager.cancel(note.getNotificationId());
                    break;
                }
                showNoteNotification(context, notificationManager, note.getNotificationId(), content);
            }
        }
    }

    public static void showNoteNotification(
            Context context, NotificationManager notificationManager, int id, String note) {
        Intent intent = new Intent(context, NotesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder ext_builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_add)
                        .setContentIntent(pendingIntent)
                        .setContentTitle(note);

        Notification notif = ext_builder.build();
        notif.flags |= Notification.FLAG_ONGOING_EVENT;
        notif.priority = Notification.PRIORITY_MIN;
        notif.tickerText = note;
        notificationManager.notify(id, notif);
    }

    private static class Note {
        private String id;
        private String content;
        private int notificationId;

        private Note(String id, String content, int notificationId) {
            this.id = id;
            this.content = content;
            this.notificationId = notificationId;
        }

        public String getId() {
            return id;
        }

        public int getNotificationId(){
            return notificationId;
        }

        public String getContent() {
            return content;
        }
    }
}
