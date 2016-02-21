package com.example.xyzreader.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Copyright 2016.  Frank Bartnitzek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Article implements Parcelable{
    private String title;
    private String author;
    private long publishedDate;
    private String body;
    private String photoUrl;
    private long id;

    protected Article(Parcel in) {
        title = in.readString();
        author = in.readString();
        publishedDate = in.readLong();
        body = in.readString();
        photoUrl = in.readString();
        id = in.readLong();
    }

    public Article(String author, String body, String photoUrl, long publishedDate, String title, long id) {
        this.author = author;
        this.body = body;
        this.photoUrl = photoUrl;
        this.publishedDate = publishedDate;
        this.title = title;
        this.id = id;
    }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeLong(publishedDate);
        dest.writeString(body);
        dest.writeString(photoUrl);
        dest.writeLong(id);
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public long getPublishedDate() {
        return publishedDate;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }
}
