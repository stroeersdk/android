package com.streer.sdkIntegration;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.stroeer.ads.StroeerSDK;
import com.stroeer.ads.exceptions.StroeerException;
import com.stroeer.ads.formats.banner.StroeerBannerListener;
import com.stroeer.ads.formats.banner.StroeerBannerView;
import com.stroeer.ads.formats.interstitial.StroeerInterstitialListener;
import com.stroeer.ads.formats.interstitial.StroeerInterstitialView;
import com.stroeer.cmp.StroeerConsent;
import com.stroeer.ads.plugins.monitoring.IAdMonitorCallback;
import com.stroeer.ads.plugins.monitoring.confiant.ConfiantLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    StroeerConsent consent;
    StroeerBannerView bannerAd;
    StroeerInterstitialView interstitialAd;

    boolean isLoading = false; // As the reload button is used, we need to track if the ad is already loading to prevent multiple loads with a single instance. if you don't have reload button, you don't need this variable.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Yieldlove SDK
        StroeerSDK.setApplicationName(getApplicationContext(),"appDfpTest");

        // Enable debug mode for Yieldlove SDK
        // This will provide additional logs for debugging purposes.
        StroeerSDK.enableDebugMode();

        // Enable Debug Panel
        StroeerSDK.enableInspectionMode();

        boolean useConfiant = false;
        boolean useGravite = false;

        if(useConfiant) {
            // Please inquire to use this Confiant.
            // This is not available for all publishers.
            ConfiantLoader.getInstance().enableTestMode();      // Enable Test Mode, This function blocks all banners and interstitials.
            ConfiantLoader.getInstance().initialize("", true, new IAdMonitorCallback() { // Please inquire to get the accountId and set up the bundle id in Confiant.
                @Override
                public void onInitialized(boolean success) {

                }
            });
        }

        // Create a map to define custom targeting parameters
        Map<String, List<String>> customTargeting = new HashMap<>();
        // Add context data for the App
        customTargeting.put("context", List.of("sports", "entertainment"));
        // Add user-specific data to the targeting map
        customTargeting.put("user", List.of("sports", "technology", "music", "New York"));
        // Add general Key Value Pair for targeting
        customTargeting.put("section", List.of("soccer"));

        StroeerSDK.setGlobalCustomTargeting(customTargeting);
        StroeerSDK.setContentUrl("https://www.example.com/article/12345");

        // Initialize YieldloveConsent
        // If you have your own consent management system, you can skip this step.
        this.consent = new StroeerConsent(
                this,
                R.id.main_activity_layout);

        this.consent.collect();

        createBanner();
    }

    private void createBanner(){
        final ViewGroup adContainer = findViewById(R.id.generalAdContainer);

        try {
            bannerAd = new StroeerBannerView(this);

            //banner, banner2, banner3 can be used in the publisherSlotName
            bannerAd.load("banner", new StroeerBannerListener() {
                @Override
                public void onAdLoaded(StroeerBannerView banner) {
                    Toast.makeText(getApplicationContext(), "Ad loaded", Toast.LENGTH_SHORT).show();
                    isLoading = false;
                }

                @Override
                public void onAdFailedToLoad(StroeerBannerView yieldloveBannerAdView, StroeerException e) {
                    Toast.makeText(getApplicationContext(), "Ad load failed", Toast.LENGTH_SHORT).show();
                    isLoading = false;
                }

                @Override
                public void onAdOpened(StroeerBannerView yieldloveBannerAdView) {
                    Toast.makeText(getApplicationContext(), "Ad opened", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdClosed(StroeerBannerView yieldloveBannerAdView) {
                    Toast.makeText(getApplicationContext(), "Ad closed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdClicked(StroeerBannerView yieldloveBannerAdView) {
                    Toast.makeText(getApplicationContext(), "Ad clicked", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdImpression(StroeerBannerView yieldloveBannerAdView) {
                    Toast.makeText(getApplicationContext(), "Ad impression", Toast.LENGTH_SHORT).show();
                }
            });

            // this should be added
            adContainer.addView(bannerAd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnPrivacyClick(View view) {
        this.consent.showPrivacyManager();
    }

    public void btnReloadClick(View view){
        // You may not need to use this reloading logic because you don't have a reload button.
        // This is just to prevent multiple loads with a single instance.
        if(isLoading == true){
            Toast.makeText(getApplicationContext(), "Ad is already loading", Toast.LENGTH_SHORT).show();
            return;
        }

        isLoading = true;
        // Release the previous ad instance if it exists
        destroyAd();
        createBanner();
    }

    public void btnConsentClick(View view) {
        this.consent.collect();
    }

    public void btnRemoveConsentClick(View view) {
        this.consent.clearConsent();
        Toast.makeText(this, "Consent reset", Toast.LENGTH_SHORT).show();
    }

    public void btnInterstitialClick(View view) {
        try {
            interstitialAd = new StroeerInterstitialView(this);
            interstitialAd.load("interstitial", new StroeerInterstitialListener(){  // <-- put here your adslot name
                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdFailedToLoad(StroeerException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Ad load failed", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyAd();
        interstitialAd.destroy();
    }

    private void destroyAd(){
        if(bannerAd != null) {
            bannerAd.destroy();
        }
    }
}