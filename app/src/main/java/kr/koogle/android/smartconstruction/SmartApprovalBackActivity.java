package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017-01-06.
 */

public class SmartApprovalBackActivity extends AppCompatActivity {
    private static final String TAG = "SmartApprovalBackActivity";
    private RbPreference pref;

    // intent 로 넘어온 값 받기
    private Intent intent;
    private String intId;

    @Bind(R.id.btn_approval_back_close) Button _btnClose;
    @Bind(R.id.btn_approval_back_return) Button _btnReturn;
    @Bind(R.id.input_approval_back_return) EditText _editReturn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval_back);

        ButterKnife.bind(this);

        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        // 리스트 클릭시 넘어온값 받기 !!
        intId = String.valueOf(getIntent().getExtras().getInt("intId"));

        //Toast.makeText(this, " 넘어온 값 : " + intId, Toast.LENGTH_SHORT).show();

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_approval_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        _btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resultId = Integer.parseInt(intId);
                setResult(resultId);
                finish();
            }
        });

        _btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_editReturn.getText().toString().isEmpty()){
                    new MaterialDialog.Builder(SmartApprovalBackActivity.this)
                            .title("반려사유")
                            .content("반려사유를 입력하세요.")
                            .positiveText("확인")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                } else {
                    addReturn();
                }
            }
        });

    }

    private void addReturn(){
        SmartService service = ServiceGenerator.createService(SmartService.class);
        final Map<String, String> mapFields = new HashMap<String, String>();
        final String content = _editReturn.getText().toString();
        mapFields.put("intId", intId);
        mapFields.put("strReturn", content);
        Call<ResponseBody> call = service.registReturn(mapFields);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    //Log.e(TAG, "success / " + response.body().string());
                    SmartSingleton.smartApproval.strReturn = content;
                    SmartSingleton.smartApproval.intLevel = 3;
                    new MaterialDialog.Builder(SmartApprovalBackActivity.this)
                            .title("반려 완료")
                            .content("정상적으로 반려 되었습니다.")
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
                Log.e("registReturn error:", t.getMessage());
            }
        });
    }
}