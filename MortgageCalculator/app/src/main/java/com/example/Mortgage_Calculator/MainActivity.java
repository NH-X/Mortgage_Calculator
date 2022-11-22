package com.example.Mortgage_Calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener ,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private EditText edit_purchasePrice, edit_mortgageRatio, edit_commercialLoans, edit_providentFund;
    private TextView text_loanAmount, text_payment;
    private RadioGroup rg_repayment;
    private CheckBox cb_commercialLoans, cb_providentFund;
    private Spinner spinner_loanPeriod, spinner_baseRate;

    private int mYear; // 还款年限
    private double mBusinessRatio; // 商业贷款的利率
    private double mAccumulationRatio; // 公积金贷款的利率

    private boolean isInterest = true;          // 是否为等额本息
    private boolean hasBusiness = true;         // 是否存在商业贷款
    private boolean hasAccumulation = false;    // 是否存在公积金贷款

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_purchasePrice = findViewById(R.id.edit_purchasePrice);           //购房总价输入框
        edit_mortgageRatio = findViewById(R.id.edit_mortgageRatio);           //按揭百分比输入框
        text_loanAmount = findViewById(R.id.text_loanAmount);                 //贷款金额显示文本
        rg_repayment = findViewById(R.id.rg_repayment);                       //单选按钮组
        cb_commercialLoans = findViewById(R.id.cb_commercialLoans);           //商业贷款复选框
        edit_commercialLoans = findViewById(R.id.edit_commercialLoans);       //商业贷款输入框
        cb_providentFund = findViewById(R.id.cb_providentFund);               //公积金复选框
        edit_providentFund = findViewById(R.id.edit_providentFund);           //公积金输入框
        spinner_loanPeriod = findViewById(R.id.spinner_loanPeriod);           //贷款年限选择器
        spinner_baseRate = findViewById(R.id.spinner_baseRate);               //基准利率选择器
        text_payment = findViewById(R.id.text_payment);               //还款明细

        findViewById(R.id.btn_loanAmount).setOnClickListener(this::onClick);      //计算贷款金额按钮
        findViewById(R.id.btn_repaymentDetails).setOnClickListener(this::onClick);//计算还款细明按钮

        cb_commercialLoans.setOnCheckedChangeListener(this::onCheckedChanged);
        cb_providentFund.setOnCheckedChangeListener(this::onCheckedChanged);
        rg_repayment.setOnCheckedChangeListener(this::onCheckedChanged);

        initialization();
        initYearSpinner();
        initbaseRateSpinner();
    }

    //初始化操作
    private void initialization() {
        text_loanAmount.setText("0");
        text_payment.setText("贷款总额为0万元\n还款总额为0万元\n利息总额为0万元\n还款总时间为0月\n每月还款金额为0.0元");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_loanAmount:
                text_loanAmount.setText(calculateLoan());
                break;
            case R.id.btn_repaymentDetails:
                if (hasBusiness && TextUtils.isEmpty(edit_commercialLoans.getText().toString())) {
                    Toast.makeText(this, "商业贷款不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (hasAccumulation && TextUtils.isEmpty(edit_providentFund.getText().toString())) {
                    Toast.makeText(this, "公积金不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!hasBusiness && !hasAccumulation) {
                    Toast.makeText(this, "请选择商业贷款或者公积金", Toast.LENGTH_SHORT).show();
                    return;
                }
                calculateRepaymentDetails();
                break;
            default:
                break;
        }
    }

    //计算贷款金额
    private String calculateLoan() {
        if (TextUtils.isEmpty(edit_purchasePrice.getText().toString())) {
            Toast.makeText(this, "购房总价不能为空！", Toast.LENGTH_SHORT).show();
            return "0";
        } else if (TextUtils.isEmpty(edit_mortgageRatio.getText().toString())) {
            Toast.makeText(this, "按揭部分不能为空", Toast.LENGTH_SHORT).show();
            return "0";
        }
        String purchasePrice = String.valueOf(edit_purchasePrice.getText());
        String mortgageRatio = String.valueOf(edit_mortgageRatio.getText());
        double result = Double.valueOf(purchasePrice) / 100 * Integer.valueOf(mortgageRatio);

        return String.format("%.3f", result);
    }

    //年限选择
    private String[] loanPeriodArray = new String[]{"5年", "10年", "15年", "20年", "30年"};
    private int[] yearArray = new int[]{5, 10, 15, 20, 30};
    //基准利率选择
    private String[] baseRateArray = new String[]{"2015年10月24日 五年期商贷利率 4.90%　公积金利率 3.25%",
            "2015年08月26日 五年期商贷利率 5.15%　公积金利率 3.25%",
            "2015年06月28日 五年期商贷利率 5.40%　公积金利率 3.50%",
            "2015年05月11日 五年期商贷利率 5.65%　公积金利率 3.75%",
            "2015年03月01日 五年期商贷利率 5.90%　公积金利率 4.00%",
            "2014年11月22日 五年期商贷利率 6.15%　公积金利率 4.25%",
            "2012年07月06日 五年期商贷利率 6.15%　公积金利率 4.50%"};
    private double[] businessArray = {4.90, 5.15, 5.40, 5.65, 5.90, 6.15, 6.55};
    private double[] accumulationArray = {3.25, 3.25, 3.50, 3.75, 4.00, 4.25, 4.50};

    //初始化贷款年限下拉框
    private void initYearSpinner() {
        ArrayAdapter<String> yearSpinner = new ArrayAdapter<>(this, R.layout.item_select, loanPeriodArray);
        yearSpinner.setDropDownViewResource(R.layout.item_dropdown);
        spinner_loanPeriod.setAdapter(yearSpinner);
        spinner_loanPeriod.setSelection(0);
        spinner_loanPeriod.setOnItemSelectedListener(new YearSelectedListener());
    }

    private class YearSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mYear = yearArray[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    //chushihua基准利率选择
    private void initbaseRateSpinner() {
        ArrayAdapter<String> baseRateSpinner = new ArrayAdapter<>(this, R.layout.item_select, baseRateArray);
        baseRateSpinner.setDropDownViewResource(R.layout.item_dropdown);
        spinner_baseRate.setAdapter(baseRateSpinner);
        spinner_baseRate.setSelection(0);
        spinner_baseRate.setOnItemSelectedListener(new baseRateSelectedListener());
    }

    private class baseRateSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mBusinessRatio = businessArray[position];
            mAccumulationRatio = accumulationArray[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    //计算还款明细
    private void calculateRepaymentDetails() {
        Repayment businessResult = new Repayment();
        Repayment accumulationResult = new Repayment();

        if (hasBusiness) {    //选择了商贷
            double businessLoad = Double.parseDouble(edit_commercialLoans.getText().toString()) * 10000;
            double businessTime = mYear * 12;
            double businessRate = mBusinessRatio / 100;
            businessResult = calMortgage(businessLoad, businessTime, businessRate, isInterest);
        }
        if (hasAccumulation) {    //选择了公积金
            double accumulationLoad = Double.parseDouble(edit_providentFund.getText().toString()) * 10000;
            double accumulationTime = mYear * 12;
            double accumulationRate = mAccumulationRatio / 100;
            accumulationResult = calMortgage(accumulationLoad, accumulationTime, accumulationRate, isInterest);
        }
        String desc = String.format("贷款总额为%s万元", formatDecimal(
                (businessResult.mTotal + accumulationResult.mTotal) / 10000, 2));
        desc = String.format("%s\n还款总额为%s万元", desc, formatDecimal(
                (businessResult.mTotal + businessResult.mTotalInterest +
                        accumulationResult.mTotal + accumulationResult.mTotalInterest) / 10000, 2));
        desc = String.format("%s\n利息总额为%s万元", desc, formatDecimal(
                (businessResult.mTotalInterest + accumulationResult.mTotalInterest) / 10000, 2));
        desc = String.format("%s\n还款总时间为%d月", desc, mYear * 12);
        if (isInterest) { // 如果是等额本息方式
            desc = String.format("%s\n每月还款金额为%s元", desc, formatDecimal(
                    businessResult.mMonthRepayment + accumulationResult.mMonthRepayment, 2));
        } else { // 如果是等额本金方式
            desc = String.format("%s\n首月还款金额为%s元，其后每月递减%s元", desc, formatDecimal(
                            businessResult.mMonthRepayment + accumulationResult.mMonthRepayment, 2),
                    formatDecimal(businessResult.mMonthMinus + accumulationResult.mMonthMinus, 2));
        }
        text_payment.setText(desc);
    }

    // 精确到小数点后第几位
    private String formatDecimal(double value, int digit) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(digit, RoundingMode.HALF_UP);
        return bd.toString();
    }

    // 根据贷款金额、还款年限、基准利率，计算还款信息
    private Repayment calMortgage(double ze, double nx, double rate, boolean bInterest) {
        double zem = (ze * rate / 12 * Math.pow((1 + rate / 12), nx))
                / (Math.pow((1 + rate / 12), nx) - 1);
        double amount = zem * nx;
        double rateAmount = amount - ze;

        double benjinm = ze / nx;
        double lixim = ze * (rate / 12);
        double diff = benjinm * (rate / 12);
        double huankuanm = benjinm + lixim;
        double zuihoukuan = diff + benjinm;
        double av = (huankuanm + zuihoukuan) / 2;
        double zong = av * nx;
        double zongli = zong - ze;

        Repayment result = new Repayment();
        result.mTotal = ze;
        if (bInterest) {
            result.mMonthRepayment = zem;
            result.mTotalInterest = rateAmount;
        } else {
            result.mMonthRepayment = huankuanm;
            result.mMonthMinus = diff;
            result.mTotalInterest = zongli;
        }
        return result;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_interest) {
            isInterest = true;
        } else if (checkedId == R.id.rb_principal) {
            isInterest = false;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_commercialLoans) {        //勾选了商业贷款
            hasBusiness = isChecked;
        } else if (buttonView.getId() == R.id.cb_providentFund) {     //勾选了额公积金
            hasAccumulation = isChecked;
        }
    }
}