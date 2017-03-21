package com.mumu.joshautomation.records;

import android.content.res.Resources;
import android.util.Log;

import com.mumu.joshautomation.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * UserRecordHandler
 *
 * This class handle electricity data and file
 */
public class UserRecordHandler {
    private final static String TAG = "JATool";
    private List<UserRecordParser.Entry> mHistoryList;
    private String mDataDirectory;
    private Resources mResources;
    private static UserRecordHandler mHandler;
    private static boolean mHandlerInitialized = false;
    private static boolean mShouldInverseRecord = true;

    private UserRecordHandler() {
        // show only be called in inner class
    }

    public static UserRecordHandler getHandler() {
        if (mHandler == null) {
            mHandler = new UserRecordHandler();
        }

        return mHandler;
    }

    public boolean getAvailable() {
        return mHandlerInitialized;
    }

    public boolean getIsRecordInverse() {
        return mShouldInverseRecord;
    }

    public void setRecordInverse(boolean inverse) {
        mShouldInverseRecord = inverse;
    }

    public void initOnce(Resources res, String dataDirPath) {
        if (!mHandlerInitialized) {
            mDataDirectory = dataDirPath;
            mResources = res;
            init();
        }
    }

    private void init() {
        InputStream userDataStream;
        String userDataPath = mDataDirectory + "/" + mResources.getString(R.string.electric_data_file_name);

        try {
            userDataStream = new FileInputStream(userDataPath);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "User file not found, create one from resource");
            userDataStream = copyDefaultRecordToUser(userDataPath);
        }

        try {
            mHistoryList = new UserRecordParser().parse(userDataStream);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Parsing XML file failed. Fetching xml to developer." + e.getMessage());
            return;
        }
        Log.d(TAG, "xml data:");
        for (UserRecordParser.Entry entry: mHistoryList) {
            Log.d(TAG, "  " + entry.toString());
        }

        mHandlerInitialized = true;
    }

    private InputStream copyDefaultRecordToUser(String path) {
        InputStream in = mResources.openRawResource(R.raw.electricity_sample);
        OutputStream out = null;

        try {
            out = new FileOutputStream(new File(path));

            int read;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                    in = new FileInputStream(new File(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return in;
    }

    public void refreshFromFile() {
        init();
    }

    // Fetch content of list
    public List<UserRecordParser.Entry> getHistoryList() {
        return mHistoryList;
    }

    public UserRecordParser.Entry get(int idx) {
        if (idx < mHistoryList.size())
            return mHistoryList.get(idx);
        else
            return null;
    }

    public int getCount() {
        return mHistoryList.size();
    }

    private int getInverseIndex(int idx) {
        if (mShouldInverseRecord)
            return getCount() - idx - 1;
        else
            return idx;
    }

    public String getRecord(int idx) {
        if (idx < getCount())
            return get(getInverseIndex(idx)).record;
        else
            return null;
    }

    public String getDate(int idx) {
        if (idx < getCount())
            return get(getInverseIndex(idx)).date;
        else
            return null;
    }

    public String getTitle(int idx) {
        if (idx < getCount())
            return get(getInverseIndex(idx)).title;
        else
            return null;
    }

    public String getDateFormatted(int idx) {
        String rawDate = getDate(idx);

        try {
            Date df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(rawDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(df);
            Calendar today = Calendar.getInstance();
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);
            DateFormat timeFormatter = new SimpleDateFormat("a hh:mm", Locale.getDefault());
            DateFormat defaultFormatter = new SimpleDateFormat("yyyy/MM/dd a hh:mm", Locale.getDefault());

            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                return mResources.getString(R.string.electric_graphic_today) + " " + timeFormatter.format(df);
            } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
                return mResources.getString(R.string.electric_graphic_yesterday) + " " + timeFormatter.format(df);
            } else {
                return defaultFormatter.format(df);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Parsing date string failed " + e.getMessage());
            e.printStackTrace();
        }

        return "";
    }

    public String getSerial(int idx) {
        if (idx < getCount())
            return get(getInverseIndex(idx)).serial;
        else
            return null;
    }

    public String getNextSerial() {
        if (getCount() > 0) {
            return "" + (Integer.parseInt(get(getInverseIndex(0)).serial) + 1);
        } else {
            Log.d(TAG, "No record found, next serial is 0");
            return "0";
        }
    }

    public int getSerialNum(int idx) {
        if (idx < getCount())
            return idx + 1;
        else
            return -1;
    }

    // Record operation
    public boolean addRecord (UserRecordParser.Entry record) throws Exception {
        String userDataPath = mDataDirectory + "/" + mResources.getString(R.string.electric_data_file_name);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new File(userDataPath));
        Element root = document.getDocumentElement();

        // add record to runtime immediately
        mHistoryList.add(record);

        // add record to xml
        Element subroot = document.createElement("entry");
        Element serial = document.createElement("serial");
        serial.appendChild(document.createTextNode(record.serial));
        subroot.appendChild(serial);
        Element date = document.createElement("date");
        date.appendChild(document.createTextNode(record.date));
        subroot.appendChild(date);
        Element rec = document.createElement("record");
        rec.appendChild(document.createTextNode(record.record));
        subroot.appendChild(rec);
        Element title = document.createElement("title");
        title.appendChild(document.createTextNode(record.title));
        subroot.appendChild(title);

        root.appendChild(subroot);

        // save it
        DOMSource source = new DOMSource(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(userDataPath);
        transformer.transform(source, result);

        return true;
    }

    public boolean removeRecord(int serial) {
        return false;
    }

    public boolean editRecord(int serial, UserRecordParser.Entry entry) {
        return false;
    }

}
