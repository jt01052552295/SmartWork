package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.daimajia.easing.linear.Linear;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartApproval;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.HtmlRemoteImageGetterLee;
import kr.koogle.android.smartconstruction.util.RbPreference;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017-01-04.
 */

public class SmartApprovalViewActivity47 extends AppCompatActivity {
    private static final String TAG = "SmartApprovalView";
    private RbPreference pref;

    @Bind(R.id.txt_approval_view_subject) TextView _txtSubject;

    @Bind(R.id.txt_approval_view_sign1) TextView _txtSign1;
    @Bind(R.id.txt_approval_view_sign2) ImageView _txtSign2;
    @Bind(R.id.txt_approval_view_sign_text) TextView _txtSignName;
    @Bind(R.id.txt_approval_view_sign3) TextView _txtSign3;

    @Bind(R.id.txt_approval_view_docno) TextView _txtDocno;
    @Bind(R.id.txt_approval_view_name) TextView _txtName;
    @Bind(R.id.txt_approval_view_date) TextView _txtDate;
    @Bind(R.id.txt_approval_view_keep) TextView _txtkeep;
    @Bind(R.id.txt_approval_view_impo) TextView _txtImpo;
    @Bind(R.id.txt_approval_view_who) TextView _txtWho;
    @Bind(R.id.txt_approval_view_title) TextView _txtTitle;
    @Bind(R.id.txt_approval_view_cate) TextView _txtCate;
    @Bind(R.id.txt_approval_view_cate2) TextView _txtCate2;
    @Bind(R.id.txt_approval_view_cost) TextView _txtCost;
    @Bind(R.id.txt_approval_view_cost2) TextView _txtCost2;
    @Bind(R.id.txt_approval_view_cost3) TextView _txtCost3;
    @Bind(R.id.txt_approval_view_detail_comName) TextView _txtDetailComName;
    @Bind(R.id.txt_approval_view_detail_comTel) TextView _txtDetailComTel;
    @Bind(R.id.txt_approval_view_detail_comPerson) TextView _txtDetailComPerson;
    @Bind(R.id.txt_approval_view_detail_comPersonTel) TextView _txtDetailComPersonTel;
    @Bind(R.id.txt_approval_view_detail_memo) TextView _txtDetailMemo;
    @Bind(R.id.txt_approval_view_detail_item) TextView _txtDetailitem;
    @Bind(R.id.txt_approval_view_detail_cost1) TextView _txtDetailCost1;
    @Bind(R.id.txt_approval_view_detail_cost2) TextView _txtDetailCost2;
    @Bind(R.id.txt_approval_view_detail_cost3) TextView _txtDetailCost3;
    @Bind(R.id.txt_approval_view_detail_cost4) TextView _txtDetailCost4;
    @Bind(R.id.txt_approval_view_detail_cost5) TextView _txtDetailCost5;
    @Bind(R.id.txt_approval_view_detail_cost6) TextView _txtDetailCost6;
    @Bind(R.id.txt_approval_view_content) HtmlTextView _txtContent;
    @Bind(R.id.txt_approval_view_return) TextView _txtReturn;
    @Bind(R.id.btn_approval_view_top) Button _btnTop;
    @Bind(R.id.ll_attach_file) LinearLayout _llAttachFile;
    @Bind(R.id.txt_attach_file) TextView _txtAttachFile;

    @Bind(R.id.btn_approval_wrap) LinearLayout _approvalWrap;
    @Bind(R.id.btn_approval_back) Button _backApproval;
    @Bind(R.id.btn_approval_update) Button _upApproval;
    @Bind(R.id.btn_approval_update2) Button _up2Approval;


    // recycleViewer
    private static RecyclerView recyclerView;
    private SmartApprovalViewAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private static RecyclerView recyclerViewSign;
    private SmartApprovalViewSignAdapter signAdapter;
    private RecyclerView.LayoutManager layoutManagerSign;

    // intent 로 넘어온 값 받기
    private Intent intent;
    private String approvalCode;
    private String approvalType;

    private ProgressWheel wheel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval_view47);
        ButterKnife.bind(this);

        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        // 리스트 클릭시 넘어온값 받기 !!
        approvalCode = String.valueOf(getIntent().getExtras().getInt("intId"));
        approvalType = String.valueOf(getIntent().getExtras().getString("strSubject"));

        //Toast.makeText(SmartApprovalViewActivity47.this, "approvalCode : " + approvalCode + "approvalType : " + approvalType  , Toast.LENGTH_LONG).show();
        // RecyclerView 저장
        recyclerView = (RecyclerView) findViewById(R.id.rv_approval_view_comments);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(SmartApprovalViewActivity47.this);
        // RecycleView에 LayoutManager 세팅
        recyclerView.setLayoutManager(layoutManager);

        // 결재사인
        recyclerViewSign = (RecyclerView) findViewById(R.id.rv_approval_view_sign);
        layoutManagerSign = new LinearLayoutManager(SmartApprovalViewActivity47.this);
        recyclerViewSign.setLayoutManager(layoutManagerSign);


        // SmartSingleton 초기화
        SmartSingleton.arrApprovalComments.clear();
        SmartSingleton.arrApprovalDetails.clear();
        SmartSingleton.arrApprovalSign.clear();

        // Adapter 생성
        adapter = new SmartApprovalViewAdapter(this, SmartSingleton.arrApprovalComments);
        signAdapter = new SmartApprovalViewSignAdapter(this, SmartSingleton.arrApprovalSign);

        // 현재 결재 데이터 불러오기
        addRows();
        // RecycleView 에 Adapter 세팅
        recyclerView.setAdapter(adapter);
        recyclerViewSign.setAdapter(signAdapter);


        // 리스트 표현하기 !!
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(itemAnimator);


        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_approval_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmartApprovalViewActivity47.this.finish();
            }
        });

        _btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollView scrollView = (ScrollView) findViewById(R.id.sv_approval_view);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });

        _backApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intId = SmartSingleton.smartApproval.intId;
                docBack(intId);
            }
        });

        // 결재하기
        _upApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intId = SmartSingleton.smartApproval.intId;
                String strSignDate = SmartSingleton.smartApproval.strSignDate;
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String strNowSign = "";
                String bType = "update";
                strSignDate = strSignDate +"|" + date;

                for(int i = 0; i<SmartSingleton.smartApproval.arrApprovalSign.size(); i++){
                    // 다음 결재 할 사람
                    if(SmartSingleton.smartApproval.strNowSign.equals(SmartSingleton.smartApproval.arrApprovalSign.get(i).strSignCode)){
                        int index = i + 1;
                        if(SmartSingleton.smartApproval.arrApprovalSign.size() <= index) {
                            strNowSign = "";
                        }else{
                            strNowSign = SmartSingleton.smartApproval.arrApprovalSign.get(index).strSignCode;
                        }
                    }
                }

                SmartService service = ServiceGenerator.createService(SmartService.class);
                final Map<String, String> mapFields = new HashMap<String, String>();
                mapFields.put("intId", intId+"");
                mapFields.put("strNowSign", strNowSign);
                mapFields.put("strSignDate", strSignDate);
                mapFields.put("bType", bType);

                Call<ResponseBody> call = service.updateDocument(mapFields);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            //Log.e("updateDocument", "success / " + response.body().string());
                            new MaterialDialog.Builder(SmartApprovalViewActivity47.this)
                                    .title("결재 완료")
                                    .content("정상적으로 결재 되었습니다.")
                                    .positiveText("확인")
                                    .onAny(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            finish();
                                        }
                                    }).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("updateDocument error:", t.getMessage());
                    }
                });
            }
        });

        // 전결하기
        _up2Approval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intId = SmartSingleton.smartApproval.intId;
                String strSignDate = SmartSingleton.smartApproval.strSignDate;
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String bType = "update2";
                strSignDate = strSignDate +"|" + date;

                SmartService service = ServiceGenerator.createService(SmartService.class);
                final Map<String, String> mapFields = new HashMap<String, String>();
                mapFields.put("intId", intId+"");
                mapFields.put("strSignDate", strSignDate);
                mapFields.put("bType", bType);

                Call<ResponseBody> call = service.update2Document(mapFields);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            //Log.v("update2Document", "success / " + response.body().string());
                            new MaterialDialog.Builder(SmartApprovalViewActivity47.this)
                                    .title("전결 완료")
                                    .content("정상적으로 전결 되었습니다.")
                                    .positiveText("확인")
                                    .onAny(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            finish();
                                        }
                                    }).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("updateDocument error:", t.getMessage());
                    }
                });
            }
        });

    }

    private void addRows(){
        /******************************************************************************************/
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        Call<SmartApproval> call = smartService.getApproval(approvalCode);
        call.enqueue(new Callback<SmartApproval>() {
            @Override
            public void onResponse(Call<SmartApproval> call, Response<SmartApproval> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SmartSingleton.smartApproval = response.body();

                    if(SmartSingleton.smartApproval.intId != 0){
                        _txtSubject.setText(SmartSingleton.smartApproval.strSubject);
                        _txtDocno.setText(SmartSingleton.smartApproval.strDocNo);
                        _txtName.setText(SmartSingleton.smartApproval.strName);
                        _txtDate.setText(SmartSingleton.smartApproval.datRegist);
                        _txtkeep.setText(SmartSingleton.smartApproval.strKeep + "년");
                        _txtImpo.setText(SmartSingleton.smartApproval.strImportance);

                        _txtCate.setText("");
                        if (!SmartSingleton.arrSmartBuilds.isEmpty()) {
                            for (int i = 0; i < SmartSingleton.arrSmartBuilds.size(); i++) {
                                if( SmartSingleton.arrSmartBuilds.get(i).strCode.equals(SmartSingleton.smartApproval.strCate1) )
                                    _txtCate.setText(SmartSingleton.arrSmartBuilds.get(i).strName);
                            }
                        }
                        _txtCate.setText(SmartSingleton.smartApproval.strCate2);

                        _txtCost.setText("0 원");
                        if(SmartSingleton.smartApproval.strCost1 != "") {
                            NumberFormat nf = NumberFormat.getInstance();
                            int price1 = Integer.parseInt(SmartSingleton.smartApproval.strCost1);
                            String cost1 = nf.format(price1);
                            _txtCost.setText(cost1+"원");
                        }

                        _txtCost2.setText("0 원");
                        if(SmartSingleton.smartApproval.strCost2 != "") {
                            NumberFormat nf = NumberFormat.getInstance();
                            int price2 = Integer.parseInt(SmartSingleton.smartApproval.strCost2);
                            String cost2 = nf.format(price2);
                            _txtCost2.setText(cost2+"원");
                        }

                        _txtCost3.setText("0 원");
                        if(SmartSingleton.smartApproval.strCost3 != "") {
                            NumberFormat nf = NumberFormat.getInstance();
                            int price3 = Integer.parseInt(SmartSingleton.smartApproval.strCost3);
                            String cost3 = nf.format(price3);
                            _txtCost3.setText(cost3+"원");
                        }

                        _txtWho.setText(SmartSingleton.smartApproval.strWho);
                        _txtTitle.setText(SmartSingleton.smartApproval.strTitle);
                        _txtContent.setHtml(SmartSingleton.smartApproval.strContent, new HtmlRemoteImageGetterLee(_txtContent, null, true, _txtContent.getWidth()));

                        if(SmartSingleton.smartApproval.arrFiles.size() > 0 ){
                            _llAttachFile.setVisibility(View.VISIBLE);
                            _txtAttachFile.setText(SmartSingleton.smartApproval.arrFiles.get(0).strNameOrigin);
                            _txtAttachFile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri uri = Uri.parse(SmartSingleton.smartApproval.arrFiles.get(0).strURL + SmartSingleton.smartApproval.arrFiles.get(0).strName);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            });
                        }

                        String[] arr_signDate   = SmartSingleton.smartApproval.strSignDate.split("\\|");
                        _txtSign1.setText("담당자");
                        // 담당자 서명
                        for (int i = 0; i < SmartSingleton.arrSmartEmployees.size(); i++) {
                            if( SmartSingleton.arrSmartEmployees.get(i).strCode.equals(SmartSingleton.smartApproval.strId) ) {
                                if(SmartSingleton.arrSmartEmployees.get(i).strSignImageURL.isEmpty() || SmartSingleton.arrSmartEmployees.get(i).strSignImageURL.equals("")){
                                    _txtSignName.setText(SmartSingleton.arrSmartEmployees.get(i).strName + " " + SmartSingleton.arrSmartEmployees.get(i).strPosition);
                                } else {
                                    Glide.with(getApplicationContext()).load(SmartSingleton.arrSmartEmployees.get(i).strSignImageURL).into(_txtSign2);
                                }
                            }
                        }
                        _txtSign3.setText(arr_signDate[1]);

                        // 결재 할 사람 버튼 활성화
                        if(SmartSingleton.smartApproval.strNowSign.equals(pref.getValue("pref_user_code", ""))
                                && SmartSingleton.smartApproval.intLevel != 3 ){
                            _approvalWrap.setVisibility(View.VISIBLE);
                        }

                        for(int i = 0; i<SmartSingleton.smartApproval.arrApprovalSign.size(); i++){
                            Log.e(TAG, " arrApprovalSign " + i + " : "
                                    + SmartSingleton.smartApproval.arrApprovalSign.get(i).strSignCode + ", "
                                    + SmartSingleton.smartApproval.arrApprovalSign.get(i).strSignDate + ", "
                                    + SmartSingleton.smartApproval.arrApprovalSign.get(i).strSignDateValue);
                        }



                        if(SmartSingleton.smartApproval.arrApprovalDetails.size() == 0 ){
                            _txtDetailComName.setText("");
                            _txtDetailComTel.setText("");
                            _txtDetailComPerson.setText("");
                            _txtDetailComPersonTel.setText("");
                            _txtDetailMemo.setText("");
                            _txtDetailitem.setText("");
                            _txtDetailCost1.setText("");
                            _txtDetailCost2.setText("");
                            _txtDetailCost3.setText("");
                            _txtDetailCost4.setText("");
                            _txtDetailCost5.setText("");
                            _txtDetailCost6.setText("");
                        } else {
                            _txtDetailComName.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strComName);
                            _txtDetailComTel.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strComTel);
                            _txtDetailComPerson.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strComPerson);
                            _txtDetailComPersonTel.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strComPersonTel);
                            _txtDetailMemo.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strComMemo);
                            _txtDetailitem.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strItem);
                            _txtDetailCost1.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strCost1 + "원");
                            _txtDetailCost2.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strCost2 + "원");
                            _txtDetailCost3.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strCost3 + "원");
                            _txtDetailCost4.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strCost4 + "원");
                            _txtDetailCost5.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strCost5 + "원");
                            _txtDetailCost6.setText(SmartSingleton.smartApproval.arrApprovalDetails.get(0).strCost6 + "원");
                        }

                        // 결재자 사인 어댑터 갱신
                        SmartSingleton.arrApprovalSign.addAll(SmartSingleton.smartApproval.arrApprovalSign);
                        signAdapter.notifyDataSetChanged();

                        // 결재 코멘트 어댑터 갱신
                        SmartSingleton.arrApprovalComments.addAll(SmartSingleton.smartApproval.arrApprovalComments);
                        adapter.notifyDataSetChanged();


                    } else {
                        Toast.makeText(SmartApprovalViewActivity47.this, "데이터가 정확하지 않습니다." , Toast.LENGTH_LONG).show();
                        Log.e("Error", "smartApproval : 데이터가 정확하지 않습니다.");
                    }
                }
            }

            @Override
            public void onFailure(Call<SmartApproval> call, Throwable t) {
                Toast.makeText(SmartApprovalViewActivity47.this, "네트워크 상태가 좋지 않습니다!" , Toast.LENGTH_LONG).show();
                Log.d("Error", t.getMessage());
            }
        });


    }

    // 반려하기 실행
    private void docBack(int intId){
        Log.d(TAG, "docBack 실행" + intId);
        Intent intext = new Intent(getApplicationContext(), SmartApprovalBackActivity.class);
        intext.putExtra("intId", intId);
        startActivityForResult(intext, 41001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("onActivityResult - ", SmartSingleton.smartApproval.strReturn);
        finish();

    }
}
