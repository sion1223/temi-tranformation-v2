Temi SDK is your way to develop skills for temi that take advantage of your robot's unique abilities! All development for temi is done on Android by creating an android application and importing Temi's SDK. temi V2's tablet runs on Android 6.0.1 (SDK level 23), and temi V3's tablet is running on Android 11 (SDK level 30). Always use the Sample app as a reference.

### Config in Your Application

#### AndroidManifest.xml

All communication betweeen temi OS and your application is linked through the meta-datas included in the AndroidManifest.xml. Below you will see the meta-data that displays your app in temi OS's application selection:

``` xml
<meta-data
    android:name="com.robotemi.sdk.metadata.SKILL"
    android:value="@string/app_name" />
```

#### MainActivity.java

At the start of the MainActivity, you might want to implement specific interfaces based on which types of callbacks you are interested in. For example,

``` java
public class MainActivity extends AppCompatActivity implements
    Robot.NlpListener,
    OnRobotReadyListener,
    Robot.ConversationViewAttachesListener,
    Robot.WakeupWordListener,
    Robot.ActivityStreamPublishListener,
    Robot.TtsListener,
    OnBeWithMeStatusChangedListener,
    OnGoToLocationStatusChangedListener,
    OnLocationsUpdatedListener
```

After doing so, Android Studio will prompt you to implement specific methods.

You will also need to add and remove each listener by doing:

``` java
protected void onStart() {
    super.onStart();
    Robot.getInstance().addOnRobotReadyListener(this);
    Robot.getInstance().addNlpListener(this);
    Robot.getInstance().addOnBeWithMeStatusChangedListener(this);
    Robot.getInstance().addOnGoToLocationStatusChangedListener(this);
    Robot.getInstance().addConversationViewAttachesListenerListener(this);
    Robot.getInstance().addWakeupWordListener(this);
    Robot.getInstance().addTtsListener(this);
    Robot.getInstance().addOnLocationsUpdatedListener(this);
}

protected void onStop() {
    super.onStop();
    Robot.getInstance().removeOnRobotReadyListener(this);
    Robot.getInstance().removeNlpListener(this);
    Robot.getInstance().removeOnBeWithMeStatusChangedListener(this);
    Robot.getInstance().removeOnGoToLocationStatusChangedListener(this);
    Robot.getInstance().removeConversationViewAttachesListenerListener(this);
    Robot.getInstance().removeWakeupWordListener(this);
    Robot.getInstance().removeTtsListener(this);
    Robot.getInstance().removeOnLocationsUpdateListener(this);
}
```

Overriding the onRobotReady method allows your app to be placed as a shortcut in temi's top bar. For example:

``` java
@Override
public void onRobotReady(boolean isReady) {
    if (isReady) {
        try {
            final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            robot.onStart(activityInfo);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
```

<!-- #### NLP Related

If you have the permission to access Dialog flow for temi, you can configure the intents for your App as well. Add the actions (configured on Dialog flow) to **AndroidManifest.xml** as following:

``` xml
<application >
    ···
    <meta-data
        android:name="@string/metadata_actions"
        android:value="debug.test" />
    ···
</application>
```

Noted, use "," to split the actions:

``` xml
<application >
    ···
    <meta-data
        android:name="@string/metadata_actions"
        android:value="
            debug.test,
            shop.guide" />
    ···
</application>
```

After that, listen to the NLP result by `NlpListener` and process the `action` from callback function `onNlpCompleted(NlpResult nlpResult)`. Noted that temi will pass the NLP action to your **MainActivity** by Android intent as well, so you can get the action by `getIntent().getParcelableExtra(EXTRA_NLP_RESULT)` before you listen to the NlpListener.

``` java
public class MainActivity extends AppCompatActivity implements OnRobotReadyListener, Robot.NlpListener {

    private Robot robot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        robot = Robot.getInstance();
        // Get the NLP action before you listen to the NlpListener
        handleIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Get the NLP action before you listen to the NlpListener
        handleIntent();
    }

    /**
     * Handle the NLP actions by Android intent
    */
    private void handleIntent() {
        NlpResult nlpResult = getIntent().getParcelableExtra(EXTRA_NLP_RESULT);
        if (nlpResult != null) {
            onNlpCompleted(nlpResult);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        robot.addOnRobotReadyListener(this);
        // Listen to the NlpListener for actions
        robot.addNlpListener(this);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        try {
            final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            robot.onStart(activityInfo);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onNlpCompleted(NlpResult nlpResult) {
        switch (nlpResult.action) {
            case "shop.guide":
                shopGuide(nlpResult);
                break;
            default:
                // ...
                break;
        }
    }
 
    private void shopGuide(NlpResult nlpResult) {
        // Parse the NLP result
        // nlpResult.params.get("slot")
    }

      @Override
      protected void onPause() {
         robot.removeOnRobotReadyListener(this);
         robot.removeNlpListener(this);
         super.onPause();
    }
 }
``` -->
