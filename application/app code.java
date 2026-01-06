
package com.example.myplant;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.slider.Slider;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SettingActivity extends AppCompatActivity {

    private Slider sliderLight;
    private ToggleButton toggleButton1,toggleButton2;
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 初始化HTTP客户端
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        // 初始化组件
        initViews();

        // 设置监听器
        setupListeners();
    }

    private void initViews() {
        sliderLight = findViewById(R.id.slider_light);

        // 设置Slider的范围 (0-255对应ESP32的PWM范围)
        sliderLight.setValueFrom(0f);
        sliderLight.setValueTo(255f);
        sliderLight.setValue(0f);  // 初始值为0

        toggleButton1=findViewById(R.id.toggle_1);
        toggleButton2 = findViewById(R.id.toggle_2);

    }

    private void setupListeners() {


        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sendDiffuserToESP32(1);
                }else{
                    sendDiffuserToESP32(0);
                }
            }
        });
        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sendEmmiterToESP32(1);
                }else{
                    sendEmmiterToESP32(0);
                }
            }
        });


        // 灯光亮度滑块监听器
        sliderLight.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // 开始拖动时的处理（可选）
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // 停止拖动时发送数据到ESP32
                int brightness = (int) slider.getValue();
                sendBrightnessToESP32(brightness);
            }
        });

        // 实时监听滑块值变化（可选，用于实时显示）
        sliderLight.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (fromUser) {
                    // 可以在这里实时显示当前值
                    // 但不建议实时发送网络请求，会造成频繁请求
                }
            }
        });
    }

    private  void sendDiffuserToESP32(int status){
        String esp32IpAddress = "192.168.10.88";
        String url = "http://" + esp32IpAddress + "/setDiffuser";
        // 构建POST请求体
        RequestBody formBody = new FormBody.Builder()
                .add("status", String.valueOf(status))
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        // 异步发送请求
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                // 请求失败时的处理
                runOnUiThread(() -> {
                    Toast.makeText(SettingActivity.this,
                            "连接ESP32失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 请求成功
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        if (responseBody.startsWith("OK")) {
                            Toast.makeText(SettingActivity.this,
                                    "雾化器设置成功: " + status,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingActivity.this,
                                    "设置失败: " + responseBody,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // HTTP错误
                    runOnUiThread(() -> {
                        Toast.makeText(SettingActivity.this,
                                "HTTP错误: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

    }

    private void sendEmmiterToESP32(int ste) {
        // 构建请求URL
        // ESP32的IP地址 - 需要根据实际情况修改
        String esp32IpAddress = "192.168.10.121:82";
        String url = "http://" + esp32IpAddress + "/setEmiiter";

        // 构建POST请求体
        RequestBody formBody = new FormBody.Builder()
                .add("status_E", String.valueOf(ste))
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        // 异步发送请求
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                // 请求失败时的处理
                runOnUiThread(() -> {
                    Toast.makeText(SettingActivity.this,
                            "连接ESP32失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 请求成功
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        if (responseBody.startsWith("OK")) {
                            Toast.makeText(SettingActivity.this,
                                    "电磁阀设置成功: " + ste,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingActivity.this,
                                    "设置失败: " + responseBody,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // HTTP错误
                    runOnUiThread(() -> {
                        Toast.makeText(SettingActivity.this,
                                "HTTP错误: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    // 发送亮度值到ESP32
    private void sendBrightnessToESP32(int brightness) {
        // 构建请求URL
        // ESP32的IP地址 - 需要根据实际情况修改
        String esp32IpAddress = "192.168.10.88";
        String url = "http://" + esp32IpAddress + "/setBrightness";

        // 构建POST请求体
        RequestBody formBody = new FormBody.Builder()
                .add("brightness", String.valueOf(brightness))
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        // 异步发送请求
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                // 请求失败时的处理
                runOnUiThread(() -> {
                    Toast.makeText(SettingActivity.this,
                            "连接ESP32失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 请求成功
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        if (responseBody.startsWith("OK")) {
                            Toast.makeText(SettingActivity.this,
                                    "亮度设置成功: " + brightness,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingActivity.this,
                                    "设置失败: " + responseBody,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // HTTP错误
                    runOnUiThread(() -> {
                        Toast.makeText(SettingActivity.this,
                                "HTTP错误: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理HTTP客户端资源
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
        }
    }
}
