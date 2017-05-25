package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.FileUploadService;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartClient;
import kr.koogle.android.smartconstruction.http.SmartComment;
import kr.koogle.android.smartconstruction.http.SmartPhoto;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.HtmlRemoteImageGetterLee;
import kr.koogle.android.smartconstruction.util.RbPreference;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class SmartClientViewActivity extends AppCompatActivity {
    private static final String TAG = "SmartClientView";
    private RbPreference pref;

    @Bind(R.id.txt_client_view_title) TextView _txtTitle;
    @Bind(R.id.txt_client_view_writer) TextView _txtWriter;
    @Bind(R.id.txt_client_view_date) TextView _txtDate;
    @Bind(R.id.txt_client_view_content) HtmlTextView _txtContent;
    @Bind(R.id.ll_attach_file) LinearLayout _llAttachFile;
    @Bind(R.id.txt_attach_file) TextView _txtAttachFile;

    @Bind(R.id.btn_client_view_modify) Button _btnModify;
    @Bind(R.id.btn_client_view_delete) Button _btnDelete;
    @Bind(R.id.btn_client_view_top) Button _btnTop;
    @Bind(R.id.btn_client_view_regist_comment) Button _btnRegistComment;
    @Bind(R.id.img_client_view_camera) ImageView _btnAddPhoto;

    @Bind(R.id.input_client_view_comment) TextView _txtComment;
    @Bind(R.id.input_client_view_comment_photo) ImageView _imgCommentPhoto;
    private String commentPhotoCode = "";

    // recycleViewer
    private static RecyclerView recyclerView;
    private SmartClientViewAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // intent 로 넘어온 값 받기
    private Intent intent;
    private String clientCode;

    private ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_view);
        ButterKnife.bind(this);
        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        // 리스트 클릭시 넘어온값 받기 !!
        clientCode = String.valueOf(getIntent().getExtras().getInt("intId"));

        // RecyclerView 저장
        recyclerView = (RecyclerView) findViewById(R.id.rv_client_view_comments);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(SmartClientViewActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerView.setLayoutManager(layoutManager);

        final LinearLayout empLayout = (LinearLayout) findViewById(R.id.emp_layout); // 내용없을때 보이는 레이아웃
        // 리스트 표현하기 !!
        if (SmartSingleton.smartClient.arrComments.isEmpty()) {
            //empLayout.setVisibility(View.VISIBLE);
        } else {
            //empLayout.setVisibility(View.GONE);
            //recyclerView.setItemAnimator(new SlideInUpAnimator());
        }

        // arrComments 초기화
        SmartSingleton.arrComments.clear();

        // Adapter 생성
        adapter = new SmartClientViewAdapter(this, SmartSingleton.arrComments);
        //if(SmartSingleton.arrComments.isEmpty() || true) {

            //wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
            //wheel.setVisibility(View.VISIBLE);
            //wheel.setBarColor(R.color.colorPrimary);
            //wheel.spin();

            addRows();
        //}
        // RecycleView 에 Adapter 세팅
        recyclerView.setAdapter(adapter);
        // 리스트 표현하기 !!
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(itemAnimator);
        /*
        notifyItemChanged(int)
        notifyItemInserted(int)
        notifyItemRemoved(int)
        notifyItemRangeChanged(int, int)
        notifyItemRangeInserted(int, int)
        notifyItemRangeRemoved(int, int)
         */

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_client_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmartClientViewActivity.this.finish();
            }
        });

        // 건축주 협의 등록버튼
        _btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SmartClientViewActivity.this, SmartClientWriteActivity.class);
                intent.putExtra("intId", SmartSingleton.smartClient.intId);
                startActivityForResult(intent, 22001);
            }
        });

        // 건축주 협의 삭제버튼
        _btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(SmartClientViewActivity.this)
                        .title("게시글 삭제")
                        .content("글이 삭제되면 복구할 수 없습니다. 정말로 삭제하시겠습니까?")
                        .positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                deleteClient(SmartSingleton.smartClient.intId);
                            }
                        })
                        .show();
            }
        });

        // Top 버튼 클릭시 상단 이동
        _btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScrollView scrollView = (ScrollView) findViewById(R.id.sv_client_view);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });

        // 이미지 추가 버튼 클릭시 이미리 리스트 페이지 이동
        _btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SmartClientViewActivity.this, CameraPicListActivity.class);
                intent.putExtra("intId", SmartSingleton.smartClient.intId);
                startActivityForResult(intent, 22006);
                //Toast.makeText(SmartClientViewActivity.this, "intId : " + smartClient.intId, Toast.LENGTH_SHORT).show();
            }
        });

        // 코멘트 등록 버튼
        _btnRegistComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( _txtComment.getText().toString().isEmpty() ) {

                    new MaterialDialog.Builder(SmartClientViewActivity.this)
                            .title("답변등록확인")
                            .content("답변을 정확하게 입력하세요.")
                            .positiveText("확인")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            })
                            .show();

                } else {
                    registComment();
                }
            }
        });

        /***************************************************************************/
        // 리스트 클릭시 상세 페이지 보기
        adapter.setOnItemClickListener(new SmartClientViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.notifyItemChanged(position);

                Intent intext = new Intent(SmartClientViewActivity.this, SmartClientViewActivity.class);
                final int intId = SmartSingleton.arrComments.get(position).intId;
                intext.putExtra("intId", intId);
                //startActivityForResult(intext, 1002);
                //Toast.makeText(SmartClientViewActivity.this, "intId : " + intId, Toast.LENGTH_SHORT).show();
            }
        });

        // 리스트 X버튼 클릭시 해당 글 삭제
        adapter.setOnItemXClickListener(new SmartClientViewAdapter.OnItemXClickListener() {
            @Override
            public void onItemXClick(View itemView, int position) {

                final String commentCode = String.valueOf(SmartSingleton.arrComments.get(position).intId);
                SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
                Call<ResponseBody> call = smartService.deleteComment(commentCode);
                Toast.makeText(SmartClientViewActivity.this, "commentCode : " + commentCode, Toast.LENGTH_SHORT).show();

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            //smartClient = response.body();
                            Log.d(TAG, response.body().toString());
                            //Log.d(TAG, "title : " + smartClient.arrComments.get(0).strContent.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(SmartClientViewActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                        Log.d("Error", t.getMessage());
                    }
                });

                adapter.remove(position);
                //Toast.makeText(SmartClientViewActivity.this, "position : " + position, Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/

    }

    private void deleteClient(int intId) {
        /******************************************************************************************/
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        Call<ResponseBody> call = smartService.deleteClient(String.valueOf(intId));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(SmartClientViewActivity.this, CameraPicListActivity.class);
                    intent.putExtra("requestCode", 21001);
                    SmartClientViewActivity.this.setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(SmartClientViewActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

    private void addRows() {
        /******************************************************************************************/
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        Call<SmartClient> call = smartService.getSmartBBSClient(clientCode);

        call.enqueue(new Callback<SmartClient>() {
            @Override
            public void onResponse(Call<SmartClient> call, Response<SmartClient> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SmartSingleton.smartClient = response.body();

                    if (SmartSingleton.smartClient.intId != 0) {
                        _txtTitle.setText(SmartSingleton.smartClient.strTitle);
                        _txtWriter.setText(SmartSingleton.smartClient.strWriter);
                        _txtDate.setText(SmartSingleton.smartClient.datWrite);
                        /*
                        HtmlRemoteImageGetter imgGetter = new HtmlRemoteImageGetter(_txtContent, smartClient.strContent);
                        _txtContent.setText(Html.fromHtml(smartClient.strContent, imgGetter, null));
                        */
                        _txtContent.setHtml(SmartSingleton.smartClient.strContent, new HtmlRemoteImageGetterLee(_txtContent, null, true, _txtContent.getWidth()));
                        //Toast.makeText(SmartClientViewActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();

                        if(SmartSingleton.smartClient.arrFiles.size() > 0) {
                            _llAttachFile.setVisibility(View.VISIBLE);
                            _txtAttachFile.setText(SmartSingleton.smartClient.arrFiles.get(0).strNameOrigin);
                            _txtAttachFile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Uri uri = Uri.parse(SmartSingleton.smartClient.arrFiles.get(0).strURL + SmartSingleton.smartClient.arrFiles.get(0).strName);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            });
                        }

                        SmartSingleton.arrComments.addAll(SmartSingleton.smartClient.arrComments);
                        adapter.notifyDataSetChanged();
                        // comment 리스트 출력 !!
                        //int curSize = adapter.getItemCount();
                        //adapter.notifyItemRangeInserted(curSize, smartClient.arrComments.size());

                        // 쓰기 권한 체크
                        if(SmartSingleton.smartClient.strUserId.equals(pref.getValue("pref_user_id",""))) {
                            _btnModify.setVisibility(View.VISIBLE);
                            _btnDelete.setVisibility(View.VISIBLE);
                        } else {
                            _btnModify.setVisibility(View.GONE);
                            _btnDelete.setVisibility(View.GONE);
                        }

                        Log.d(TAG, "curSize : " + adapter.getItemCount() + " / arrComments.size : " + SmartSingleton.smartClient.arrComments.size());

                    } else {
                        Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "smartClient : 데이터가 정확하지 않습니다.");
                    }
                }

                //wheel.stopSpinning();
                //wheel.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<SmartClient> call, Throwable t) {
                Toast.makeText(SmartClientViewActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());

                //wheel.stopSpinning();
                //wheel.setVisibility(View.GONE);
            }
        });
        /******************************************************************************************/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 22001: // 내용 수정 페이지에서 온 경우 내용 새로 고침

                _txtTitle.setText(SmartSingleton.smartClient.strTitle);
                _txtWriter.setText(SmartSingleton.smartClient.strWriter);
                _txtDate.setText(SmartSingleton.smartClient.datWrite);
                _txtContent.setHtml(SmartSingleton.smartClient.strContent, new HtmlRemoteImageGetterLee(_txtContent, null, true, _txtContent.getWidth()));

                ScrollView scrollView = (ScrollView) findViewById(R.id.sv_client_view);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                break;

            case 22006: // 댓글에서 사진 추가하기

                if( data != null) {
                    // intent 객체에서 smartPhoto 객체 받아옴!!
                    Bundle bundle = data.getExtras();
                    SmartPhoto smartPhoto = bundle.getParcelable("smartPhoto");

                    final String intId = String.valueOf(smartPhoto.intId);
                    final String strFileURL = smartPhoto.strURL + smartPhoto.strThumbnail;
                    if (!strFileURL.isEmpty()) {
                        commentPhotoCode = intId; // 답글 이미지 코드값 저장 !!
                        Picasso.with(SmartClientViewActivity.this)
                                .load(strFileURL)
                                .fit() // resize(700,400)
                                .into(_imgCommentPhoto);
                        //_imgCommentPhoto.getLayoutParams().height = 200;
                        //_imgCommentPhoto.requestLayout();
                        _imgCommentPhoto.setVisibility(View.VISIBLE);
                        scrollView = (ScrollView) findViewById(R.id.sv_client_view);
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        //Toast.makeText(SmartClientViewActivity.this, "strFileURL : " + strFileURL, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public void registComment() {
        /******************************************************************************************/
        SmartService service = ServiceGenerator.createService(SmartService.class);
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // File file = fileImage; //FileUtils.getFile(this, fileUri); // Uri 값을 받을 경우 !!

        final Map<String, String> mapFields = new HashMap<String, String>();
        final String content = _txtComment.getText().toString();
        mapFields.put("strBBS", "client");
        mapFields.put("intBBSId", clientCode);
        mapFields.put("strContent", content);
        mapFields.put("strFileCode", commentPhotoCode);
        Call<ResponseBody> call = service.registComment(mapFields);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    //Log.v("Upload", "success / " + response.body().string());

                    final SmartComment sc = new SmartComment();
                    Log.d(TAG, "_txtComment 완료전 : " + _txtComment.getText().toString());
                    sc.intId = Integer.parseInt(clientCode);
                    sc.strContent = _txtComment.getText().toString();
                    sc.strWriter = pref.getValue("pref_user_id", "");
                    sc.strName = pref.getValue("pref_user_name", "");
                    sc.datWrite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    sc.strFileURL = response.body().string();
                    SmartSingleton.arrComments.add(sc);

                    int curSize = adapter.getItemCount();
                    //Log.d(TAG, "전 : curSize : " + adapter.getItemCount() + " / arrComments.size : " + smartClient.arrComments.size());
                    adapter.notifyItemRangeInserted(curSize, 1);
                    //Log.d(TAG, "후 : curSize : " + adapter.getItemCount() + " / arrComments.size : " + smartClient.arrComments.size());
                    /*
                    SmartClientViewActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    */

                    _txtComment.setText("");
                    _imgCommentPhoto.setVisibility(View.GONE);
                    ScrollView scrollView = (ScrollView) findViewById(R.id.sv_client_view);
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    Log.d(TAG, "_txtComment 완료후 : " + _txtComment.getText().toString());

                    new MaterialDialog.Builder(SmartClientViewActivity.this)
                            .title("답변등록 완료")
                            .content("답변이 정상적으로 등록 되었습니다.")
                            .positiveText("확인")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            })
                            .show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

    @UiThread
    protected void dataSetChanged() {
        adapter.notifyDataSetChanged();
    }
}
