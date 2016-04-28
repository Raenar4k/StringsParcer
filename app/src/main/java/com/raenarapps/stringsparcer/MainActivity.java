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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import rx.Observable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Test";
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
        Observable.just("hello")
                .map(s -> s + " test observable")
                .subscribe(s -> Log.d(TAG, "onCreate: " + s));
    }

    @Override
    public void onClick(View v) {
        if (debug) Log.d("Test", "onClick");
        if (v.getId() == R.id.buttonMerge) {
            String fileNameiOS = editTextiOS.getText().toString();
            File pathiOS = new File(Environment.getExternalStorageDirectory() + "/StringsParcer/iOS");
            String fileNameAndroid = editTextAndroid.getText().toString() + ".xml";
            File pathAndroid = new File(Environment.getExternalStorageDirectory() + "/StringsParcer/android");
            if (!pathAndroid.exists()) {
                pathAndroid.mkdirs();
                if (debug) Log.d(TAG, "mkdirs");
            }
            File androidFile = new File(pathAndroid, fileNameAndroid);
            if (androidFile.exists()) {
                HashMap<String, String> androidStringMap = parseFromAndroid(androidFile);
                Set<Map.Entry<String, String>> entries = androidStringMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    entry.getKey();
                    entry.getValue();
                }
            }
        }
    }


    private HashMap<String, String> parseFromAndroid(File androidFile) {
        HashMap<String, String> androidMap = new HashMap<>();
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
                        if (parser.getName().equals("string")) {
                            gotText = false;
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                if (parser.getAttributeName(i).equals("name")) {
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
                            androidMap.put(key, value);
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
        return androidMap;
    }

    private void simpleParse(File androidFile) {
        HashMap<String, String> androidMap = new HashMap<>();
        if (androidFile.exists()) {
            StringBuilder stringBuilder = new StringBuilder();
            if (debug) Log.d("Test", "file exists");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(androidFile));
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (debug) Log.d("Test", "setText");
            textViewOutput.setText(stringBuilder.toString());
        }
    }
}
