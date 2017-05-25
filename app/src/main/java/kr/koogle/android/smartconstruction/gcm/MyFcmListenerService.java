package kr.koogle.android.smartconstruction.gcm;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import kr.koogle.android.smartconstruction.IntroActivity;
import kr.koogle.android.smartconstruction.MainActivity;
import kr.koogle.android.smartconstruction.PopupActivity;
import kr.koogle.android.smartconstruction.R;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MyFcmListenerService extends FirebaseMessagingService {
    /**
     * Foreground에서 메세지를 수신했을 때의 Callback입니다.
     * @param message 레퍼런스(https://developers.google.com/android/reference/com/google/firebase/messaging/RemoteMessage)를 참조하세요.
     */

    @Override
    public void onMessageReceived(RemoteMessage message){
        String from = message.getFrom();
        Map<String, String> data = message.getData();
        String title = data.get("title");
        String msg = data.get("message");

        // Non-blocking methods. No need to use AsyncTask or background thread.
        //FirebaseMessaging.getInstance().subscribeToTopic("mytopic");
        //FirebaseMessaging.getInstance().unsubscribeToTopic("mytopic");

        if ( getRunningProcess(getBaseContext()).equals("kr.koogle.android.smartconstruction") && false ) // 앱이 실행중이면..
        {

        }
        else
        {
            // 큰 아이콘
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ico_noti);

            // 알림 사운드
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // 알림 클릭시 이동할 인텐트
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.google.com/cloud-messaging/"));
            Intent intent = new Intent(this, IntroActivity.class);

            // 노티피케이션을 생성할때 매개변수는 PendingIntent 이므로 Intent를 PendingIntent 로 만들어 주어야함.
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            // 노티피케이션 빌더 : 위에서 생성한 이미지나 텍스트, 사운드 등을 설정해 줍니다.
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ico_noti_small)
                    .setTicker(title)
                    .setLargeIcon(bitmap)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setNumber(1)
                    .setSound(soundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // 노티피케이션을 생성합니다.
            notificationManager.notify(0 /* ID of notification 알림 지울때 사용 */, notificationBuilder.build());

            // 삼성런처에서만 가능한 벳지 카운트
            // ShortcutBadger.applyCount(this, 1);
            Intent intentBadger = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            // 패키지 네임과 클래스 네임 설정
            intentBadger.putExtra("badge_count_package_name", getApplicationContext().getPackageName());
            intentBadger.putExtra("badge_count_class_name", "kr.koogle.android.smartconstruction.IntroActivity");
            // 업데이트 카운트
            intentBadger.putExtra("badge_count", 1);
            sendBroadcast(intentBadger);



            // 팝업 호출시 (푸시 리시버에서 호출)
            Intent intentPopup = new Intent(this, PopupActivity.class);
            intentPopup.putExtra("title", title);
            intentPopup.putExtra("content", msg);
            intentPopup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);   // 이거 안해주면 안됨
            this.startActivity(intentPopup);

        }
    }

    public static String getRunningProcess(Context context) {

        String strPackage = "";

        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> process = am.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo proc : process)
        {
            if(proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                strPackage = proc.processName;
            }
        }
        return strPackage;
        /*
        String packageName = "";
        if (Build.VERSION.SDK_INT > 20)
        {
            packageName = am.getRunningAppProcesses().get(0).processName;
        }
        else
        {
            packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
        return packageName;
        */
    }

    // 실행중인 앱인지 체크하기
    public static boolean isRunningProcess(Context context, String packageName) {

        boolean isRunning = false;
        ActivityManager actMng = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = actMng.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo rap : list)
        {
            if (rap.processName.equals(packageName))
            {
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }

    // 뱃지 카운트 하기
    public void updateIconBadgeCount(Context context, int count) {

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");

        // Component를 정의
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", getLauncherClassName(context));

        // 카운트를 넣어준다.
        intent.putExtra("badge_count", count);

        // Version이 3.1이상일 경우에는 Flags를 설정하여 준다.
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            intent.setFlags(0x00000020);
        }

        // send
        sendBroadcast(intent);
    }
    private String getLauncherClassName(Context context) {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(getPackageName());

        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, 0);
        if(resolveInfoList != null && resolveInfoList.size() > 0) {
            return resolveInfoList.get(0).activityInfo.name;
        }

        return "";
    }

}
