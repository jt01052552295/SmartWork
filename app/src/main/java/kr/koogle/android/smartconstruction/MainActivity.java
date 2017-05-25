package kr.koogle.android.smartconstruction;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.annotation.NonNull;
import android.content.ClipData;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.NavigationView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;

import android.net.Uri;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.commonsware.cwac.security.RuntimePermissionUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.*;
import kr.koogle.android.smartconstruction.util.BackPressCloseHandler;
import kr.koogle.android.smartconstruction.util.HtmlRemoteImageGetterLee;
import kr.koogle.android.smartconstruction.util.MarketVersionChecker;
import kr.koogle.android.smartconstruction.util.RbPreference;
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Main";
    private static BackPressCloseHandler backPressCloseHandler;
    private static RbPreference pref;

    // 퍼미션 체크 요청 3-1
    private static final int RESULT_PERMS_ALL = 1341;
    private static final String[] PERMS_ALL = {
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };
    private RuntimePermissionUtils utils;

    public static FloatingActionButton fab;

    public static Fragment tempFragmentBuild;
    public static Fragment tempFragmentClient;
    public static Fragment tempFragmentOrder;
    public static Fragment tempFragmentApproval;

    private static ImageView navImage;
    private static TextView navName;
    private static TextView navEmail;
    private static ImageView btnNavSettings;

    // 탭메뉴 적용
    private final ArrayList<String> appMenu = new ArrayList<String>();
    private final ArrayList<String> appMenu_chk = new ArrayList<String>();
    private final int tabSize = 0;


    // viewPager 관련
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private int pageNumber;

    // UTILITY METHODS
    private Toast mToast;
    private Thread mThread;
    private Handler mHandler;

    String deviceVersion;
    String storeVersion;
    private BackgroundThread mBackgroundThread;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        // 퍼미션 체크 요청 3-2
        utils = new RuntimePermissionUtils(this);
        // 퍼미션 채크해서 퍼미션 요청 !!!
        if (!haveNecessaryPermissions() && utils.useRuntimePermissions()) {
            requestPermissions(PERMS_ALL, RESULT_PERMS_ALL);
        }
        else {
            // handlePage();
        }

        // 업데이트 버전 체크
        mBackgroundThread = new BackgroundThread();
        mBackgroundThread.start();



        // ToolBar 관련
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 그룹웨어 앱표시 체크된 메뉴만 탭표시
        if(pref.getValue("pref_app_menu1_chk","").equals("1")){
            appMenu_chk.add((!pref.getValue("pref_app_menu1_chk","").equals(""))?pref.getValue("pref_app_menu1_chk",""):"1");
            appMenu.add((!pref.getValue("pref_app_menu1","").equals(""))?pref.getValue("pref_app_menu1",""):"스마트현장");
        }
        if(pref.getValue("pref_app_menu2_chk","").equals("1")){
            appMenu_chk.add((!pref.getValue("pref_app_menu2_chk","").equals(""))?pref.getValue("pref_app_menu2_chk",""):"1");
            appMenu.add((!pref.getValue("pref_app_menu2","").equals(""))?pref.getValue("pref_app_menu2",""):"건축주협의");
        }
        if(pref.getValue("pref_app_menu3_chk","").equals("1")){
            appMenu_chk.add((!pref.getValue("pref_app_menu3_chk","").equals(""))?pref.getValue("pref_app_menu3_chk",""):"1");
            appMenu.add((!pref.getValue("pref_app_menu3","").equals(""))?pref.getValue("pref_app_menu3",""):"작업지시");
        }
        if(pref.getValue("pref_app_menu4_chk","").equals("1")){
            appMenu_chk.add((!pref.getValue("pref_app_menu4_chk","").equals(""))?pref.getValue("pref_app_menu4_chk",""):"1");
            appMenu.add((!pref.getValue("pref_app_menu4","").equals(""))?pref.getValue("pref_app_menu4",""):"전자결재");
        }


        // viewPager 관련
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // ViewPager 에 TabLayout 연결
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // 네비게이션 이벤트
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset == 0) {
                    // drawer closed
                    invalidateOptionsMenu();
                } else if (slideOffset != 0) {
                    // started opening
                    final TextView navName = (TextView) findViewById(R.id.nav_name);
                    final TextView navEmail = (TextView) findViewById(R.id.nav_email);
                    navName.setText(pref.getValue("pref_user_name", ""));
                    navEmail.setText(pref.getValue("pref_user_email", ""));

                    invalidateOptionsMenu();
                }
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // 네비게이션 연동
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 건축주이면 메뉴 숨기기
        if(pref.getValue("pref_user_type","").equals("client")) {
            final Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_smart_build).setVisible(false);
            menu.findItem(R.id.nav_smart_order).setVisible(false);
            menu.findItem(R.id.nav_manage_employee).setVisible(false);
        }

        // Settings 버튼 클릭 이벤트
        View navHeaderView = navigationView.getHeaderView(0);
        btnNavSettings = (ImageView) navHeaderView.findViewById(R.id.btn_nav_settings);
        btnNavSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btnNavSettings.setEnabled(true);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    backPressCloseHandler.onBackPressed();
                    //super.onBackPressed();
                }
            }
        });

        // Floating Action Button 관련
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.img_camera_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(MainActivity.this)
                        .title("촬영선택")
                        .items(new String[]{"사진 촬영", "동영상 촬영"})
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                // showToast(which + ": " + text);
                                switch(which) {
                                    case 0: // 사진 촬영
                                        Intent intent = new Intent(MainActivity.this, CameraPicActivity.class);
                                        intent.putExtra("intId", 0);
                                        startActivity(intent);
                                        break;

                                    case 1: // 동영상 촬영
                                        intent = new Intent(MainActivity.this, CameraMovActivity.class);
                                        intent.putExtra("intId", 0);
                                        startActivity(intent);
                                        break;
                                }
                            }
                        })
                        .show();

                // Snackbar.make(view, "사진촬영을 시작합니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        // 닫힐때 한번 더 확인
        backPressCloseHandler = new BackPressCloseHandler(this);

        /******************************************************************************************/
        if(SmartSingleton.arrLaborCategorys.isEmpty()) {
            // Labor Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getLaborCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseLaborCategorys = response.body();

                        if (responseLaborCategorys.size() != 0) {
                            Log.d(TAG, "arrLaborCategorys : size " + responseLaborCategorys.size());
                            SmartSingleton.arrLaborCategorys.addAll(responseLaborCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseLaborCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrMaterialCategorys.isEmpty()) {
            // Material Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getMaterialCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseMaterialCategorys = response.body();

                        if (responseMaterialCategorys.size() != 0) {
                            Log.d(TAG, "arrMaterialCategorys : size " + responseMaterialCategorys.size());
                            SmartSingleton.arrMaterialCategorys.addAll(responseMaterialCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseMaterialCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    //Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrEquipmentCategorys.isEmpty()) {
            // Equipment Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getEquipmentCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseEquipmentCategorys = response.body();

                        if (responseEquipmentCategorys.size() != 0) {
                            Log.d(TAG, "arrEquipmentCategorys : size " + responseEquipmentCategorys.size());
                            SmartSingleton.arrEquipmentCategorys.addAll(responseEquipmentCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseEquipmentCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrUnitCategorys.isEmpty()) {
            // Unit Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getUnitCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseUnitCategorys = response.body();

                        if (responseUnitCategorys.size() != 0) {
                            Log.d(TAG, "arrUnitCategorys : size " + responseUnitCategorys.size());
                            SmartSingleton.arrUnitCategorys.addAll(responseUnitCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseUnitCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrWeatherCategorys.isEmpty()) {
            // Weather Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartCategory>> call = smartService.getWeatherCategorys();

            call.enqueue(new Callback<ArrayList<SmartCategory>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartCategory>> call, Response<ArrayList<SmartCategory>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartCategory> responseWeatherCategorys = response.body();

                        if (responseWeatherCategorys.size() != 0) {
                            Log.d(TAG, "arrWeatherCategorys : size " + responseWeatherCategorys.size());
                            SmartSingleton.arrWeatherCategorys.addAll(responseWeatherCategorys);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseWeatherCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartCategory>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/
        if(SmartSingleton.arrSmartEmployees.isEmpty()) {
            // Equipment Category 값 불러오기 (한번만!!)
            SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
            Call<ArrayList<SmartEmployee>> call = smartService.getSmartEmployees();

            call.enqueue(new Callback<ArrayList<SmartEmployee>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartEmployee>> call, Response<ArrayList<SmartEmployee>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartEmployee> responses = response.body();

                        if (responses.size() != 0) {
                            Log.d(TAG, "arrSmartEmployees : size " + responses.size());
                            SmartSingleton.arrSmartEmployees.addAll(responses);
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "responseEquipmentCategorys : 데이터가 정확하지 않습니다.");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<SmartEmployee>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "네트워크 상태가 좋지 않습니다.!", Toast.LENGTH_SHORT).show();
                    Log.d("Error", t.getMessage());
                }
            });
        }
        /******************************************************************************************/

    }

    // 퍼미션 체크 요청 3-2
    private boolean haveNecessaryPermissions() {
        return(utils.hasPermission(CAMERA) &&
                utils.hasPermission(RECORD_AUDIO) &&
                utils.hasPermission(WRITE_EXTERNAL_STORAGE));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (haveNecessaryPermissions()) {
            //handlePage();
        }
        else {
            finish();
        }
    }

    //  ############## Fragment 통신 ##################  // SmartFragment 용
    /*
    public void onArticleSelected(int position) {
        SmartFragment smartFragment = (SmartFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_smart);
        if(smartFragment != null) {
            // Framgment 통신 사용 !!! -> OneFragment
            smartFragment.updateArticleView(position);
        } else {
            OneFragment newFragment = new OneFragment();
            Bundle args = new Bundle();
            args.putString("strId", "leftlife");
            newFragment.setArguments(args); // Fragment 생성시 데이타 넘길때 꼭 이렇게 !!

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
    */

    /*
     * 엑티비티 생명주기 관련 함수 ************************************************************************
     */
    @Override
    protected void onResume() {
        super.onResume();
        // the intent filter defined in AndroidManifest will handle the return from ACTION_VIEW intent
        String redirectUri = "";
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(redirectUri)) {
            // use the parameter your API exposes for the code (mostly it's "code")
            String code = "code"; //uri.getQueryParameter("code");
            if (code != null) {

            } else if (uri.getQueryParameter("error") != null) {
                // show an error message here
            }
        }
    }

    @Override
    protected  void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        SmartSingleton.getInstance().arrSmartBuilds.clear();
        SmartSingleton.getInstance().arrSmartWorks.clear();
        SmartSingleton.getInstance().arrSmartClients.clear();
        SmartSingleton.getInstance().arrSmartApprovals.clear();
        SmartSingleton.getInstance().arrSmartOrders.clear();
        SmartSingleton.getInstance().arrSmartPhotos.clear();
        SmartSingleton.getInstance().arrSmartEmployees.clear();

        SmartSingleton.getInstance().arrLaborCategorys.clear();
        SmartSingleton.getInstance().arrMaterialCategorys.clear();
        SmartSingleton.getInstance().arrEquipmentCategorys.clear();
        SmartSingleton.getInstance().arrUnitCategorys.clear();
        SmartSingleton.getInstance().arrWeatherCategorys.clear();

        SmartSingleton.getInstance().arrComments.clear();
        SmartSingleton.getInstance().arrApprovalComments.clear();
        SmartSingleton.getInstance().arrApprovalDetails.clear();

        super.onDestroy();
    }

    // Drawer Layout 관련
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            backPressCloseHandler.onBackPressed();
            //super.onBackPressed();
        }
    }

    /*
     * App Bar 메뉴 관련 함수 ************************************************************************
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Log.d(TAG, "getCurrentItem : " + String.valueOf(mViewPager.getCurrentItem()));
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            switch(mViewPager.getCurrentItem()) {

                case 0: // 스마트 현장

                    // 쓰기 권한 체크
                    if( !pref.getValue("pref_user_type","").equals("employee") ) {
                        new MaterialDialog.Builder(this).content("현장소장만 등록이 가능합니다.")
                                .positiveText("확인")
                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    }
                                }).show();
                        return true;
                    }
                    Intent intent = new Intent(MainActivity.this, SmartWorkViewActivity.class);
                    intent.putExtra("strCode", "");
                    startActivityForResult(intent, 10001);
                    break;

                case 1: // 건축주 협의

                    intent = new Intent(MainActivity.this, SmartClientWriteActivity.class);
                    intent.putExtra("intId", 0);
                    startActivityForResult(intent, 20001);
                    break;

                case 2: // 작업 지시

                    intent = new Intent(MainActivity.this, SmartOrderWriteActivity.class);
                    intent.putExtra("intId", 0);
                    startActivityForResult(intent, 30001);
                    break;

                default:
                    break;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Navigation 메뉴 관련 함수 ************************************************************************
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_smart_build) {

            // 쓰기 권한 체크
            if( !pref.getValue("pref_user_type","").equals("employee") ) {
                new MaterialDialog.Builder(this).content("현장소장만 등록이 가능합니다.")
                        .positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        }).show();
                return true;
            }
            Intent intent = new Intent(MainActivity.this, SmartWorkViewActivity.class);
            intent.putExtra("strCode", "");
            startActivityForResult(intent, 10001);

        } else if (id == R.id.nav_smart_client) {
            Intent intent = new Intent(MainActivity.this, SmartClientWriteActivity.class);
            intent.putExtra("intId", 0);
            startActivityForResult(intent, 20001);

        } else if (id == R.id.nav_smart_order) {
            Intent intent = new Intent(MainActivity.this, SmartOrderWriteActivity.class);
            intent.putExtra("intId", 0);
            startActivityForResult(intent, 30001);

        } else if (id == R.id.nav_manage_photo) {
            Intent intent = new Intent(MainActivity.this, CameraPicListActivity.class);
            intent.putExtra("intId", SmartSingleton.smartClient.intId);
            startActivityForResult(intent, 40001);

        } else if (id == R.id.nav_manage_employee) {
            Intent intent = new Intent(MainActivity.this, SmartEmployeeActivity.class);
            intent.putExtra("intId", "");
            startActivityForResult(intent, 50001);

        } else if (id == R.id.nav_logout) {
            // Settings 값 !!
            RbPreference pref = new RbPreference(getApplicationContext());
            pref.put("pref_user_code", "");
            pref.put("pref_user_id", "");
            pref.put("pref_user_name", "");
            pref.put("pref_user_type", "");
            pref.put("pref_user_group", "");
            pref.put("pref_user_phone", "");
            pref.put("pref_user_email", "");
            pref.put("pref_access_token", "");
            pref.put("pref_app_menu1", "");
            pref.put("pref_app_menu2", "");
            pref.put("pref_app_menu3", "");
            pref.put("pref_app_menu4", "");
            pref.put("pref_app_menu1_chk", "");
            pref.put("pref_app_menu2_chk", "");
            pref.put("pref_app_menu3_chk", "");
            pref.put("pref_app_menu4_chk", "");

            SmartSingleton.clearInstance();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void replaceFragment(int reqNewFragmentIndex)
    {
        /*
        Fragment newFragment = null;
        newFragment = getFragment(reqNewFragmentIndex);

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.commit();
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 10001:
                if( data != null) {
                }
                /*
                SmartBuildFragment fragment1 = (SmartBuildFragment) MainActivity.tempFragmentBuild;
                if(fragment1 != null) {
                    fragment1.adapter.clear();
                    fragment1.addItems();
                }
                */
                break;

            case 20001:
                if( data != null) {
                }
                SmartClientFragment fragment2 = (SmartClientFragment) MainActivity.tempFragmentClient;
                if(fragment2 != null) {
                    fragment2.adapter.clear();
                    fragment2.addItems();
                }
                break;

            case 30001:
                if( data != null) {
                }
                SmartOrderFragment fragment3 = (SmartOrderFragment) MainActivity.tempFragmentOrder;
                if(fragment3 != null) {
                    fragment3.adapter.clear();
                    fragment3.addItems();
                }
                break;
        }

        if( data != null) {
            switch(data.getIntExtra("requestCode", 0)) {

                case 21001:
                    SmartClientFragment fragment2 = (SmartClientFragment) MainActivity.tempFragmentClient;
                    if(fragment2 != null) {
                        fragment2.adapter.clear();
                        fragment2.addItems();
                    }
                    break;

                case 31001:
                    SmartOrderFragment fragment = (SmartOrderFragment) MainActivity.tempFragmentOrder;
                    if(fragment != null) {
                        fragment.adapter.clear();
                        fragment.addItems();
                    }
                    break;

                case 41001:
                    SmartApprovalFragment fragment4 = (SmartApprovalFragment) MainActivity.tempFragmentApproval;
                    if(fragment4 != null) {
                        fragment4.adapter.clear();
                        fragment4.addItems("전체");
                    }
                    break;
            }
            Log.d(TAG, "requestCode : " + requestCode + " / data : " + data.getIntExtra("requestCode", 0) + " ----------------------------------------------");
        }
    }

    /*
     * viewPager 관련 Adapter 클래스 ##################################################################
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {



        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            return getFragment(position);
        }

        @Override
        public int getCount() {
            int count;
            if(pref.getValue("pref_user_type","").equals("client")) {   // 건축주이면 2개 메뉴 구성
                count = 2;
            }else{                                                          // 현장소장이면 4개 메뉴 구성
                count = appMenu_chk.size();
            }

            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return appMenu.get(position).toString();
        }

        private Fragment getFragment(int idx) {
            //tempFragmentBuild = null;
            //tempFragmentClient = null;
            //tempFragmentOrder = null;

            Fragment fragment = null;
            switch(idx) {
                case 0:
                    tempFragmentBuild = new SmartBuildFragment();
                    fragment = tempFragmentBuild;
                    break;
                case 1:
                    tempFragmentClient = new SmartClientFragment();
                    fragment = tempFragmentClient;
                    break;
                case 2:
                    tempFragmentOrder = new SmartOrderFragment();
                    fragment = tempFragmentOrder;
                    break;
                case 3:
                    tempFragmentApproval = new SmartApprovalFragment();
                    fragment = tempFragmentApproval;
                    break;
                default:
                    Log.d("getFragment", "Unhandle Case");
                    break;
            }

            return fragment;
        }
    }



    /* 업 버전 체크 쓰레드 */
    public class BackgroundThread extends Thread {
        @Override
        public void run() {
            // 패키지 네임 전달
            storeVersion = MarketVersionChecker.getMarketVersion(getPackageName());
            // 디바이스 버전 가져옴
            try {
                deviceVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            deviceVersionCheckHandler.sendMessage(deviceVersionCheckHandler.obtainMessage());
            // 핸들러로 메세지 전달
        }
    }

    private final DeviceVersionCheckHandler deviceVersionCheckHandler = new DeviceVersionCheckHandler(this);
    // 핸들러 객체 만들기
    private static class DeviceVersionCheckHandler extends Handler{
        private final WeakReference<MainActivity> mainActivityWeakReference;
        public DeviceVersionCheckHandler(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mainActivityWeakReference.get();
            if (activity != null) {
                activity.handleMessage(msg);
                // 핸들메세지로 결과값 전달
            }
        }
    }

    private void handleMessage(Message msg) {
        //핸들러에서 넘어온 값 체크
        //Toast.makeText(MainActivity.this, storeVersion + " - " + deviceVersion, Toast.LENGTH_SHORT).show();
        if (storeVersion.compareTo(deviceVersion) > 0) {
            // 업데이트 필요
            new MaterialDialog.Builder(this)
                    .title("업데이트")
                    .content("새로운 버전이 있습니다.\n보다 나은 사용을 위해 업데이트 해 주세요.")
                    .positiveText("업데이트 바로가기")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id="+getPackageName()));
                            startActivity(intent);
                            //Toast.makeText(MainActivity.this, "플레이스토어 링크" , Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();

        } else {
            // 업데이트 불필요
            //Toast.makeText(MainActivity.this, "스마트건설관리 최신버전 입니다" , Toast.LENGTH_SHORT).show();
        }
    }

}