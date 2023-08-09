package com.example.individualassignment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText TotalBill, TotalPeople;
    private LinearLayout layoutIndividualPercentages;
    private LinearLayout layoutIndividualAmounts;
    private Button btnCalculate, btnShare;
    private TextView tvResult;
    private double totalBillAmount;
    private int totalPeople;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TotalBill = findViewById(R.id.TotalBill);
        TotalPeople = findViewById(R.id.TotalPeople);
        radioGroup = findViewById(R.id.radioGroup);
        layoutIndividualPercentages = findViewById(R.id.layoutIndPercentages);
        layoutIndividualAmounts = findViewById(R.id.layoutIndAmounts);
        tvResult = findViewById(R.id.tvResult);
        btnCalculate = findViewById(R.id.btnCal);
        btnShare = findViewById(R.id.btnShare);

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBillBreakdown();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareBillBreakdownResults();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radEqual) {
                    // Show the Equal breakdown option
                    findViewById(R.id.layoutIndPercentages).setVisibility(View.GONE);
                    findViewById(R.id.layoutIndAmounts).setVisibility(View.GONE);
                } else if (checkedId == R.id.radPercentage) {
                    // Show the  Percentage custom breakdown options
                    findViewById(R.id.layoutIndPercentages).setVisibility(View.VISIBLE);
                    findViewById(R.id.layoutIndAmounts).setVisibility(View.GONE);

                    // Clear previous EditText views
                    layoutIndividualPercentages.removeAllViews();

                    // Obtain the total of people entered by the user
                    totalPeople = Integer.parseInt(TotalPeople.getText().toString());

                    // Add EditText views for percentages per pax
                    for (int i = 0; i < totalPeople; i++) {
                        EditText etPercentage = new EditText(MainActivity.this);
                        etPercentage.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        etPercentage.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        etPercentage.setHint("Person " + (i + 1));
                        layoutIndividualPercentages.addView(etPercentage);
                    }
                } else if (checkedId == R.id.radAmount) {
                    // Show the custom breakdown options by Amount
                    findViewById(R.id.layoutIndPercentages).setVisibility(View.GONE);
                    findViewById(R.id.layoutIndAmounts).setVisibility(View.VISIBLE);

                    // Clear previous EditText views
                    layoutIndividualAmounts.removeAllViews();
                    totalPeople = Integer.parseInt(TotalPeople.getText().toString());

                    // Add EditText views for amounts per pax
                    for (int i = 0; i < totalPeople; i++) {
                        EditText etAmount = new EditText(MainActivity.this);
                        etAmount.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        etAmount.setHint("Person " + (i + 1));
                        layoutIndividualAmounts.addView(etAmount);
                    }
                }
            }
        });
    }

    private void calculateBillBreakdown() {
        try {
            totalBillAmount = Double.parseDouble(TotalBill.getText().toString());
            totalPeople = Integer.parseInt(TotalPeople.getText().toString());
        } catch (NumberFormatException e) {
            // Handle invalid input
            return;
        }

        double[] amounts = new double[totalPeople];
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (radioGroup.getCheckedRadioButtonId() == R.id.radEqual) {
            // Equal Breakdown
            double equalAmount = totalBillAmount / totalPeople;
            for (int i = 0; i < totalPeople; i++) {
                amounts[i] = equalAmount;
            }
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radPercentage) {
            // Custom Breakdown by Percentage
            double totalPercentage = 0;
            for (int i = 0; i < totalPeople; i++) {
                EditText etPercentage = (EditText) layoutIndividualPercentages.getChildAt(i);
                if (etPercentage == null || etPercentage.getText().toString().isEmpty()) {
                    // Handle empty input
                    Toast.makeText(MainActivity.this, "Sorry, please enter valid percentages.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    double percentage = Double.parseDouble(etPercentage.getText().toString());
                    totalPercentage += percentage;
                    amounts[i] = (totalBillAmount * percentage) / 100;
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    Toast.makeText(MainActivity.this, "Sorry, please enter valid percentages.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (totalPercentage != 100) {
                // Handle invalid input (total percentage is not 100%)
                Toast.makeText(MainActivity.this, "Total percentage need to be 100%.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radAmount) {
            double totalAmount = 0;
            for (int i = 0; i < totalPeople; i++) {
                // Retrieve the EditText view for amount per pax from layoutIndividualAmounts
                EditText etAmount = (EditText) layoutIndividualAmounts.getChildAt(i);
                if (etAmount == null || etAmount.getText().toString().isEmpty()) {
                    // Handle empty input
                    Toast.makeText(MainActivity.this, "Sorry, please enter valid amounts for all people.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    double amount = Double.parseDouble(etAmount.getText().toString());
                    totalAmount += amount;
                    amounts[i] = amount;
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    Toast.makeText(MainActivity.this, "Sorry, please enter valid amounts.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // Use a threshold to check if the total amount is equal to the total bill amount
            double threshold = 0.01;
            if (Math.abs(totalAmount - totalBillAmount) > threshold) {
                // Notify user if total amount is not equal to the actual total bill amount
                Toast.makeText(MainActivity.this, "Total amount need to be equal to the total bill amount.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Display the calculated bill
        StringBuilder resultText = new StringBuilder();
        for (int i = 0; i < totalPeople; i++) {
            resultText.append("Person ").append(i + 1).append(": RM ").append(decimalFormat.format(amounts[i])).append("\n");
        }
        tvResult.setText(resultText.toString());
        tvResult.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.VISIBLE);
    }

    private void shareBillBreakdownResults() {
        // Get the breakdown results as a string
        String breakdownResults = tvResult.getText().toString();

        // Create an Intent to share the breakdown results
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Bill Breakdown");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, breakdownResults);

        // Show the sharing options to the user
        startActivity(Intent.createChooser(sharingIntent, "Share breakdown results via"));
    }
}