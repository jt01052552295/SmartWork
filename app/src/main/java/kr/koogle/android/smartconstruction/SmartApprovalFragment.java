package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartApproval;
import kr.koogle.android.smartconstruction.http.SmartOrder;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2016-12-27.
 */

public class SmartApprovalFragment extends Fragment {

    private static final String TAG = "SmartApproval";
    private RbPreference pref;

    public static RecyclerView recyclerView;
    public static SmartApprovalAdpater adapter;

    private static String strBuildCode = "start";
    private static Boolean isNewBuild = false;
    private static String strWorkTitleTop = "";

    private static boolean isLoading;
    private static int visibleThreshold = 10;

    private RecyclerView.LayoutManager layoutManager;

    private View rootView;
    private LayoutInflater mInflater;
    private View viewEmpty;
    private Spinner spinner;
    private String documentType;

    // Pull to Refresh 4-1
    private SwipeRefreshLayout swipeContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_smart_approval, container, false);
        mInflater = inflater;

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getContext());

        Log.e(TAG, "SmartApprovalFragment 실행");

        spinner = (Spinner) rootView.findViewById(R.id.document_type);
        ArrayAdapter docuAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.document_type, R.layout.support_simple_spinner_dropdown_item);
        docuAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(docuAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                documentType = parent.getItemAtPosition(position).toString();
                fetchTimelineAsync(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // RecyclerView 저장
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_smart_approval);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(getActivity());
        // RecycleView에 LayoutManager 세팅
        recyclerView.setLayoutManager(layoutManager);

        /******************************************************************************************/
        // Adapter 생성
        adapter = new SmartApprovalAdpater(getContext(), SmartSingleton.arrSmartApprovals);
        // 리스트 추가
        //if(isNewBuild || SmartSingleton.arrSmartApprovals.isEmpty()) {
            //addItems("전체");
        //}
        // RecycleView 에 Adapter 세팅
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new SlideInUpAnimator());
        /******************************************************************************************/


        // 스크롤 이벤트 잡아내기 !!
        final NestedScrollView parentScrollView=(NestedScrollView)rootView.findViewById (R.id.nsv_fragment_smart_approval);
        parentScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                final int totalItemCount = layoutManager.getItemCount();
                final int layoutManagerH = layoutManager.getHeight();
                final int parentH = parentScrollView.getHeight();
                //final int itemH = layoutManager.getChildAt(0).getHeight();

                if ( layoutManagerH - parentH - scrollY < 10 ) {
                    isLoading = true;
                    /******************************************************************************************/
                    addItems(documentType);
                    /******************************************************************************************/
                }
            }
        });

        final LinearLayout empLayout = (LinearLayout) rootView.findViewById(R.id.emp_layout); // 내용없을때 보이는 레이아웃
        // 리스트 표현하기 !!
        if (SmartSingleton.arrSmartApprovals.isEmpty()) {
            //empLayout.setVisibility(View.VISIBLE);
        } else {
            //empLayout.setVisibility(View.GONE);
        }

        /***************************************************************************/
        // 리스트 클릭시 상세 페이지 보기 !!
        adapter.setOnItemClickListener(new SmartApprovalAdpater.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                adapter.notifyItemChanged(position);
                final String strSubject = SmartSingleton.arrSmartApprovals.get(position).strSubject;
                final int intId = SmartSingleton.arrSmartApprovals.get(position).intId;
                Intent intext = null;

                if(strSubject.equals("기안서")) {
                    intext = new Intent(getActivity(), SmartApprovalViewActivity.class);
                } else if(strSubject.equals("지출결의서")) {
                    intext = new Intent(getActivity(), SmartApprovalViewActivity45.class);
                } else if(strSubject.equals("설계변경")) {
                    intext = new Intent(getActivity(), SmartApprovalViewActivity47.class);
                } else if(strSubject.equals("무상처리")) {
                    intext = new Intent(getActivity(), SmartApprovalViewActivity48.class);
                }


                intext.putExtra("intId", intId);
                intext.putExtra("strSubject", strSubject);
                startActivityForResult(intext, 41001);
                //Toast.makeText(getContext(), "intId : " + intId +" - " +strSubject, Toast.LENGTH_SHORT).show();
            }
        });

        /***************************************************************************/

        // Handling Touch Events
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                // Handle on touch events here
                //Log.d(TAG, "onTouchEvent : touched !!");
            }

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        // 스크롤시 FAB 버튼 숨기기 !!
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final FloatingActionButton fab = MainActivity.fab;
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 3) {
                    // Scroll Down
                    if (fab.isShown()) {
                        fab.hide();
                    }
                } else {
                    // Scroll Up
                    if (!fab.isShown()) {
                        fab.show();
                    }
                }
                Log.e(TAG, "dy : " + dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == 0) fab.show();
                Log.e(TAG, "newState : " + newState);
            }
        });

        // Pull to Refresh 4-2
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_smart_approval);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright);

        return rootView;
    }

    // Pull to Refresh 4-3
    public void fetchTimelineAsync(int page) {
        adapter.clear();
        addItems(documentType);
    }

    public void addItems(String docuType) {
        documentType = docuType;

        /******************************************************************************************/
        // SmartApproval 값 불러오기 (전자결재)
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        final Map<String, String> mapOptions = new HashMap<String, String>();
        mapOptions.put("offset", String.valueOf(layoutManager.getItemCount()));
        Log.e(TAG, "offset " + layoutManager.getItemCount());

        Call<ArrayList<SmartApproval>> call = null;

        if(documentType.equals("전체")){
            call = smartService.getApprovals(mapOptions);
        } else if(documentType.equals("기안서")){
            call = smartService.getApprovals46(mapOptions);
        } else if(documentType.equals("지출결의서")){
            call = smartService.getApprovals45(mapOptions);
        } else if(documentType.equals("설계변경")){
            call = smartService.getApprovals47(mapOptions);
        } else if(documentType.equals("무상처리")){
            call = smartService.getApprovals48(mapOptions);
        }


        call.enqueue(new Callback<ArrayList<SmartApproval>>() {
            @Override
            public void onResponse(Call<ArrayList<SmartApproval>> call, Response<ArrayList<SmartApproval>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ArrayList<SmartApproval> responseSmartApprovals = response.body();

                    if(responseSmartApprovals.size() != 0 ) {
                        Log.e(TAG, "responseSmartApprovals : size " + responseSmartApprovals.size());
                        SmartSingleton.arrSmartApprovals.addAll(responseSmartApprovals);
                        // 최근 카운트 체크
                        int curSize = adapter.getItemCount();
                        adapter.notifyItemRangeInserted(curSize, responseSmartApprovals.size());

                        RelativeLayout rmSmartBBSApproval = (RelativeLayout) rootView.findViewById(R.id.fm_smart_bbs_approval);
                        rmSmartBBSApproval.removeView(viewEmpty);
                    } else {
                        if(SmartSingleton.arrSmartApprovals.isEmpty()){
                            viewEmpty = mInflater.inflate(R.layout.row_empty, null);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            viewEmpty.setLayoutParams(params);
                            RelativeLayout rmSmartBBSApproval = (RelativeLayout) rootView.findViewById(R.id.fm_smart_bbs_approval);
                            rmSmartBBSApproval.addView(viewEmpty);
                        } else {
                            RelativeLayout rmSmartBBSApproval = (RelativeLayout) rootView.findViewById(R.id.fm_smart_bbs_approval);
                            rmSmartBBSApproval.removeView(viewEmpty);
                        }
                    }

                } else {
                    Toast.makeText(getContext(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responseSmartApprovals : 데이터가 정확하지 않습니다.");
                }

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<SmartApproval>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
            }
        });



    }


}
