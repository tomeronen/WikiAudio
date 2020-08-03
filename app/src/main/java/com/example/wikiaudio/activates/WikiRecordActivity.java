package com.example.wikiaudio.activates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.wikiaudio.R;
import com.example.wikiaudio.audio_player.AudioPlayer;
import com.example.wikiaudio.audio_recoder.AudioRecorder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class WikiRecordActivity extends AppCompatActivity {

    private static final int REQ_RECORD_PERMISSION = 12;
    WebView wikiPage;
    ProgressBar progressBar;
    String DEBUG_URL = "https://en.wikipedia.org/wiki/Android_(operating_system)";
    FloatingActionButton recordButton;
    boolean startRecording = true; // when button pressed record our stop recording?
    AppCompatActivity activity;
    private MediaRecorder recorder;
    private String pageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_recored_acrivaty);
        wikiPage = findViewById(R.id.wikiPage);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(6);
        recordButton = findViewById(R.id.recoredButton);
        String html = "<p><b>Hadera</b> (Hebrew: <link rel=\"mw-deduplicated-inline-style\" href=\"mw-data:TemplateStyles:r940540609\"><span dir=\"rtl\">חֲדֵרָה</span>, Arabic: <span lang=\"ar\" dir=\"rtl\">الخضيرة</span>\u200E, <small>romanized: </small><i lang=\"ar-Latn\" title=\"Arabic-language romanization\">al-Ḫuḍayrah</i>) is a city located in the Haifa District of Israel, in the northern Sharon region, approximately 45 kilometers (28 miles) from the major cities of Tel Aviv and Haifa. The city is located along 7 km (5 mi) of the Israeli Mediterranean Coastal Plain. The city's population includes a high proportion of immigrants arriving since 1990, notably from Ethiopia and the former Soviet Union. In 2018 it had a population of 95,683.</p><p>Hadera was established in 1891 as a farming colony by members of the Zionist group, Hovevei Zion, from Lithuania and Latvia. By 1948, it was a regional center with a population of 11,800. In 1952, Hadera was declared a city, with jurisdiction over an area of 53,000 dunams.</p>\n" +
                "\n" +
                "\n" +
                "<h2><span id=\"History\">History</span></h2>\n" +
                "<h3><span id=\"Ottoman_era\">Ottoman era</span></h3>\n" +
                "\n" +
                "\n" +
                "<p>Hadera was founded on 24 January 1891, in the early days of modern Zionism by Jewish immigrants from Lithuania and Latvia on land purchased by Yehoshua Hankin, known as the Redeemer of the Valley. The land was purchased from a Christian effendi, Selim Khuri. This was the largest purchase of land in Eretz Israel by a Zionist group, although the land was of low quality and mostly swampland. The only inhabitants prior to the purchase were a few families raising water buffaloes and selling reeds. The village was named after <i>Wadi al-Khudeira</i> (Arabic: <span lang=\"ar\" dir=\"rtl\">وادي الخضيرة\u200E\u200E</span>\u200E, <small>lit. </small>'the valley of verdure'), as the nearby section of Hadera Stream was known. Earlier, the whole Hadera Stream had been known as <i>Nahr Akhdar</i> (Arabic: <span lang=\"ar\" dir=\"rtl\">نهر الأخضر</span>\u200E, <small>lit. </small>'green river').</p><p>The Crusaders called the location <i>Lictera</i> – a corruption of the Arabic name, <i>el-Khudeira</i>. From the outset, attempts were made to pick instead a Hebrew name for the new settlement. About half a year after it was founded, rabbi Ya'akov Goldman reported on an event in \"the moshav of <i>Hadere</i>, that is, <i>Hatzor</i>\". The name <i>Liktera</i> was in preferential use by the British military during World War I.</p><p>Baron Rothschild's surveyor, Yitzhak Goldhar, claimed that Hadera was founded on the site of the former town called <i>Gedera of Caesarea</i> (Hebrew: <span lang=\"he\" dir=\"rtl\">גדרה של קיסרין</span>\u200E), as mentioned in Tosefta <i>Shevi'it</i>, ch. 7. Benjamin Mazar preferred to locate ancient <i>Gador</i>, formerly known as <i>Gedera by Caesaria</i>, at Tell Ahḍar (\"green hill\"), later known as Tell esh Sheikh Ziraq and currently as Tel Gador, on the coast south of Giv'at Olga. Others say that the ancient Gadera should be identified with Umm Qais or with al-Judeira.\n" +
                "</p><p>The first Jewish settlers lived in a building known as the Khan near Hadera's main synagogue. The population consisted of ten families and four guards. Baron Edmond de Rothschild provided funding for Egyptian laborers to drain the swamps. Old tombstones in the local cemetery reveal that out of a population of 540, 210 died of malaria. Therefore a Bible verse from the Psalms (Tehillim) was inscribed in the city's logo: \"Those who sow in tears, will reap with songs of joy.\" (Ps 126:5) Hashomer guards kept watch over the fields to prevent incursions by the neighboring Bedouin.\n" +
                "</p><p>By the early twentieth century, Hadera had become the regional economic center. In 1913, the settlement included forty households, as well as fields and vineyards, stretching over 30,000 dunam.</p>\n" +
                "<h3><span id=\"British_Mandate\">British Mandate</span></h3>\n" +
                "<p>Land disputes in the area were resolved by the 1930s, and the population had grown to 2,002 in 1931. Free schooling was introduced in the city in 1937 in all schools apart from the Histadrut school.</p>\n" +
                "\n" +
                "<ul class=\"gallery mw-gallery-traditional\"><li class=\"gallerybox\" style=\"width: 155px\">\n" +
                "\t\t<li class=\"gallerybox\" style=\"width: 155px\">\n" +
                "</ul><h3><span id=\"State_of_Israel\">State of Israel</span></h3>\n" +
                "<p>After the 1948 War, the north-western part of  Hadera (including \"Newe Chayyim\") expanded on the land which had  belonged to the depopulated Palestinian village  of Arab al-Fuqara.</p><p>Hadera's population increased dramatically in 1948 as immigrants flocked to the country. Most of the newcomers were from Europe, though 40 Yemenite families settled there, too. In 1953, Israel's first paper mill opened in Hadera. Financed by investors from Israel, United States, Brazil and Australia, the mill was designed to meet all of Israel's paper needs.New neighborhoods were built, among them Givat Olga on the coast, and Beit Eliezer in the east of the city. In 1964, Hadera was declared a city.</p><p>In the 1990s, large numbers of Russian and Ethiopian immigrants settled in Hadera. Hadera, considered a safe place by its inhabitants, was jolted by several acts of terrorism during the second intifada. On October 28, 2001, four civilians were killed when a terrorist opened fire on pedestrians at a bus stop. A massacre of six civilians at a Bat Mitzvah occurred in early 2002. A suicide bomber blew himself up at a falafel stand on October 26, 2005, killing seven civilians and injuring 55, five in severe condition. However, since the construction of the West Bank barrier, the frequency of such incidents has dropped drastically. During the second Lebanon War, on August 4, 2006, three rockets fired by Hezbollah hit Hadera. Hadera is 50 miles (80 km) south of the Lebanese border and marked the farthest point inside Israel hit by Hezbollah.</p><p>In the 2000s, the city center was rejuvenated, a high-tech business park was constructed, and the world's largest desalination plant was built. New neighborhoods are under construction in the underdeveloped northeastern part of the city, and plans are under way for a large park, shopping malls and hotels with a total of 1,800 rooms. The city is envisaged as a future vacation destination due to its closeness to the Galilee, beaches, and access to major highways.</p>\n" +
                "<h2><span id=\"Geography_and_wildlife\">Geography and wildlife</span></h2>\n" +
                "\n" +
                "<p>Hadera is located on the Israeli Mediterranean coastal plain, 45 km (28 mi) north of Tel Aviv.  The city's jurisdiction covers 53,000 dunams (53.0 km<sup>2</sup>; 20.5 sq mi), making it the fourth largest city in the country. Nahal Hadera Park, a eucalyptus forest covering 1,300 dunams (1.3 km<sup>2</sup>; 0.5 sq mi) and Hasharon Park are located on the outskirts of Hadera.</p><p>Hot water gushing from the Hadera power plant draws schools of hundreds of sandbar and dusky sharks every winter. Scientists are researching the rare phenomenon, which is unknown in the vicinity. It is speculated that the water, which is ten degrees warmer than the rest of the sea, may be the attraction.</p>\n" +
                "<h2><span id=\"Transportation\">Transportation</span></h2>\n" +
                "<p>Hadera lies along two main Israel Railways lines: the Coastal Line and the nowadays freight-only Eastern Line. The city's railway station is located in the west of the city and is on the Tel Aviv suburban line which runs between Binyamina and Ashkelon. The city center of Hadera is located near Israel's two main north-south highways; Highway 2, linking Tel Aviv to Haifa, and Highway 4. This made Hadera an important junction for all coastal bus transportation after 1948 and into the 1950s.\n" +
                "</p>\n" +
                "<h2><span id=\"Economy\">Economy</span></h2>\n" +
                "\n" +
                "<p>Hadera Paper, established in 1953, continues to be a major employer in the city. The world's largest desalination plant of its type, was inaugurated in December 2009. Hadera is the location of the Orot Rabin Power Plant, Israel's largest power station.</p>\n" +
                "<h2><span id=\"Demographics\">Demographics</span></h2>\n" +
                "<p>According to the Israel Central Bureau of Statistics, as of October 2013, Hadera had a population of 91,634 which is growing at an annual rate of 1.2%. As of 2003, the city had a population density of 1,516.6 per km<sup>2</sup>. Of the city's population of 2013 of 91,634, approximately 23,407 were immigrants, many from Ethiopia.</p><p>According to a census conducted in 1922 by the British Mandate authorities, Hadera had a population of 540 inhabitants, consisting of 450 Jews, 89 Muslims and 1 Christian. \n" +
                "Hadera has grown steadily since 1948, when the city had a population of 11,800. In 1955, the population almost doubled to 22,500. In 1961 it rose to 25,600, 1972 to 32,200, and 1983, to 38,700.</p><p>The median age in Hadera is 32.8, with 23,200 people 19 years of age or younger, 12.1% between 20 and 29, 14,100 between 30 and 44, 17,600 from 45 to 64, and 9,700, 65 or older. As of 2007, there were 37,500 males and 39,200 females.</p><p>In 2001, the ethnic makeup was 99.2% Jewish and other non-Arab, with no significant Arab population. In 2000, there were 27,920 salaried workers and 1,819 self-employed. The mean monthly wage in 2000 for a salaried worker was ILS 5,135, a real change of 8.0% over the course of 2000. Salaried males had a mean monthly wage of ILS 6,607 (a real change of 9.0%) compared with ILS 3,598 for females (a real change of 3.1%). The mean income for the self-employed was 6,584. A total of 1,752 people received unemployment benefits and 6,753 received income supplements.\n" +
                "</p>\n" +
                "<h2><span id=\"Education\">Education</span></h2>\n" +
                "\n" +
                "\n" +
                "<p>In 2001, there were 15,622 students studying at 42 schools (24 elementary schools with 7,933 students, and 21 high schools with 7,689 students). A total of 57.5% of 12th graders were entitled to a matriculation certificate.\n" +
                "</p><p>The Democratic School of Hadera, which opened in 1987, was the first of its kind in Israel. The Technoda, an educational center for science and technology equipped with a state-of-the-art telescope and planetarium, is located in Hadera's Givat Olga neighborhood.</p>\n" +
                "<h2><span id=\"Medical_facilities\">Medical facilities</span></h2>\n" +
                "<p>Hadera is served by the Hillel Yaffe Medical Center.\n" +
                "</p>\n" +
                "<h2><span id=\"Neighborhoods\">Neighborhoods</span></h2>\n" +
                "<p>Neighborhoods of Hadera include Givat Olga, Beit Eliezer, Kfar Brandeis, Haotzar, Hephzibah, Neve Haim, Nissan, Ephraim, Bilu, Klarin, Nahaliel, Shimshon, Shlomo, Pe'er, Bialik, Beitar and The Park.\n" +
                "</p>\n" +
                "<h2><span id=\"Sports\">Sports</span></h2>\n" +
                "<p>Hadera is home to three current football clubs: Hapoel Hadera, which currently plays in Israeli Premier League after being promoted at the end of 2017/18 season. Beitar Hadera (playing in Liga Gimel Shomron) and the women's football club Maccabi Kishronot Hadera (playing in Ligat Nashim Rishona). In the past the city was also home to Maccabi Hadera, Hapoel Nahliel and Hapoel Beit Eliezer.\n" +
                "</p><p>The city is also represented in the Israeli Beach Soccer League. Its team, Hapoel Hadera, won the championship (under its previous name, Hadera's Princes) in 2008.</p><p>In Basketball, Maccabi Hadera's women's basketball team plays in second tier Liga Leumit, while the club's Maccabi Hadera men's basketball team plays in third tier Liga Artzit.\n" +
                "</p>\n" +
                "<h2><span id=\"Notable_residents\">Notable residents</span></h2>\n" +
                "<ul><li>Eldad Amir (born 1961), Olympic competitive sailor</li>\n" +
                "<li>Shimon Baadani (born 1928), Sephardi rabbi, rosh kollel, and senior leader of the Shas party</li>\n" +
                "<li>Avshalom Feinberg</li>\n" +
                "<li>Shlomo Gronich, musician</li>\n" +
                "<li>Orna Grumberg, computer scientist</li>\n" +
                "<li>Tzuri Gueta, designer</li>\n" +
                "<li>Sarit Hadad, singer</li>\n" +
                "<li>Moshe Kahlon (Givat Olga neighborhood), politician</li>\n" +
                "<li>Yoel Sela (born 1951), Olympic competitive sailor</li>\n" +
                "<li>Baruch Shmailov (born 1994), judoka</li>\n" +
                "<li>Alon Stein (born 1978), basketball player and coach</li></ul><h2><span id=\"Twin_towns_.E2.80.94_sister_cities\"></span><span id=\"Twin_towns_—_sister_cities\">Twin towns — sister cities</span></h2>\n" +
                "\n" +
                "<p>Hadera is twinned with:</p>\n" +
                "\n" +
                "<h2><span id=\"See_also\">See also</span></h2>\n" +
                "<ul><li>Desalination#Israel</li>\n" +
                "<li>Hadera Stream</li></ul><h2><span id=\"References\">References</span></h2>\n" +
                "\n" +
                "<h2><span id=\"External_links\">External links</span></h2>\n" +
                "<ul><li><span><span>Official website</span></span> <span>(in Hebrew)</span></li></ul>";
        wikiPage.loadData(html,"text/html", "UTF-8");
//        wikiPage.setWebViewClient(new MyBrowser());
//        wikiPage.loadUrl(DEBUG_URL);
        activity = this;
        this.pageName = "check";


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean havePermissions = checkRecordingPermissions();
                if (startRecording && havePermissions) {

                    try {
                        // TODO TAKE OUT TO SPECIAL CLASS
                        startRecording = false;
                        startBlinkingAnimation(recordButton);
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        String a =  activity.getFilesDir() + "/" + pageName + ".mp3";
                        recorder.setOutputFile(a);
                        recorder.prepare();
                        recorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (!havePermissions) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQ_RECORD_PERMISSION);
                } else {
                    progressBar.incrementProgressBy(1); //todo for debug only
                    startRecording = true;
                    stopBlinkingAnimation(recordButton);
                    if(recorder != null)
                    {
                        recorder.stop();
                        try {
                            MediaPlayer mp = new MediaPlayer();
                            String a =  activity.getFilesDir() + "/" + pageName + ".mp3";
                            mp.setDataSource(a);
                            mp.prepare();
                            mp.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
    }

    private boolean checkRecordingPermissions() {
        int writeToStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        return writeToStoragePermission == PackageManager.PERMISSION_GRANTED &&
                recordPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_RECORD_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        // TODO - START RECORDING OUR MAKE PRESS AGAIN?

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                        // TODO explain why we need.


                    }
                }
        }
    }


    //
    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private void startBlinkingAnimation(View v){
        Animation mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(600);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        v.startAnimation(mAnimation);
    }

    private void stopBlinkingAnimation(View v){
        v.clearAnimation();
    }

//    private String getFilename()
//    {
//        String filepath = Environment.getExternalStorageDirectory().getPath();
//        File file = new File(filepath,getFilesDir());
//        if(!file.exists()){
//            file.mkdirs();
//        }
//        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp3");
//    }

}
