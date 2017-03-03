package com.tonydantona.nomad;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static com.google.android.gms.wearable.DataMap.TAG;

// utility function for XML resource (res/xml) parsing
public class XMLParsingUtilities {

    private static final String TAG = "XMLParsingUtilities";

    // returns type of the next XML parsing event
    public static int GetNextParseEvent(XmlPullParser xmlResourceParser) {
        try {
            return xmlResourceParser.next();
        } catch (XmlPullParserException e) {
            Log.d(TAG, "GetNextParseEvent failed: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "GetNextParseEvent failed: " + e.getMessage());
        }
        return 0;
    }
}