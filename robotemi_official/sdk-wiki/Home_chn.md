Temi SDK是你通过机器人独特能力开发temi技能的途径！通过创建Android应用并接入temi的SDK，你可以在Android上为temi完成所有的开发工作。temi V2 平板的操作系统版本为 Android 6.0.1（Android SDK 级别为 23）。temi V3 平板的操作系统版本为 Android 11 (Android SDK 级别为 30)。请始终将我们提供的示例应用作为开发过程中的参考。

### 接入 temi SDK

#### AndroidManifest.xml

temi OS与你的应用程序之间的所有通信都是通过 **AndroidManifest.xml** 中的 `meta-data` 进行链接的。下面你将看到两个 `meta-data` ，第一个是在temi OS的应用程序列表中显示你的应用程序，第二个是注册要监听的NLP意图（`debug.test` 这个意图可以用语料“开始调试机器人”、“开始测试机器人”进行测试）：

``` xml
<meta-data
    android:name="com.robotemi.sdk.metadata.SKILL"
    android:value="@string/app_name" />

<meta-data
    android:name="com.robotemi.sdk.metadata.ACTIONS"
    android:value=
         "debug.test,
         xxx.xxx" />
```

#### MainActivity.java

在MainActivity中你可以根据你感兴趣的状态事件回调去实现特定的接口。例如：

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

实现这些接口之后，Android Studio会提示你要重写这些接口的方法。

当然你还需要在Activity的生命周期中添加或移除这些监听接口：

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

实现 `OnRobotReadyListener` 接口并重写 `onRobotReady(boolean isReady)` 方法并再方法中添加如下代码以确保你的应用图标在temi的顶部导航栏 **Top Bar** 上展示，并且可以作为一个快捷方式进入你的应用。例如：

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

#### NLP 相关

如果需要通过语音“叮当叮当，xxxx”让你创建的项目听懂并作出相应的动作，那么你需要在NLP后台配置好与 `action` 映射的具体字段，然后在 **AndroidManifest.xml** 中添加相应的 `action` 。例如在NLP后台配好 `debug.test` **之后**，在 **AndroidManifest.xml** 中添加以下标签：

``` xml
<application >
    ···
    <meta-data
        android:name="@string/metadata_actions"
        android:value="debug.test" />
    ···
</application>
```

注：如果有多个 `action` ,可以直接在 `debug.test` 后面追加，用英文逗号“,”分隔，如：

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

然后，你还需要在Java代码（例如在 **MainActivity.java** ）中注册 `Robot.NlpListener` 监听事件，并在回调方法 `onNlpCompleted(NlpResult nlpResult)` 中对 `NlpResult` 对象做相应的处理，例如：

``` java
public class MainActivity extends AppCompatActivity implements OnRobotReadyListener, Robot.NlpListener {

    private Robot robot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        robot = Robot.getInstance();
        handleIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        NlpResult nlpResult = getIntent().getParcelableExtra(EXTRA_NLP_RESULT);
        if (nlpResult != null) {
            onNlpCompleted(nlpResult);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册监听事件
        robot.addOnRobotReadyListener(this);
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
            case "debug.test":
                // 实现做debug.test对应的动作的代码，如：
                handleResponse(nlpResult);
                break;
            default:
                // ...
                break;
        }
    }
 
    /**
    * 按要求执行具体指令
    *
    * @param response NLP响应信息实体
    *
    */
    private void handleResponse(NlpResult nlpResult) {
        // 解析nlpResult，执行具体指令..
       // nlpResult.params.get("槽位标识")
    }

      @Override
      protected void onPause() {
          // 取消监听
         robot.removeOnRobotReadyListener(this);
         robot.removeNlpListener(this);
         super.onPause();
    }
 }
```

`debug.test` 这个 `action` 用于测试 `NLP` 的整个流程，可以通过语料“开始调试xxx”、“开始测试xxx”进行测试，“xxx”为语料的槽位，可以为“机器人”等词语。
    
 **注意：** 中文版 temi 中，从云端配置的意图 `action` 中的大写字母均会被转换成小写，“_”会转换成“.”。