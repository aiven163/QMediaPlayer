package com.wenjoyai.videoplayer.media;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.wenjoyai.videoplayer.QApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yuqilin on 17/3/16.
 */

public class VideoLoader {

    public static final String TAG = "VideoLoader";

    public interface VideoLoaderListener {
        void onLoadItem(int position, MediaWrapper video);
        void onLoadCompleted(List<MediaWrapper> videos);
    }

    private ArrayList<MediaWrapper> mVideos = new ArrayList<>();

//    private Map<String, List<MediaWrapper>> mAllVideos = new HashMap<>();

    private VideoLoaderListener mVideoLoaderListener;

    public VideoLoader(VideoLoaderListener videoLoaderListener) {
        mVideoLoaderListener = videoLoaderListener;
    }

    public void scanStart() {
        QApplication.runBackground(new Runnable() {
            @Override
            public void run() {
                loadVideos();
                if (mVideoLoaderListener != null) {
                    mVideoLoaderListener.onLoadCompleted(mVideos);
                }
            }
        });
    }

    public List<MediaWrapper> getVideos() {
        return mVideos;
    }

//    public Map<String, List<MediaWrapper>> getAllVideos() {
//        return mAllVideos;
//    }

    public void loadVideos() {
        String[] thumbColumns = new String[]{
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        };

        String[] mediaColumns = new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_TAKEN
        };

        //首先检索SDcard上所有的video
        ContentResolver contentResolver = QApplication.getAppContext().getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                MediaWrapper media = new MediaWrapper();

                media.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                media.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));
                media.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));
                media.setLength(parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))));
                media.setFileSize(parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))));
                media.setDateTaken(parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN))));

                //获取当前Video对应的Id，然后根据该ID获取其Thumb
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
//                String selection = MediaStore.Video.Thumbnails.VIDEO_ID +"=?";
//                String[] selectionArgs = new String[]{
//                        id+""
//                };

//                String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id;
//                String thumbPath = "";
//                Cursor thumbCursor = contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, null, null);
//                if (thumbCursor != null && thumbCursor.moveToFirst()) {
//                    thumbPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
//                } else {
//                }

                media.setVideoId(id);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                Log.d(TAG, "====Scanned : [" + mVideos.size() + "] "
                        + media.getVideoId() + " "
                        + media.getFilePath() + " "
                        + media.getTitle() + " "
                        + simpleDateFormat.format(new Date(media.getDateTaken())));


//                String parentPath = FileUtils.getParent(media.filePath);
//                List<MediaWrapper> videos = null;
//                if (mAllVideos.containsKey(parentPath)) {
//                    videos = mAllVideos.get(parentPath);
//                } else {
//                    videos = new ArrayList<>();
//                }
//                videos.add(media);
//                mAllVideos.put(parentPath, videos);

                //然后将其加入到videoList
                mVideos.add(media);

                if (mVideoLoaderListener != null) {
                    mVideoLoaderListener.onLoadItem(mVideos.size() - 1, media);
                }

//                mVideoFragment.notifyDataChanged();

            } while(cursor.moveToNext());
        }

//        Collections.sort(mVideos, new Comparator<MediaWrapper>() {
//            @Override
//            public int compare(MediaWrapper item1, MediaWrapper item2) {
//                if (item1 == null) {
//                    return item2 == null ? 0 : -1;
//                } else if (item2 == null) {
//                    return 1;
//                }
//                int compare = 0;
//                compare = item1.filePath.toUpperCase(Locale.ENGLISH).compareTo(item2.filePath.toUpperCase(Locale.ENGLISH));
//                return compare;
//            }
//        });

        cursor.close();
    }

    private long parseLong(String value) {
        long result = 0;
        try {
            Long.parseLong(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseLong NumberFormatException : " + value);
        }
        return result;
    }
}
