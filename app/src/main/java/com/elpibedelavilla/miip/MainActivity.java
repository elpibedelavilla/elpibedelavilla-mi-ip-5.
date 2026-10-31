
package com.elpibedelavilla.miip;

import android.app.Activity;
import android.content.res.ColorStateList;
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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private TextView ipText, locationText, flagText, statusText;
    private Button refreshButton;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private volatile boolean busy = false;

    private final int CYAN = Color.rgb(0, 235, 255);
    private final int LIGHT_CYAN = Color.rgb(146, 255, 255);

    private int dp(float value) {
        return (int) (getResources()
                .getDisplayMetrics().density * value + 0.5f);
    }

    private GradientDrawable shape(
            int color, int radius, boolean border) {

        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));

        if (border) {
            d.setStroke(dp(2), CYAN);
        }
        return d;
    }

    private TextView text(
            String value, float size, int color, boolean bold) {

        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);

        if (bold) {
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return t;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        buildScreen();
    }

    private void buildScreen() {

        FrameLayout root = new FrameLayout(this);

        // MAPA MUNDIAL DE FONDO
        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.tv_background);
        background.setScaleType(
                ImageView.ScaleType.CENTER_CROP);

        root.addView(background,
                new FrameLayout.LayoutParams(-1, -1));

        float widthDp = getResources()
                .getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density;

        boolean small = widthDp < 700;

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(20), dp(8), dp(20), dp(8));

        root.addView(content,
                new FrameLayout.LayoutParams(-1, -1));

        // CABECERA
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);

      // Título integrado en la imagen de fondo


        header.addView(subtitle);

        content.addView(header,
                new LinearLayout.LayoutParams(-1, 0, 1.1f));

        // ZONA CENTRAL
        FrameLayout middle = new FrameLayout(this);

        content.addView(middle,
                new LinearLayout.LayoutParams(-1, 0, 3.1f));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(
                dp(18), dp(12), dp(18), dp(12));

        panel.setBackground(shape(
                Color.argb(235, 0, 9, 23),
                18, true));

        panel.setElevation(dp(12));

        FrameLayout.LayoutParams panelParams =
                new FrameLayout.LayoutParams(
                        small ? dp(360) : dp(570),
                        small ? dp(180) : dp(230),
                        Gravity.CENTER);

        panelParams.leftMargin = dp(4);
        panelParams.rightMargin = dp(4);

        middle.addView(panel, panelParams);

        TextView label = text(
                "TU IP ACTUAL ES",
                small ? 18 : 24,
                Color.WHITE, true);

        panel.addView(label);

        ipText = text(
                "—",
                small ? 30 : 48,
                CYAN, true);

        ipText.setSingleLine(true);
        ipText.setAutoSizeTextTypeUniformWithConfiguration(
                18, 48, 1, 1);

        LinearLayout.LayoutParams ipParams =
                new LinearLayout.LayoutParams(-1, -2);
        ipParams.topMargin = dp(10);
        ipParams.bottomMargin = dp(10);

        panel.addView(ipText, ipParams);

        LinearLayout placeRow = new LinearLayout(this);
        placeRow.setOrientation(LinearLayout.HORIZONTAL);
        placeRow.setGravity(Gravity.CENTER);

        locationText = text(
                "Pulsa REFRESH para consultar",
                small ? 15 : 21,
                Color.WHITE, false);

        flagText = text(
                "",
                small ? 23 : 30,
                Color.WHITE, false);

        placeRow.addView(locationText);

        LinearLayout.LayoutParams flagParams =
                new LinearLayout.LayoutParams(-2, -2);
        flagParams.leftMargin = dp(12);

        placeRow.addView(flagText, flagParams);
        panel.addView(placeRow);

        // BOTÓN ACTUALIZAR
        LinearLayout buttonArea = new LinearLayout(this);
        buttonArea.setGravity(Gravity.CENTER);

        content.addView(buttonArea,
                new LinearLayout.LayoutParams(-1, 0, 1.0f));

        refreshButton = new Button(this);

        refreshButton.setText(
                "⟳  REFRESH / ACTUALIZAR IP");

        refreshButton.setTextSize(small ? 15 : 19);
        refreshButton.setTextColor(Color.WHITE);
        refreshButton.setAllCaps(false);

        refreshButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.rgb(0, 135, 180)));

        refreshButton.setElevation(dp(8));

        refreshButton.setOnClickListener(
                v -> queryIp());

        buttonArea.addView(refreshButton,
                new LinearLayout.LayoutParams(
                        small ? dp(310) : dp(440),
                        dp(62)));

        // ESTADO
        statusText = text(
                "Pulsa REFRESH para consultar tu IP",
                small ? 12 : 15,
                Color.rgb(205, 225, 240),
                false);

        content.addView(statusText,
                new LinearLayout.LayoutParams(-1, 0, .65f));

        // VERSIÓN
        TextView version = text(
                "v1.1.0",
                12,
                Color.rgb(165, 185, 200),
                false);

        FrameLayout.LayoutParams vp =
                new FrameLayout.LayoutParams(
                        -2, -2,
                        Gravity.RIGHT | Gravity.BOTTOM);

        vp.setMargins(0, 0, dp(20), dp(12));
        root.addView(version, vp);

        setContentView(root);
        refreshButton.requestFocus();
    }

    private String flagEmoji(String code) {

        if (code == null ||
                !code.matches("(?i)[A-Z]{2}")) {
            return "";
        }

        String upper = code.toUpperCase(Locale.ROOT);

        return new String(Character.toChars(
                0x1F1E6 + upper.charAt(0) - 'A')) +
                new String(Character.toChars(
                0x1F1E6 + upper.charAt(1) - 'A'));
    }

    private void queryIp() {

        if (busy) return;

        busy = true;
        refreshButton.setEnabled(false);
        statusText.setText("Consultando IP...");

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                connection = (HttpURLConnection)
                        new URL("https://ipwho.is/")
                                .openConnection();

                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                connection.setRequestProperty(
                        "Accept", "application/json");

                connection.setRequestProperty(
                        "User-Agent",
                        "ElPibeDeLaVillaMiIP/1.1");

                if (connection.getResponseCode() != 200) {
                    throw new Exception(
                            "Servicio no disponible");
                }

                StringBuilder response =
                        new StringBuilder();

                try (BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream(),
                                        java.nio.charset.StandardCharsets.UTF_8))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject json =
                        new JSONObject(response.toString());

                if (!json.optBoolean("success", false)) {
                    throw new Exception(
                            "Consulta no disponible");
                }

                String ip = json.optString("ip", "—");
                String city = json.optString("city", "");
                String country = json.optString("country", "");
                String code = json.optString("country_code", "");

                String location =
                        (city.isEmpty() ? "" : city + ", ") +
                        country;

                runOnUiThread(() -> {

                    ipText.setText(ip);

                    locationText.setText(
                            location.isEmpty()
                                    ? "Ubicación no disponible"
                                    : location);

                    flagText.setText(flagEmoji(code));

                    statusText.setText(
                            "Consulta completada · Pulsa REFRESH para actualizar");

                    busy = false;
                    refreshButton.setEnabled(true);
                    refreshButton.requestFocus();
                });

            } catch (Exception ex) {

                runOnUiThread(() -> {

                    statusText.setText(
                            "No se pudo consultar la IP. Pulsa REFRESH para reintentar.");

                    busy = false;
                    refreshButton.setEnabled(true);
                    refreshButton.requestFocus();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_ESCAPE ||
                keyCode == KeyEvent.KEYCODE_BACK) {

            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
