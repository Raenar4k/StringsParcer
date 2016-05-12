package com.raenarapps.stringsparser;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.raenarapps.stringsparser.Constants.ParserAndroid;
import com.raenarapps.stringsparser.Constants.ParserCSV;
import com.raenarapps.stringsparser.Constants.ParserIOS;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StringsParser";
    private boolean debugAndroid = false;
    private boolean debugIOS = false;
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
        editTextiOS.setText("localizable");
        textViewOutput = (TextView) findViewById(R.id.textViewOutput);
        Button buttonMerge = (Button) findViewById(R.id.buttonMerge);
        Button buttonSplit = (Button) findViewById(R.id.buttonSplit);
        if (buttonMerge != null) {
            buttonMerge.setOnClickListener(this);
        }
        if (buttonSplit != null) {
            buttonSplit.setOnClickListener(this);
        }
        makeDirectoriesIfNeeded();
    }

    private void makeDirectoriesIfNeeded() {
        File pathAndroid = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_ANDROID);
        if (!pathAndroid.exists()) {
            pathAndroid.mkdirs();
        }
        File pathiOS = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_IOS);
        if (!pathiOS.exists()) {
            pathiOS.mkdirs();
        }
        File pathMerged = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_MERGED);
        if (!pathMerged.exists()) {
            pathMerged.mkdirs();
        }
    }

    @Override
    public void onClick(View v) {

        File pathAndroid = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_ANDROID);
        File pathiOS = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_IOS);

        switch (v.getId()) {
            case R.id.buttonMerge:
                String fileNameAndroid = editTextAndroid.getText().toString() + Constants.ANDROID_EXT;
                File androidFile = new File(pathAndroid, fileNameAndroid);
                if (!androidFile.exists())
                    Toast.makeText(this, R.string.toast_android_notfound, Toast.LENGTH_SHORT).show();
                String fileNameiOS = editTextiOS.getText().toString() + Constants.IOS_EXT;
                File iOSFile = new File(pathiOS, fileNameiOS);
                if (!iOSFile.exists())
                    Toast.makeText(this, R.string.toast_ios_notfound, Toast.LENGTH_SHORT).show();

                if (androidFile.exists() && iOSFile.exists()) {
                    Observable<List<StringObject>> androidObservable = Observable.just(androidFile)
                            .map(this::parseFromAndroid);
                    Observable<List<StringObject>> iOSObservable = Observable.just(iOSFile)
                            .map(this::parseFromIOS);

                    Observable<List<StringObject>> mergeObservable = Observable.zip(
                            androidObservable,
                            iOSObservable,
                            this::mergeLists);

                    mergeObservable
                            .map(this::exportToCSV)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aVoid -> {
                                Toast.makeText(MainActivity.this, R.string.toast_merge,
                                        Toast.LENGTH_SHORT).show();
                            }, e -> {
                                Log.e(TAG, "mergeObservable onError: " + e.getMessage());
                            });
                }
                break;
            case R.id.buttonSplit:
                File mergedFile = new File(Environment.getExternalStorageDirectory()
                        + Constants.DIRECTORY_MERGED, ParserCSV.FILENAME);
                if (mergedFile.exists()) {
                    Observable.just(mergedFile)
                            .map(this::importFromCSV)
                            .map(mergedStringsList -> {
                                parsetoIOS(mergedStringsList);
                                parseToAndroid(mergedStringsList);
                                return null;
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aVoid -> {
                                Toast.makeText(MainActivity.this, R.string.toast_split,
                                        Toast.LENGTH_SHORT).show();
                            }, e -> {
                                Log.e(TAG, "splitObservable onError: " + e.getMessage());
                            });
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_merge_not_found,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private List<StringObject> mergeLists(List<StringObject> androidList, List<StringObject> iosList) {
        List<StringObject> mergedStringsList = new ArrayList<>();
        Map<String, String> usedKeysMap = new HashMap<>();

        addToMergedList(androidList, mergedStringsList, usedKeysMap);
        addToMergedList(iosList, mergedStringsList, usedKeysMap);
        return mergedStringsList;
    }

    private void addToMergedList(List<StringObject> listToMerge,
                                 List<StringObject> mergedList,
                                 Map<String, String> usedKeysMap) {
        for (StringObject stringObject : listToMerge) {
            String key = stringObject.getKey();
            String value = stringObject.getValue();
            String os = stringObject.getOs();
            if (!usedKeysMap.containsKey(key)) {
                usedKeysMap.put(key, value);
                mergedList.add(new StringObject(key, value, os));
            } else {
                String storedValue = usedKeysMap.get(key);
                if (!storedValue.equals(value)) {
                    mergedList.add(new StringObject(key, value, os));
                } else {
                    for (StringObject mergedStringObject : mergedList) {
                        if (mergedStringObject.getKey().equals(key)
                                && mergedStringObject.getValue().equals(value)
                                && !mergedStringObject.getOs().equals(os)) {
                            mergedStringObject.setOs(Constants.OS_ANY);
                            break;
                        }
                    }
                }
            }
        }
    }


    private void parsetoIOS(List<StringObject> mergedStringsList) {
        File file = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_IOS,
                ParserIOS.NEW_FILENAME);
        try {
            FileWriter writer = new FileWriter(file);
            for (StringObject stringObject : mergedStringsList) {
                if (stringObject.getOs().equals(Constants.OS_IOS)
                        || stringObject.getOs().equals(Constants.OS_ANY)) {
                    writer.write(getString(R.string.IOS_string_format,
                            stringObject.getKey(), stringObject.getValue()));
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<StringObject> parseFromIOS(File file) {
        List<StringObject> iOSStringList = new ArrayList<>();
        try {
            Scanner fileScanner = new Scanner(new FileInputStream(file));
            String line;
            if (!fileScanner.hasNextLine()) {
                if (debugIOS) Log.d(TAG, "parseFromIOS: fail scan");
            }
            while (fileScanner.hasNextLine()) {
                line = fileScanner.nextLine();
                line = line.replace(";", "");
                line = line.replace("\"", "");
                String[] splitStrings = line.split("=");
                String key = splitStrings[0].trim();
                String value = splitStrings[1].trim();
                if (debugIOS) Log.d(TAG, "parseFromIOS: key =" + key + ";");
                if (debugIOS) Log.d(TAG, "parseFromIOS: value =" + value + ";");
                iOSStringList.add(new StringObject(key, value, Constants.OS_IOS));
            }
            //todo better resource handling
            fileScanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return iOSStringList;
    }

    private List<StringObject> importFromCSV(File file) {
        List<StringObject> mergedStringsList = new ArrayList<>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(file), ';');
            String nextLine[];
            while ((nextLine = csvReader.readNext()) != null) {
                if (!nextLine[ParserCSV.OS_INDEX].equals(ParserCSV.OS)) {
                    String key = nextLine[ParserCSV.KEY_INDEX];
                    //skipping value, getting new value instead
                    String value = nextLine[ParserCSV.NEW_VALUE_INDEX];
                    String os = nextLine[ParserCSV.OS_INDEX];
                    mergedStringsList.add(new StringObject(key, value, os));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mergedStringsList;
    }

    private Void exportToCSV(List<StringObject> mergedStringsList) {
        File fileMerged = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_MERGED,
                ParserCSV.FILENAME);
        try {
            fileMerged.createNewFile();
            CSVWriter csvWriter = new CSVWriter(new FileWriter(fileMerged, false), ';');
            String[] header = {ParserCSV.KEY, ParserCSV.VALUE, ParserCSV.NEW_VALUE, ParserCSV.OS};
            csvWriter.writeNext(header);
            for (StringObject stringObject : mergedStringsList) {
                String[] data = {stringObject.getKey(), stringObject.getValue(), "", stringObject.getOs()};
                csvWriter.writeNext(data);
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseToAndroid(List<StringObject> mergedStringsList) {
        File file = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_ANDROID,
                ParserAndroid.NEW_FILENAME);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, ParserAndroid.ENCODING);
            serializer.setFeature(ParserAndroid.FEATURE_INDENT, true);
            serializer.startTag("", ParserAndroid.RESOURCES);
            for (StringObject stringObject : mergedStringsList) {
                if (stringObject.getOs().equals(Constants.OS_ANDROID)
                        || stringObject.getOs().equals(Constants.OS_ANY)) {
                    serializer.startTag(null, ParserAndroid.STRING);
                    serializer.attribute(null, ParserAndroid.NAME, stringObject.getKey());
                    serializer.text(stringObject.getValue());
                    serializer.endTag(null, ParserAndroid.STRING);
                }
            }
            serializer.endTag("", ParserAndroid.RESOURCES);
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
                        if (debugAndroid) Log.d(TAG, "START_DOCUMENT");
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(ParserAndroid.STRING)) {
                            gotText = false;
                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                if (parser.getAttributeName(i).equals(ParserAndroid.NAME)) {
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
                            androidStringsList.add(new StringObject(key, value, Constants.OS_ANDROID));
                            if (debugAndroid)
                                Log.d(TAG, "END_TAG: key = " + key + " value = " + value);
                            key = null;
                            value = null;
                        }
                        break;
                    default:
                        break;
                }
                parser.next();
            }
            if (debugAndroid) Log.d(TAG, "END_DOCUMENT");
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
