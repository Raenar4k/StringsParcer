package com.raenarapps.stringsparcer;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Test";
    public static final String PARCER_ANDROID_STRING = "string";
    public static final String PARCER_ANDROID_NAME = "name";
    public static final String PARCER_ANDROID_RESOURCES = "resources";
    public static final String PARCER_ANDROID_NEW_FILENAME = "newStrings.xml";
    boolean debug = true;
    private EditText editTextAndroid;
    private EditText editTextiOS;
    private TextView textViewOutput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextAndroid = (EditText) findViewById(R.id.editTextAndroid);
        editTextAndroid.setText("strings");
        editTextiOS = (EditText) findViewById(R.id.editTextIOS);
        textViewOutput = (TextView) findViewById(R.id.textViewOutput);
        Button buttonMerge = (Button) findViewById(R.id.buttonMerge);
        if (buttonMerge != null) {
            buttonMerge.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (debug) Log.d("Test", "onClick");
        if (v.getId() == R.id.buttonMerge) {
            String fileNameiOS = editTextiOS.getText().toString();
            File pathiOS = new File(Environment.getExternalStorageDirectory() + Const.DIRECTORY_IOS);

            String fileNameAndroid = editTextAndroid.getText().toString() + Const.ANDROID_EXT;
            File pathAndroid = new File(Environment.getExternalStorageDirectory() + Const.DIRECTORY_ANDROID);
            if (!pathAndroid.exists()) {
                pathAndroid.mkdirs();
                if (debug) Log.d(TAG, "mkdirs");
            }

            List<StringObject> mergedStringsList = new ArrayList<>();

            File androidFile = new File(pathAndroid, fileNameAndroid);
            if (androidFile.exists()) {
                Observable.just(androidFile)
                        .subscribeOn(Schedulers.io())
                        .map(this::parseFromAndroid)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((collection) -> {
                            mergedStringsList.addAll(collection);
                            parseToAndroid(mergedStringsList);
                        });
            }
        }
    }

    private void parseToAndroid(List<StringObject> mergedStringsList) {
        File file = new File(Environment.getExternalStorageDirectory()+Const.DIRECTORY_ANDROID,
                PARCER_ANDROID_NEW_FILENAME);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag("", PARCER_ANDROID_RESOURCES);
            for (StringObject stringObject : mergedStringsList) {
                if (stringObject.getOs().equals(Const.OS_ANDROID)){
                    serializer.startTag(null, PARCER_ANDROID_STRING);
                    serializer.attribute(null, PARCER_ANDROID_NAME, stringObject.getKey());
                    serializer.text(stringObject.getValue());
                    serializer.endTag(null, PARCER_ANDROID_STRING);
                }
            }
            serializer.endTag("", PARCER_ANDROID_RESOURCES);
            serializer.endDocument();
            serializer.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<StringObject> parseFromAndroid(File androidFile) {
        List<StringObject> androidStringsList = new ArrayList<>();
        boolean gotText = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            FileInputStream fileInputStream = new FileInputStream(androidFile);
            parser.setInput(fileInputStream, null);
            String key = null;
            String value = null;
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (parser.getEventType()) {
                    case XmlPullParser.START_DOCUMENT:
                        if (debug) Log.d(TAG, "START_DOCUMENT");
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(PARCER_ANDROID_STRING)) {
                            gotText = false;
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                if (parser.getAttributeName(i).equals(PARCER_ANDROID_NAME)) {
                                    //name of the string
                                    key = parser.getAttributeValue(i);
                                }
                            }
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (!gotText) {
                            //value of the string
                            value = parser.getText();
                            gotText = true;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (key != null && value != null) {
                            androidStringsList.add(new StringObject(key, value, Const.OS_ANDROID));
                            if (debug) Log.d(TAG, "END_TAG: key = " + key + " value = " + value);
                            key = null;
                            value = null;
                        }
                        break;
                    default:
                        break;
                }
                parser.next();
            }
            if (debug) Log.d(TAG, "END_DOCUMENT");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return androidStringsList;
    }
}
