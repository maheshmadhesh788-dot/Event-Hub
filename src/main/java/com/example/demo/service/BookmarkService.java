package com.example.demo.service;

import com.example.demo.model.Bookmark;
import java.util.List;

public interface BookmarkService {
    Bookmark addBookmark(String rollNumber, Long eventId);
    void removeBookmark(String rollNumber, Long eventId);
    List<Bookmark> getBookmarksByStudent(String rollNumber);
    boolean isBookmarked(String rollNumber, Long eventId);
}
