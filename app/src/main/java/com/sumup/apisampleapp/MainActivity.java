package com.sumup.apisampleapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sumup.merchant.api.SumUpAPI;
import com.sumup.merchant.api.SumUpPayment;
import java.math.BigDecimal;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

  private static final String CALLBACK_URI = "sumupsampleresult://result";
  private static final int REQUEST_CODE_PAYMENT = 2;

  private EditText mAffiliateKey;
  private EditText mAppId;
  private EditText mTotal;
  private EditText mCurrency;
  private EditText mForeignTxId;

  private TextView mResultCode;
  private TextView mResultMessage;
  private TextView mTxCode;
  private TextView mReceiptSent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViews();

    Button btnCheckoutIntent = (Button) findViewById(R.id.button_charge_intent);
    btnCheckoutIntent.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            BigDecimal total;
            SumUpPayment.Currency currency;
            try {
              total = new BigDecimal(mTotal.getText().toString());
              currency = SumUpPayment.Currency.valueOf(mCurrency.getText().toString());
            } catch (IllegalArgumentException e) {
              Toast.makeText(
                      MainActivity.this, R.string.invalid_total_or_currency, Toast.LENGTH_SHORT)
                  .show();
              return;
            }

            SumUpPayment payment =
                SumUpPayment.builder()
                    // mandatory parameters
                    // Please go to https://me.sumup.com/developers to get your Affiliate Key by
                    // entering the application ID of your app. (e.g. com.sumup.sdksampleapp)
                    .affiliateKey(mAffiliateKey.getText().toString())
                    .total(total)
                    .currency(currency) // optional: add details
                    .title("Taxi Ride")
                    .receiptEmail("customer@mail.com")
                    .receiptSMS("+3531234567890")
                    // optional: Add metadata
                    .addAdditionalInfo("AccountId", "taxi0334")
                    .addAdditionalInfo("From", "Paris")
                    .addAdditionalInfo("To", "Berlin")
                    // optional: foreign transaction ID, must be unique!
                    .foreignTransactionId(foreignTransactionId()) // can not exceed 128 chars
                    // optional: skip the success screen
                    .skipSuccessScreen()
                    .build();

            SumUpAPI.checkout(MainActivity.this, payment, REQUEST_CODE_PAYMENT);
          }
        });

    Button btnCheckoutUri = (Button) findViewById(R.id.button_charge_uri);
    btnCheckoutUri.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Uri payUri =
                new Uri.Builder()
                    .scheme("sumupmerchant")
                    .authority("pay")
                    .appendPath("1.0")
                    .appendQueryParameter("affiliate-key", mAffiliateKey.getText().toString())
                    .appendQueryParameter("app-id", mAppId.getText().toString())
                    // field available from App version 1.88.0 and above. Otherwise keep
                    // deprecated field "amount"
                    .appendQueryParameter("total", mTotal.getText().toString())
                    .appendQueryParameter("currency", mCurrency.getText().toString())
                    .appendQueryParameter("title", "Taxi Ride")
                    .appendQueryParameter("receipt-mobilephone", "+3531234567890")
                    .appendQueryParameter("receipt-email", "customer@mail.com")
                    .appendQueryParameter("foreign-tx-id", foreignTransactionId())
                    // optional: skip the success screen
                    .appendQueryParameter("skip-screen-success", "true")
                    .appendQueryParameter("callback", CALLBACK_URI)
                    .build();

            Intent payIntent = new Intent(Intent.ACTION_VIEW, payUri);
            startActivity(payIntent);
          }
        });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    resetViews();

    if (requestCode == REQUEST_CODE_PAYMENT && data != null) {
      Bundle extra = data.getExtras();
      if (extra == null) {
        return;
      }

      mResultCode.setText(
          getString(R.string.result_code_format, extra.getInt(SumUpAPI.Response.RESULT_CODE)));
      mResultMessage.setText(
          getString(R.string.message_format, extra.getString(SumUpAPI.Response.MESSAGE)));

      String txCode = extra.getString(SumUpAPI.Response.TX_CODE);
      mTxCode.setText(txCode == null ? "" : getString(R.string.transaction_code_format, txCode));

      boolean receiptSent = extra.getBoolean(SumUpAPI.Response.RECEIPT_SENT);
      mReceiptSent.setText(getString(R.string.receipt_sent_format, receiptSent));
    }
  }

  private String foreignTransactionId() {
    String foreignTxId = mForeignTxId.getText().toString();
    return foreignTxId.isEmpty() ? UUID.randomUUID().toString() : foreignTxId;
  }

  private void resetViews() {
    mResultCode.setText("");
    mResultMessage.setText("");
    mTxCode.setText("");
    mReceiptSent.setText("");
  }

  private void findViews() {
    mAffiliateKey = (EditText) findViewById(R.id.input_affiliate_key);
    mAppId = (EditText) findViewById(R.id.input_app_id);
    mTotal = (EditText) findViewById(R.id.input_total);
    mCurrency = (EditText) findViewById(R.id.input_currency);
    mForeignTxId = (EditText) findViewById(R.id.input_foreign_tx_id);
    mResultCode = (TextView) findViewById(R.id.result);
    mResultMessage = (TextView) findViewById(R.id.result_msg);
    mTxCode = (TextView) findViewById(R.id.tx_code);
    mReceiptSent = (TextView) findViewById(R.id.receipt_sent);
  }
}
