package com.myexample.userconsentdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RadioGroup;

public class ConsentDialog {

    private Context context;
    private SharedPreferences settings;

    private final Dialog dialog;

    private ConsentDialog.NoticeDialogListener mListener;

    private final RadioGroup radiogroupAds;
    private final RadioGroup radiogroupFirebaseAnalytics;

    public ConsentDialog(Context context, SharedPreferences settings){
        this.context = context;
        this.settings = settings;

        this.dialog = new Dialog(context);
        this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.dialog.setContentView(R.layout.dialog_consent);

        mListener = (NoticeDialogListener) context;

        radiogroupAds = (RadioGroup)dialog.findViewById(R.id.radio_Ads);
        radiogroupFirebaseAnalytics = (RadioGroup)dialog.findViewById(R.id.radio_FirebaseAnalytics);
    }

    public interface NoticeDialogListener {
        public void onConsentDialogNegativeClick();
        public void onConsentDialogPositiveClick();
    }

    public void showConsentDialog(){

        // Set text: PrivacyPolicy
        String consentPrivacyPolicyMessage = getConsentPrivacyPolicyMessage();
        WebView myWebViewPrivacyPolicy = (WebView) dialog.findViewById(R.id.webViewPrivacyPolicy_consentDialog);
        setWebViewContent(myWebViewPrivacyPolicy, consentPrivacyPolicyMessage);

        // Set text: CookiePolicy
        String consentCookiePolicyMessage = getConsentCookiePolicyMessage();
        WebView myWebViewCookiePolicy = (WebView) dialog.findViewById(R.id.webViewCookiePolicy_consentDialog);
        setWebViewContent(myWebViewCookiePolicy, consentCookiePolicyMessage);

        // Set radio buttons
        if (settings.getBoolean("isFirstRun", true)) {
            radiogroupAds.check(R.id.radio_Ads_option01);
            radiogroupFirebaseAnalytics.check(R.id.radio_FirebaseAnalytics_option01);
        } else {
            if(!settings.getBoolean("adsNonPersonalized", true)){
                radiogroupAds.check(R.id.radio_Ads_option01);
            } else {
                radiogroupAds.check(R.id.radio_Ads_option02);
            }

            if(!settings.getBoolean("offFirebaseAnalytics", true)){
                radiogroupFirebaseAnalytics.check(R.id.radio_FirebaseAnalytics_option01);
            } else {
                radiogroupFirebaseAnalytics.check(R.id.radio_FirebaseAnalytics_option02);
            }
        }

        // Set buttons
        final Button dialogButtonNegative = (Button) dialog.findViewById(R.id.buttonNegative_consentDialog);
        dialogButtonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onConsentDialogNegativeClick();
                dialog.dismiss();
            }
        });

        Button dialogButtonPositive = (Button) dialog.findViewById(R.id.buttonPositive_consentDialog);
        dialogButtonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onConsentDialogPositiveClick();
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void setWebViewContent(WebView myWebView, String consentMessage){
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultFontSize(8);
        myWebView.loadData(consentMessage, "text/html; charset=utf-8", "utf-8");
    }

    public View getCheckedAdsRadioButtonView(){
        int AdsCheckedRadioButtonId = radiogroupAds.getCheckedRadioButtonId();
        return dialog.findViewById(AdsCheckedRadioButtonId);
    }

    public View getCheckedFirebaseAnalyticsRadioButtonView(){
        int firebaseAnalyticsCheckedRadioButtonId = radiogroupFirebaseAnalytics.getCheckedRadioButtonId();
        return dialog.findViewById(firebaseAnalyticsCheckedRadioButtonId);
    }

    private String getConsentPrivacyPolicyMessage() {
        return "Your Privacy Policy";
    }

    private String getConsentCookiePolicyMessage() {
        return "Your Cookie Policy";
    }

}
