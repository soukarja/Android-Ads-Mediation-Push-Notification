package com.sdadsManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.onesignal.OneSignal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

public class adsManager {

    private int maxFBAdClicksPerDay = 3;
    private int maxGoogleAdClicksPerDay = 2;
    private int maxCTRPerDay = 30;
    private String ONESIGNAL_APP_ID;


    public String FacebookBanner1, FacebookBanner2, FacebookBanner3;
    public String FacebookInterstitialAdCode, FacebookRewardedAdCode;
    public InterstitialAd FacebookinterstitialAd;

    //Google Ads
    public String GoogleBanner1, GoogleBanner2, GoogleBanner3;
    public String GoogleInterstitialAdCode;
    public String GoogleRewardedAdCode;
    public com.google.android.gms.ads.interstitial.InterstitialAd GoogleinterstitialAd;

    //facebook Rewarded
    public RewardedAd mRewardedAd;

    private SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private String SHARED_PREF;

    private Context context;
    private Activity activity;
    private boolean isRewarded = false;


    public adsManager(Activity activity, boolean testMode) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        AudienceNetworkAds.initialize(context);

        this.ONESIGNAL_APP_ID = "";
        this.SHARED_PREF = "adsSettings";
        this.sp = context.getSharedPreferences(this.SHARED_PREF, Context.MODE_PRIVATE);
        this.ed = sp.edit();

        if (testMode) {

            AdSettings.setTestMode(true);
            this.FacebookBanner1 = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
            this.FacebookBanner2 = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
            this.FacebookBanner3 = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
            this.FacebookInterstitialAdCode = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
            this.FacebookRewardedAdCode = "YOUR_PLACEMENT_ID";


            this.GoogleBanner1 = "ca-app-pub-3940256099942544/6300978111";
            this.GoogleBanner2 = "ca-app-pub-3940256099942544/6300978111";
            this.GoogleBanner3 = "ca-app-pub-3940256099942544/6300978111";
            this.GoogleInterstitialAdCode = "ca-app-pub-3940256099942544/1033173712";
            this.GoogleRewardedAdCode = "ca-app-pub-3940256099942544/5224354917";
        } else {

            this.FacebookBanner1 = "";
            this.FacebookBanner2 = "";
            this.FacebookBanner3 = "";

            this.FacebookInterstitialAdCode = "";

            this.FacebookRewardedAdCode = "";


            this.GoogleBanner1 = "";
            this.GoogleBanner2 = "";
            this.GoogleBanner3 = "";

            this.GoogleInterstitialAdCode = "";

            this.GoogleRewardedAdCode = "";
        }

        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }

                // Start loading ads here...
            }
        });
    }


    //Function Required to Get Current Date - Used to log Ad Activity into Shared Preferences
    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mmddyyyy");
        String date = dateFormat.format(new Date());
        return date;
    }

    //Banner Ads Integration

    //Function to Create a Google Banner and return it
    public com.google.android.gms.ads.AdView createGoogleBanner(String adCode, LinearLayout bannerBox) {
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        com.google.android.gms.ads.AdView adViewG = new com.google.android.gms.ads.AdView(context);
        adViewG.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        adViewG.setAdUnitId(adCode);
        bannerBox.addView(adViewG);
        return adViewG;
    }

    //Function to Show the Google Banner Ad Previously Created and replace it with Facebook Banner if Google Banner fails to load
    public void showGoogleBanner(com.google.android.gms.ads.AdView adViewG, com.facebook.ads.AdView fbBanner) {
        adViewG.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                adViewG.setVisibility(View.GONE);
                adViewG.destroy();
                if (fbBanner != null && canShowFacebookAds()) {
                    fbBanner.setVisibility(View.VISIBLE);
                    showFacebookBanner(fbBanner);
                }
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                if (fbBanner != null) {
                    adViewG.setVisibility(View.VISIBLE);
                    fbBanner.setVisibility(View.GONE);
                    fbBanner.destroy();
                }
                super.onAdLoaded();
            }

            @Override
            public void onAdClicked() {
                updateGoogleClicks();
                super.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                updateGoogleImpressions();
                super.onAdImpression();
            }
        });
        adViewG.loadAd(new AdRequest.Builder().build());
    }

    //Function to Show Google Banner, previously created
    public void showGoogleBanner(com.google.android.gms.ads.AdView adViewG) {
        showGoogleBanner(adViewG, null);
    }

    //Function to Create a Facebook Banner and return it
    public com.facebook.ads.AdView createFacebookBanner(String adCode, LinearLayout bannerBox) {
        com.facebook.ads.AdView adViewF = new AdView(context, adCode, AdSize.BANNER_HEIGHT_50);
        bannerBox.addView(adViewF);
        return adViewF;
    }

    //Function to Show the Facebook Banner Ad Previously Created and replace it with Google Banner if Facebook Banner fails to load
    public void showFacebookBanner(com.facebook.ads.AdView adV, com.google.android.gms.ads.AdView ReplacementGoogleBanner) {
        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {

                adV.setVisibility(View.GONE);
                adV.destroy();
                if (ReplacementGoogleBanner != null && canShowGoogleAds()) {
                    ReplacementGoogleBanner.setVisibility(View.VISIBLE);
                    showGoogleBanner(ReplacementGoogleBanner);
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                adV.setVisibility(View.VISIBLE);
                if (ReplacementGoogleBanner != null) {
                    ReplacementGoogleBanner.destroy();
                    ReplacementGoogleBanner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                updateFacebookClicks();
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                updateFacebookImpressions();
            }
        };
        adV.loadAd(adV.buildLoadAdConfig().withAdListener(adListener).build());
    }

    //Function to Show Facebook Banner, previously created
    public void showFacebookBanner(com.facebook.ads.AdView adV) {
        showFacebookBanner(adV, null);
    }

    //Function to show Banner Ads previously Created, with Preference given to Facebook and then Google
    public void showBannerAds(com.facebook.ads.AdView fBBanner, com.google.android.gms.ads.AdView googleBanner) {
        if (canShowFacebookAds())
            showFacebookBanner(fBBanner, googleBanner);
        else if (canShowGoogleAds())
            showGoogleBanner(googleBanner, fBBanner);
    }


    //Interstitial Ads Integration

    //Show Facebook Interstitial Ads
    public void showFacebookInterstitial(String adCode) {
        showFacebookInterstitial(adCode, "");
    }

    //Show Facebook Interstitial Ads and replace it with Google Interstitial id if Facebook cannot be loaded
    public void showFacebookInterstitial(String adCode, String googleCode) {

        // initializing InterstitialAd Object
        // InterstitialAd Constructor Takes 2 Arguments
        // 1)Context
        // 2)Placement Id

        InterstitialAd intad = new InterstitialAd(context, adCode);

        // loading Ad
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {

            @Override
            public void onInterstitialDisplayed(Ad ad) {

                // Showing Toast Message
//                Toast.makeText(MainActivity.this, "onInterstitialDisplayed", Toast.LENGTH_SHORT).show();

            }

            @Override

            public void onInterstitialDismissed(Ad ad) {
                // Showing Toast Message

            }

            @Override

            public void onError(Ad ad, AdError adError) {

                // Showing Toast Message

//                Toast.makeText(MainActivity.this, "onError", Toast.LENGTH_SHORT).show();
//                loadGoogleInterestitialAd();
                if (!googleCode.equals("") && canShowGoogleAds())
                    showGoogleInterstitial(googleCode);

            }

            @Override

            public void onAdLoaded(Ad ad) {

                // Showing Toast Message
                intad.show();

            }

            @Override

            public void onAdClicked(Ad ad) {

                // Showing Toast Message
                updateFacebookClicks();

            }

            @Override

            public void onLoggingImpression(Ad ad) {

                // Showing Toast Message
                updateFacebookImpressions();

            }

        };

        intad.loadAd(
                intad.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());
    }

    //Show Google Interstitial Ads
    public void showGoogleInterstitial(String googleAdCode) {
        showGoogleInterstitial(googleAdCode, "");
    }

    //Show Google Interstitial Ads and replace it with Facebook Interstitial id if Google cannot be loaded
    public void showGoogleInterstitial(String googleAdCode, String fbAdCode) {
        AdRequest adRequest = new AdRequest.Builder().build();
        com.google.android.gms.ads.interstitial.InterstitialAd.load(context, googleAdCode, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
//                super.onAdLoaded(interstitialAd);
                GoogleinterstitialAd = interstitialAd;
                GoogleinterstitialAd.show(activity);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                super.onAdFailedToLoad(loadAdError);
                GoogleinterstitialAd = null;
                if (!fbAdCode.equals("") && canShowFacebookAds())
                    showFacebookInterstitial(fbAdCode);
            }
        });
    }

    //Function to show Interstitial Ads, with Preference given to Facebook and then Google
    public void showInterstitialAds(String facebookAdCode, String googleAdCode) {
        if (canShowFacebookAds())
            showFacebookInterstitial(facebookAdCode, googleAdCode);
        else if (canShowGoogleAds())
            showGoogleInterstitial(googleAdCode, facebookAdCode);
    }

    //Function Overloading to show Interstitial Ads if the Ad Codes are mentioned within the AdsManager Class
    public void showInterstitialAds() {
        showInterstitialAds(this.FacebookInterstitialAdCode, this.GoogleInterstitialAdCode);
    }

    //Rewarded Ads

    //Show Google Rewarded Ads with a callback function executed when the user is Rewarded, and replace it with Facebook rewarded, if Google Fails to load
    public void showGooglerewarded(String adCode, String fbADCode, Callable<Void> onRewared) {
        isRewarded = false;
        RewardedAd.load(context, adCode, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                super.onAdLoaded(rewardedAd);
                mRewardedAd = rewardedAd;
                Log.d("adsTest", "Google Rewarded Ad was loaded.");

                if (mRewardedAd != null) {
                    mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d("adsTest", "Google Rewarded Ad was shown.");
                            updateGoogleImpressions();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
//                            Log.d(TAG, "Ad was dismissed.");
                            mRewardedAd = null;
                            if (isRewarded)
                            {
                                try {
                                    onRewared.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Handle the reward.
                            Log.d("adsTest", "The user earned the Google reward.");
//                            int rewardAmount = rewardItem.getAmount();
//                            String rewardType = rewardItem.getType();
                            isRewarded = true;

                        }
                    });
                } else {
                    Log.d("adsTest", "The Google rewarded ad wasn't ready yet.");
                    isRewarded = false;
                    if (!fbADCode.equals("") && canShowFacebookAds())
                        showFacebookRewarded(fbADCode, onRewared);
                }


            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mRewardedAd = null;
                isRewarded = false;
//                Log.d(TAG, loadAdError.getMessage());
                if (!fbADCode.equals("") && canShowFacebookAds())
                    showFacebookRewarded(fbADCode, onRewared);
            }
        });

//        return isRewarded;
    }

    //Show Google Rewared Ads, with a callback function executed when the user is Rewarded
    public void showGooglerewarded(String googleAdCode, Callable<Void> onRewared) {
        showGooglerewarded(googleAdCode, "", onRewared);
    }

    //Show Facebook Rewared Ads, with a callback function executed when the user is Rewarded
    public void showFacebookRewarded(String adCode, Callable<Void> onRewared) {
        showFacebookRewarded(adCode, "", onRewared);
    }

    //Show Facebook Rewarded Ads with a callback function executed when the user is Rewarded, and replace it with Google rewarded, if Facebook Fails to load
    public void showFacebookRewarded(String adCode, String googleAdCode, Callable<Void> onRewared) {
        isRewarded = false;
        RewardedVideoAd rewardedVideoAd = new RewardedVideoAd(context, adCode);
        RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
            @Override
            public void onError(Ad ad, com.facebook.ads.AdError adError) {
                Log.d("adsTest", "Fb Rewarded Failed");
                if (!googleAdCode.equals("") && canShowGoogleAds())
                    showGooglerewarded(googleAdCode, onRewared);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Rewarded video ad is loaded and ready to be displayed
//                Log.d(TAG, "Rewarded video ad is loaded and ready to be displayed!");
                Log.d("adsTest", "Fb Rewarded Loaded");
                rewardedVideoAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Rewarded video ad clicked
//                Log.d(TAG, "Rewarded video ad clicked!");
                isRewarded = true;
                updateFacebookClicks();
                Log.d("adsTest", "Fb Rewarded Clicked");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Rewarded Video ad impression - the event will fire when the
                // video starts playing
                Log.d("adsTest", "Fb Rewarded video ad impression logged!");
                updateFacebookImpressions();
            }

            @Override
            public void onRewardedVideoCompleted() {
                // Rewarded Video View Complete - the video has been played to the end.
                // You can use this event to initialize your reward
                Log.d("adsTest", "Rewarded video completed!");
                isRewarded = true;

                // Call method to give reward
                // giveReward();
            }

            @Override
            public void onRewardedVideoClosed() {
                // The Rewarded Video ad was closed - this can occur during the video
                // by closing the app, or closing the end card.
//                Log.d(TAG, "Rewarded video ad closed!");
                if (isRewarded) {
                    try {
                        onRewared.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "Please Watch Video to get Reward", Toast.LENGTH_SHORT).show();
                }


            }
        };

        rewardedVideoAd.loadAd(
                rewardedVideoAd.buildLoadAdConfig()
                        .withAdListener(rewardedVideoAdListener)
                        .build());

    }

    //Show Rewarded Ads with a callback function executed when the user is Rewarded. The Preference given to Facebook and then Google.
    public void showRewardedAds(String fbAdCode, String GoogleAdCode, Callable<Void> onRewared) {
        isRewarded = false;
        if (canShowFacebookAds())
            showFacebookRewarded(fbAdCode, GoogleAdCode, onRewared);
        else if (canShowGoogleAds())
            showGooglerewarded(GoogleAdCode, fbAdCode, onRewared);

//        return false;
    }

    //Function Overloading to show Rewarded ads with a callback function executed when the user is Rewarded and if the AD Codes are present within the Ads Manager Class
    public void showRewardedAds(Callable<Void> onRewared) {
        showRewardedAds(this.FacebookRewardedAdCode, this.GoogleRewardedAdCode, onRewared);
    }

    //Function Overloading to show rewarded Ads, if the AD Codes are present within the Ads Manager Class and do nothing when rewarded
    public void showRewardedAds() {
        showRewardedAds(this.FacebookRewardedAdCode, this.GoogleRewardedAdCode, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        });
    }

    //Returns True if showing Facebook Ads are Safe. Otherwise returns false.
    public boolean canShowFacebookAds() {
        int clicks = sp.getInt("fbclicks_" + getDate(), 0);
        int impressions = sp.getInt("fbimp_" + getDate(), 0);

        if (clicks + impressions <= 0)
            return true;

        if (clicks >= this.maxFBAdClicksPerDay)
            return false;

//        int calcCTR = (int) Math.ceil((clicks * 100) / impressions);
//
//        if (calcCTR >= this.maxCTRPerDay && impressions > 3)
//            return false;

        return true;
    }

    //Returns True if showing Google Ads are Safe. Otherwise returns false.
    public boolean canShowGoogleAds() {
        int clicks = sp.getInt("googleclicks_" + getDate(), 0);
        int impressions = sp.getInt("googleimp_" + getDate(), 0);

        if (clicks + impressions <= 0)
            return true;

        if (clicks >= this.maxGoogleAdClicksPerDay)
            return false;

//        int calcCTR = (int) Math.ceil((clicks * 100) / impressions);
//
//        if (calcCTR >= this.maxCTRPerDay && impressions > 3)
//            return false;

        return true;
    }

    //Increments Facebook Ad Clicks by 1.
    private void updateFacebookClicks() {
        int clicks = sp.getInt("fbclicks_" + getDate(), 0) + 1;
        ed.putInt("fbclicks_" + getDate(), clicks);
        ed.commit();
    }

    //Increments Facebook Ad Impressions by 1.
    private void updateFacebookImpressions() {
        int clicks = sp.getInt("fbimp_" + getDate(), 0) + 1;
        ed.putInt("fbimp_" + getDate(), clicks);
        ed.commit();
    }

    //Increments Google Ad Clicks by 1.
    private void updateGoogleClicks() {
        int clicks = sp.getInt("googleclicks_" + getDate(), 0) + 1;
        ed.putInt("googleclicks_" + getDate(), clicks);
        ed.commit();
    }

    //Increments Google Ad Impressions by 1.
    private void updateGoogleImpressions() {
        int clicks = sp.getInt("googleimp_" + getDate(), 0) + 1;
        ed.putInt("googleimp_" + getDate(), clicks);
        ed.commit();
    }


    //Set-Up Onesignal Notification Integration for the App
    public void setupOnegignalIntegration(String oneSignalAppID) {
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(context);
        OneSignal.setAppId(oneSignalAppID);
    }

    //Function Overloading to set-up Onesignal Notification Integration if App id is defined within the adsManagerClass
    public void setupOnegignalIntegration() {
        setupOnegignalIntegration(this.ONESIGNAL_APP_ID);
    }

}
