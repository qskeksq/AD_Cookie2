package com.example.administrator.cookie2;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... params) {
                return urlNetworking();
            }
            @Override
            protected void onPostExecute(String result) {
                textView.setText(result);
            }
        }.execute();
    }

    private String urlNetworking() {
        try {
            URL url = new URL("http://10.0.2.2:3000");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.addRequestProperty("cookie", cookieStore.get());       // 아하 이런식으로 넣어줘서 보내줄 수 있다.

            // 답변해주거나 답변을 받아올 때 모두 스트림을 열어서 주고 받는다.
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.e("확인", "HTTP_OK");
                InputStream is = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(reader);

                // 헤더는 배열을 값으로 갖는 맵으로 되어 있다.
                Map<String, List<String>> headers = conn.getHeaderFields();
                Iterator<String> iterator = headers.keySet().iterator();
                Log.e("헤더", headers.toString());
                while(iterator.hasNext()){
                    Log.e("Headers", "headers = "+iterator.next());    // 다음에 해당하는 object 반환
                }

                SharedPreferences sp = getSharedPreferences("MySp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("cookie", headers.get("Set-Cookie").get(0));   // 헤더의 값을 이렇게 가져올 수 있군.
                // 쿠기의 목적은 자신을 구분하는 아이디, 구분자, 크데렌셜이다. 쿠키를 사용하고, 판별하고, 암호화해서 주는 것은 서버가 하는 것이고, 우리가 할 것은
                // 잘 저장하고 있다가 잘 보내주기만 하면 됨.

                // 결국 쿠기는 일련의 문자열이기 때문에 저장 방식은 매우 다양하다. 내가 적절하도록 텍스트 파일, sd, 캐시, sp 등 여러 방법이 있다.
                // 다만 안드로이드가 미리 만들어 놓은 안드로이드 쿠키 저장소를 사용할 수 있다.
                List<String> cookies = headers.get("Set-Cookie");
                CookieManager.setDefault(new CookieManager());
                CookieManager cookieManager = (CookieManager) CookieManager.getDefault();
                URI uri = new URI(url.toString());
                cookieManager.getCookieStore().add(uri, HttpCookie.parse(cookies.get(0)).get(0));
                conn.addRequestProperty("Cookies", cookieManager.getCookieStore().get(uri).get(0).toString());

                // 따라서 0. 쿠키를 사용하는 이유는 크레덴셜이 전부다. 1. 쿠기는 일련의 문자열이고 저장 방식이 다양한다.
                //        2. 따라서 적절한 때 꺼내 보내주기만 하면 된다. 3. 기본 구동 방식으은 알고 okHttp 사용하자

                return br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("확인", "HTTP_NOT_OK");
        return "error";
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView);
    }
}
