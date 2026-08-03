package com.awrtyi.bmi;   

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class MainActivity extends AppCompatActivity {

    // 控件
    private SwitchCompat switchUnit;
    private SeekBar seekHeight, seekWeight;
    private EditText etHeight, etWeight;
    private TextView tvUnitLabel, tvHeightUnit, tvWeightUnit, tvResult;
    private Button btnCalculate;

    // 当前单位模式，true=公制，false=英制
    private boolean isMetric = true;

    // 存储当前的数值（公制单位：身高cm，体重kg）
    private double heightCm = 160;    // 默认1.60米
    private double weightKg = 60;     // 默认60公斤

    // 防止循环更新的标志
    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定控件
        switchUnit = findViewById(R.id.switch_unit);
        seekHeight = findViewById(R.id.seek_height);
        seekWeight = findViewById(R.id.seek_weight);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        tvUnitLabel = findViewById(R.id.tv_unit_label);
        tvHeightUnit = findViewById(R.id.tv_height_unit);
        tvWeightUnit = findViewById(R.id.tv_weight_unit);
        tvResult = findViewById(R.id.tv_result);
        btnCalculate = findViewById(R.id.btn_calculate);

        // 初始化界面（公制）
        updateUnitMode(true);

        // 单位切换监听
        switchUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUnitMode(isChecked);
        });

        // 身高滑动条监听
        seekHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !isUpdating) {
                    // 根据模式更新对应变量
                    if (isMetric) {
                        heightCm = progress;
                    } else {
                        heightCm = progress * 2.54; // 英寸转厘米
                    }
                    updateInputFields();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 体重滑动条监听
        seekWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !isUpdating) {
                    if (isMetric) {
                        weightKg = progress;
                    } else {
                        weightKg = progress * 0.453592; // 磅转公斤
                    }
                    updateInputFields();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 身高输入框监听
        etHeight.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                String str = s.toString().trim();
                if (str.isEmpty()) return;
                try {
                    double value = Double.parseDouble(str);
                    if (isMetric) {
                        // 公制：身高单位是厘米，但输入框显示米，所以乘以100
                        heightCm = value * 100;
                    } else {
                        // 英制：输入框直接是英寸
                        heightCm = value * 2.54;
                    }
                    // 限制范围
                    if (heightCm < 50) heightCm = 50;
                    if (heightCm > 300) heightCm = 300;
                    // 同步滑动条
                    isUpdating = true;
                    if (isMetric) {
                        seekHeight.setProgress((int) heightCm);
                    } else {
                        seekHeight.setProgress((int) (heightCm / 2.54));
                    }
                    isUpdating = false;
                    // 更新显示（修正可能四舍五入的显示）
                    updateInputFields();
                } catch (NumberFormatException ignored) {}
            }
        });

        // 体重输入框监听
        etWeight.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                String str = s.toString().trim();
                if (str.isEmpty()) return;
                try {
                    double value = Double.parseDouble(str);
                    if (isMetric) {
                        weightKg = value; // 公制直接是公斤
                    } else {
                        weightKg = value * 0.453592; // 磅转公斤
                    }
                    if (weightKg < 10) weightKg = 10;
                    if (weightKg > 500) weightKg = 500;
                    isUpdating = true;
                    if (isMetric) {
                        seekWeight.setProgress((int) weightKg);
                    } else {
                        seekWeight.setProgress((int) (weightKg / 0.453592));
                    }
                    isUpdating = false;
                    updateInputFields();
                } catch (NumberFormatException ignored) {}
            }
        });

        // 计算按钮
        btnCalculate.setOnClickListener(v -> calculateBMI());
    }

    /**
     * 根据单位模式更新所有UI：滑动条范围、进度、单位标签、输入框数值
     */
    private void updateUnitMode(boolean metric) {
        isMetric = metric;
        // 更新单位名称和符号
        if (metric) {
            tvUnitLabel.setText("公制");
            tvHeightUnit.setText("cm");
            tvWeightUnit.setText("kg");
        } else {
            tvUnitLabel.setText("英制");
            tvHeightUnit.setText("in");
            tvWeightUnit.setText("lb");
        }

        // 调整滑动条范围并设置进度
        if (metric) {
            seekHeight.setMax(300);
            seekWeight.setMax(500);
            // 进度直接使用厘米和公斤
            seekHeight.setProgress((int) heightCm);
            seekWeight.setProgress((int) weightKg);
        } else {
            seekHeight.setMax(100);
            seekWeight.setMax(500);
            seekHeight.setProgress((int) (heightCm / 2.54));
            seekWeight.setProgress((int) (weightKg / 0.453592));
        }
        // 更新输入框显示
        updateInputFields();
    }

    /**
     * 根据当前存储的(heightCm, weightKg)和单位模式更新输入框文本
     */
    private void updateInputFields() {
        isUpdating = true;
        if (isMetric) {
            // 身高显示为米，保留两位小数
            double heightM = heightCm / 100.0;
            etHeight.setText(String.format("%.2f", heightM));
            // 体重显示公斤，保留一位小数
            etWeight.setText(String.format("%.1f", weightKg));
        } else {
            double heightIn = heightCm / 2.54;
            double weightLb = weightKg / 0.453592;
            etHeight.setText(String.format("%.1f", heightIn));
            etWeight.setText(String.format("%.1f", weightLb));
        }
        isUpdating = false;
    }

    /**
     * 计算并显示BMI结果
     */
    private void calculateBMI() {
        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);
        String rating;

        if (bmi < 18.5) {
            rating = "过轻";
        } else if (bmi <= 22.9) {
            rating = "正常";
        } else if (bmi <= 24.9) {
            rating = "偏胖";
        } else if (bmi <= 29.9) {
            rating = "肥胖";
        } else if (bmi <= 40) {
            rating = "重度肥胖";
        } else {
            rating = "极度肥胖";
        }

        String unitText = isMetric ? "公制" : "英制";
        String result = String.format("单位：%s\nBMI = %.2f\n体重评级：%s", unitText, bmi, rating);
        tvResult.setText(result);
    }
}
//你好！！