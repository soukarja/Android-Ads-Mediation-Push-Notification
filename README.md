
# Android Ads Mediation & Push Notification

Onesignal Push Notifications and Ads Mediation for Facebook Audience Network and Google Admob for Android in Java.

- Supports Banner Ads
- Supports Interstitial Ads
- Supports Rewarded Ads
- Supports Push Notifications with Onesignal

## Installation

#### Required Changes in android > defaultconfig in ``build.gradle(:app)``

```bash
multiDexEnabled true
```

#### Required dependencies in ``build.gradle(:app)``

```bash
implementation 'com.onesignal:OneSignal:4.4.0'
implementation 'com.google.android.gms:play-services-ads:20.2.0'
implementation 'com.google.ads.mediation:facebook:6.5.0.0'
```
#### Facebook network_security_config file for Ads Cache
Create a New XML Resource in xml > values > ``network_security_config.xml``
```bash
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    ...
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">127.0.0.1</domain>
    </domain-config>
    ...
</network-security-config>
```

#### Required changes in AndroidManifest.xml

Add Permissions 
```
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

Include Metadata for Admob Integration
```
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713"/>
```

Include Reference to the Facebook network_security_config.xml file
```
android:networkSecurityConfig="@xml/network_security_config"
```


  
### Sample AndroidManifest.xml File

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.sdadsManager">

<!--    Ad Permissions-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />

<!--    reference to network_security_config.xml and Admob App ID-->
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AdsManager"
        android:networkSecurityConfig="@xml/network_security_config">
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713"/>
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

  
## Usage

#### Initialize AdsManager
`SYNTAX: new adsManager (activity, testMode)`
```
adsManager ads = new adsManager(MainActivity.this, true);
```

#### Integrate Onesignal

```
ads.setupOnegignalIntegration("ONESIGNAL_ID");
```

### Banner Ads
#### Create LinearLayout in XML File
```
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:id="@+id/BannerBox"
    android:orientation="vertical"/>
```

#### Variable Declaraitions
```
private LinearLayout bannerBox;
private com.facebook.ads.AdView faceBookBanner;
private com.google.android.gms.ads.AdView googleBanner;
```
#### Create & Show Banner Ads
```
bannerBox = (LinearLayout) findViewById(R.id.BannerBox);
faceBookBanner = ads.createFacebookBanner("AD_Code", bannerBox);
googleBanner = ads.createGoogleBanner("AD_code", bannerBox);
ads.showBannerAds(faceBookBanner, googleBanner);

// ads.showFacebookBanner(faceBookBanner); //Show only Facebook Banner
// ads.showGoogleBanner(googleBanner); //Show Only Google Banner
```
##### One Liner
```
ads.showBannerAds(ads.createFacebookBanner("AD_Code", (LinearLayout) findViewById(R.id.BannerBox)), ads.createGoogleBanner("AD_code", (LinearLayout) findViewById(R.id.BannerBox)));
```

### Interstitial Ads
```
ads.showInterstitialAds(ads.FacebookInterstitialAdCode, ads.GoogleInterstitialAdCode);

// ads.showGoogleInterstitial(ads.GoogleInterstitialAdCode); //Show Only Google Interstitial Ad
// ads.showFacebookInterstitial(ads.FacebookInterstitialAdCode); //Show only Facebook Interstitial Ad
```

### Rewarded Ads
```
ads.showRewardedAds(ads.FacebookRewardedAdCode, ads.GoogleRewardedAdCode, new Callable<Void>() {
    @Override
    public Void call() throws Exception {
        Toast.makeText(getApplicationContext(), "The User gets a Reward!", Toast.LENGTH_SHORT).show();
        return null;
    }
});
```
##### Show only Facebook rewarded Ad
```
ads.showFacebookRewarded(ads.FacebookRewardedAdCode, new Callable<Void>() {
    @Override
    public Void call() throws Exception {
        Toast.makeText(getApplicationContext(), "The User gets a Reward!", Toast.LENGTH_SHORT).show();
        return null;
    }
});
```
##### Show only Google rewarded Ad
```
ads.showGooglerewarded(ads.GoogleRewardedAdCode, new Callable<Void>() {
    @Override
    public Void call() throws Exception {
        Toast.makeText(getApplicationContext(), "The User gets a Reward!", Toast.LENGTH_SHORT).show();
        return null;
    }
});
```
  
## Quick Links

 - [Click Here to Download `adsManager.java`](https://raw.githubusercontent.com/soukarja/Android-Ads-Mediation-Push-Notification/main/adsManager.java)
 - [FaceBook Audience Network](https://business.facebook.com/pub/home)
 - [Google Admob](https://apps.admob.com/v2/home)

  
