package com.example.yepej.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
{
    Random numberGenerator = new Random();
    ArrayList<String> celebs = new ArrayList<String>();
    Button[] buttonArray = new Button[4];
    Button answerButton;
    Button answerButton1;
    Button answerButton2;
    Button answerButton3;
    int answerIndex = 0;
    int correctButton = 0;

    public class DownloaderHTML extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params)
        {
            String html = "";

            try
            {
                URL url = new URL(params[0]);
                HttpURLConnection connection = ((HttpURLConnection) url.openConnection());

                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1)
                {
                    char current = ((char) data);
                    html += current;
                    data = reader.read();
                }

                return html;
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloaderImage extends AsyncTask<String, Void, Bitmap>
    {

        @Override
        protected Bitmap doInBackground(String... params)
        {
            Bitmap img = null;

            try
            {
                URL url = new URL(params[0]);
                HttpURLConnection connection = ((HttpURLConnection) url.openConnection());

                connection.connect();

                InputStream in = connection.getInputStream();

                img = BitmapFactory.decodeStream(in);

                return img;
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeButtons();

        startGame();


    }

    private void startGame()
    {
        String result = "";

        DownloaderHTML downloaderHTML = new DownloaderHTML();

        try
        {
            result = downloaderHTML.execute("http://www.posh24.se/kandisar").get();
            addCelebrityToList(result);
            displayImage();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
    }

    private void initializeButtons()
    {
        answerButton = ((Button) findViewById(R.id.answerButton));
        answerButton1 = ((Button) findViewById(R.id.answerButton1));
        answerButton2 = ((Button) findViewById(R.id.answerButton2));
        answerButton3 = ((Button) findViewById(R.id.answerButton3));

        buttonArray[0] = answerButton;
        buttonArray[1] = answerButton1;
        buttonArray[2] = answerButton2;
        buttonArray[3] = answerButton3;
    }


    private void addCelebrityToList(String html)
    {
        html += html.replace("\n", "").replace("\t", "");

        Pattern p = Pattern.compile("<div class=\"image\">(.*?)</div>");
        Matcher m = p.matcher(html);

        while (m.find())
        {
            celebs.add(m.group(1).replace("\"", ""));
        }
    }

    private void displayImage()
    {
        answerIndex = numberGenerator.nextInt(100);
        DownloaderImage downloader = new DownloaderImage();
        Bitmap picture = null;
        ImageView img = ((ImageView) findViewById(R.id.img));
        Pattern p = Pattern.compile("=(.*?) a");
        Matcher m = p.matcher(celebs.get(answerIndex));

        if (m.find())
        {
            try
            {
                picture = downloader.execute(m.group(1)).get();
                img.setImageBitmap(picture);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
        }

        setAnswers();
    }

    private void setAnswers()
    {
        Pattern p = Pattern.compile("alt=(.*?)/");
        Matcher m = p.matcher(celebs.get(answerIndex));
        correctButton = numberGenerator.nextInt(3);

        if (m.find())
        {
            for (int i = 0; i < buttonArray.length; i++)
            {
                if (i == correctButton)
                {
                    buttonArray[i].setText(m.group(1));
                    buttonArray[i].setTag("true");
                }
                else
                {
                    int randomAnswer = numberGenerator.nextInt(100);
                    Matcher m2 = p.matcher(celebs.get(randomAnswer));
                    m2.find();
                    buttonArray[i].setText(m2.group(1));
                }
            }
        }
    }

    public void buttonClick(View control)
    {
        if (control.getTag().equals("true"))
        {
            Toast.makeText(this, "Correct", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "InCorrect", Toast.LENGTH_SHORT).show();
        }

        buttonArray[0].setTag("");
        buttonArray[1].setTag("");
        buttonArray[2].setTag("");
        buttonArray[3].setTag("");

        displayImage();
    }
}
