package com.elpibedelavilla.miip;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private TextView ipText, locationText, flagText, statusText;
    private Button refreshButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean busy = false;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        buildScreen(); // Deliberately no automatic IP request.
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }
    private int dp(int value) { return (int)(getResources().getDisplayMetrics().density * value + .5f); }
    private TextView text(String value, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(sp); t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private void buildScreen() {
        FrameLayout root = new FrameLayout(this);
        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.tv_background);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(background, new FrameLayout.LayoutParams(-1,-1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24),dp(12),dp(24),dp(12));
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1,-1);
        root.addView(content, cp);

        float widthDp = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
        boolean small = widthDp < 700;
        TextView title = text("El Pibe de la Villa", small ? 32 : 52, Color.rgb(146,255,255), true);
        content.addView(title, new LinearLayout.LayoutParams(-1,0,1.1f));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(16),dp(8),dp(16),dp(8));
        panel.setBackground(shape(Color.argb(242,0,0,0), 12));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1,0,2.2f);
        pp.setMargins(0,0,0,dp(10));
        content.addView(panel,pp);

        panel.addView(text("TU IP ACTUAL ES",small ? 19 : 26,Color.WHITE,true));
        ipText = text("—",small ? 29 : 43,Color.rgb(146,255,255),true);
        ipText.setSingleLine(true);
        ipText.setAutoSizeTextTypeUniformWithConfiguration(18, 43, 1, 1);
        panel.addView(ipText);
        LinearLayout placeRow = new LinearLayout(this);
        placeRow.setOrientation(LinearLayout.HORIZONTAL);
        placeRow.setGravity(Gravity.CENTER);
        locationText = text("Pulsa REFRESH para consultar",small ? 16 : 22,Color.WHITE,false);
        flagText = text("",small ? 24 : 31,Color.WHITE,false);
        placeRow.addView(locationText);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-2,-2);
        fp.leftMargin = dp(12);
        placeRow.addView(flagText,fp);
        panel.addView(placeRow);

        refreshButton = new Button(this);
        refreshButton.setText("⟳  REFRESH / ACTUALIZAR IP");
        refreshButton.setTextSize(small ? 16 : 19);
        refreshButton.setTextColor(Color.WHITE);
        refreshButton.setAllCaps(false);
        refreshButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0,119,155)));
        refreshButton.setOnClickListener(v -> queryIp());
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(small ? dp(320) : dp(410),dp(60));
        content.addView(refreshButton,bp);
        statusText = text("Consulta manual · Sin VPN o con VPN",14,Color.rgb(190,213,232),false);
        content.addView(statusText,new LinearLayout.LayoutParams(-1,0,.7f));
        setContentView(root);
        refreshButton.requestFocus();
    }
    private String flagEmoji(String code) {
        if (code == null || !code.matches("(?i)[A-Z]{2}")) return "";
        String upper = code.toUpperCase(java.util.Locale.ROOT);
        return new String(Character.toChars(0x1F1E6 + upper.charAt(0) - 'A')) +
               new String(Character.toChars(0x1F1E6 + upper.charAt(1) - 'A'));
    }
    private void queryIp() {
        if (busy) return;
        busy = true;
        refreshButton.setEnabled(false);
        statusText.setText("Consultando IP...");
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL("https://ipwho.is/").openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "ElPibeDeLaVillaMiIP/1.0");
                if (connection.getResponseCode() != 200) throw new Exception("Servicio no disponible (HTTP " + connection.getResponseCode() + ")");
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line; while ((line = reader.readLine()) != null) response.append(line);
                }
                JSONObject json = new JSONObject(response.toString());
                if (!json.optBoolean("success",false)) throw new Exception("La consulta no está disponible ahora");
                String ip = json.optString("ip","—");
                String city = json.optString("city","");
                String country = json.optString("country","");
                String code = json.optString("country_code","");
                String location = (city.isEmpty() ? "" : city + ", ") + country;
                runOnUiThread(() -> {
                    ipText.setText(ip);
                    locationText.setText(location.isEmpty() ? "Ubicación no disponible" : location);
                    flagText.setText(flagEmoji(code));
                    statusText.setText("Consulta completada · Pulsa REFRESH para actualizar");
                    busy = false; refreshButton.setEnabled(true); refreshButton.requestFocus();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    statusText.setText("No se pudo consultar la IP. Pulsa REFRESH para reintentar.");
                    busy = false; refreshButton.setEnabled(true); refreshButton.requestFocus();
                });
            } finally { if (connection != null) connection.disconnect(); }
        });
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
            finish(); return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    @Override protected void onDestroy() { executor.shutdownNow(); super.onDestroy(); }
}
