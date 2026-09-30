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
import com.stroeer.ads.formats.interstitial.StroeerInterstitialFullListener;
import com.stroeer.ads.formats.interstitial.StroeerInterstitialView;
import com.stroeer.ads.formats.rewarded.StroeerRewardedFullListener;
import com.stroeer.ads.formats.rewarded.StroeerRewardedView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
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
    StroeerRewardedView rewardedAd;

    boolean isLoading = false; // As the reload button is used, we need to track if the ad is already loading to prevent multiple loads with a single instance. if you don't have reload button, you don't need this variable.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Yieldlove SDK
        StroeerSDK.setApplicationName(getApplicationContext(),"appTest");

        // Enable debug mode for Yieldlove SDK
        // This will provide additional logs for debugging purposes.
        StroeerSDK.enableDebugMode();

        // Enable Debug Panel
        StroeerSDK.enableInspectionMode();

        boolean useConfiant = false;

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

            bannerAd.getBannerConfig().setCustomTargeting(Map.of(
                    "context", "localContext",
                    "user", "localUser",
                    "section", "localCustomValue, localCustomValue2"
            ));

            //banner, banner2, banner3 can be used in the publisherSlotName
            bannerAd.load("b1", new StroeerBannerListener() {
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
            // Otherwise the reload button would stay blocked forever.
            isLoading = false;
        }
    }

    public void btnPrivacyClick(View view) {
        this.consent.showPrivacyManager();
    }

    public void btnReloadClick(View view){
        // You may not need to use this reloading logic because you don't have a reload button.
        // This is just to prevent multiple loads with a single instance.
        if(isLoading){
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
            // Release any previous instance before starting a new load.
            destroyInterstitial();

            // Interstitials must be created with an Activity context, never the application context.
            interstitialAd = new StroeerInterstitialView(this);

            // loadAfterReady defaults to true, which makes the SDK show the ad as soon as it is
            // loaded. Set it to false when you want to decide yourself when the ad appears.
            interstitialAd.setLoadAfterReady(false);

            // StroeerInterstitialFullListener adds the full-screen callbacks on top of
            // onAdLoaded/onAdFailedToLoad that StroeerInterstitialListener provides.
            interstitialAd.load("interstitial", new StroeerInterstitialFullListener() {  // <-- put here your adslot name
                @Override
                public void onAdLoaded() {
                    // With loadAfterReady = false it is up to us to show the ad.
                    if (interstitialAd != null) {
                        interstitialAd.show();
                    }
                }

                @Override
                public void onAdFailedToLoad(StroeerException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Interstitial load failed", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Toast.makeText(getApplicationContext(), "Interstitial shown", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(StroeerException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Interstitial failed to show", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Toast.makeText(getApplicationContext(), "Interstitial dismissed", Toast.LENGTH_SHORT).show();
                    // The ad object cannot be shown twice, so release it here.
                    destroyInterstitial();
                }

                @Override
                public void onAdImpression() {
                    Toast.makeText(getApplicationContext(), "Interstitial impression", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdClicked() {
                    Toast.makeText(getApplicationContext(), "Interstitial clicked", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnRewardedClick(View view) {
        try {
            // Release any previous instance before starting a new load.
            destroyRewarded();

            // Rewarded ads must be created with an Activity context, never the application context.
            rewardedAd = new StroeerRewardedView(this);

            // Same as for interstitials: take control over when the ad is shown.
            rewardedAd.setLoadAfterReady(false);

            rewardedAd.load("rewarded", new StroeerRewardedFullListener() {  // <-- put here your adslot name
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    if (rewardedAd != null) {
                        rewardedAd.show();
                    }
                }

                @Override
                public void onAdFailedToLoad(StroeerException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Rewarded load failed", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onUserEarnedReward(RewardItem item) {
                    // Grant the reward to the user here.
                    Toast.makeText(
                            getApplicationContext(),
                            "Reward earned: " + item.getAmount() + " " + item.getType(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Toast.makeText(getApplicationContext(), "Rewarded shown", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(StroeerException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Rewarded failed to show", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    Toast.makeText(getApplicationContext(), "Rewarded dismissed", Toast.LENGTH_SHORT).show();
                    // The ad object cannot be shown twice, so release it here.
                    destroyRewarded();
                }

                @Override
                public void onAdImpression() {
                    Toast.makeText(getApplicationContext(), "Rewarded impression", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdClicked() {
                    Toast.makeText(getApplicationContext(), "Rewarded clicked", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        destroyAd();
        destroyInterstitial();
        destroyRewarded();

        // StroeerConsent holds this Activity and a coroutine scope, so it has to be released too.
        // destroy() is terminal: the instance cannot be reused afterwards.
        if (this.consent != null) {
            this.consent.destroy();
            this.consent = null;
        }

        super.onDestroy();
    }

    private void destroyAd(){
        if(bannerAd != null) {
            // destroy() releases the ad, but it does not detach the view from its container,
            // so remove it here to avoid stacking dead banners on every reload.
            bannerAd.destroy();
            ViewGroup parent = (ViewGroup) bannerAd.getParent();
            if (parent != null) {
                parent.removeView(bannerAd);
            }
            bannerAd = null;
        }
    }

    private void destroyInterstitial(){
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
    }

    private void destroyRewarded(){
        if (rewardedAd != null) {
            rewardedAd.destroy();
            rewardedAd = null;
        }
    }
}