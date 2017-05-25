package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.SmartApprovalSign;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;

/**
 * Created by Administrator on 2017-01-05.
 */

public class SmartApprovalViewSignAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SmartApprovalViewDetailsAdapter";
    private RbPreference pref;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_NONE = 2;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private ArrayList<SmartApprovalSign> mRows;
    private Context getContext() {
        return mContext;
    }

    public SmartApprovalViewSignAdapter(Context context, ArrayList<SmartApprovalSign> arrRows){
        mRows = arrRows;
        mContext = context;
        pref = new RbPreference(context.getApplicationContext());
    }

    public void setmOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void add(SmartApprovalSign item, int position) {
        mRows.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mRows.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return VIEW_TYPE_NONE;
        } else {
            return mRows.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_approval_view_sign, parent, false);
            return new UserViewHolder(getContext(), view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_loading_item, parent, false);
            return new LoadingViewHolder(getContext(), view);
        } else if (viewType == VIEW_TYPE_NONE) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_sign_none, parent, false);
            return new RowNoneViewHolder(getContext(), view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SmartApprovalSign row = mRows.get(position);
        if (holder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;

            // 생성되는 View에 position값 저장
            userViewHolder.position = position;

            TextView tvSign1 = userViewHolder.sign1;
            ImageView tvSign2 = userViewHolder.sign2;
            TextView sign_text = userViewHolder.sign_text;
            TextView tvSign3 = userViewHolder.sign3;

            if(row.strSignCode != null){
                for (int i = 0; i < SmartSingleton.arrSmartEmployees.size(); i++) {
                    if( SmartSingleton.arrSmartEmployees.get(i).strCode.equals(row.strSignCode) ) {
                        tvSign1.setText(SmartSingleton.arrSmartEmployees.get(i).strName + " " + SmartSingleton.arrSmartEmployees.get(i).strPosition);
                    }
                }
            }
            if(row.strSignDateValue != null){
                for (int i = 0; i < SmartSingleton.arrSmartEmployees.size(); i++) {
                    if (SmartSingleton.arrSmartEmployees.get(i).strCode.equals(row.strSignCode)) {
                        if(SmartSingleton.arrSmartEmployees.get(i).strSignImageURL.isEmpty() || SmartSingleton.arrSmartEmployees.get(i).strSignImageURL.equals("")){
                            sign_text.setText(SmartSingleton.arrSmartEmployees.get(i).strName);
                        } else {
                            Glide.with(mContext).load(SmartSingleton.arrSmartEmployees.get(i).strSignImageURL).into(tvSign2);
                        }
                    }
                }
                tvSign3.setText(row.strSignDate);
            }


        } else if (holder instanceof LoadingViewHolder)  {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);

            Log.d(TAG, " 결재사인 리스트 생성 : 실패 !! ");
        }
    }

    @Override
    public int getItemCount() {
        return mRows.size();
    }

    public void setLoaded() { isLoading = false; }

    // 로딩용 뷰홀더 클래스 ########################################################################
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public Context context;

        public LoadingViewHolder(Context context, final View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
            this.context = context;
        }
    }

    public static class RowNoneViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        public RowNoneViewHolder(Context context, final View itemView) {
            super(itemView);
            this.context = context;
        }
    }

    // 뷰홀더 클래스 ###############################################################################
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private Context context;
        public int position;
        public TextView sign1;
        public ImageView sign2;
        public TextView sign_text;
        public TextView sign3;

        public UserViewHolder(View itemView) {
            super(itemView);
        }

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            this.context = context;
            sign1 = (TextView) itemView.findViewById(R.id.r_approval_view_sign1);
            sign2 = (ImageView) itemView.findViewById(R.id.r_approval_view_sign2);
            sign_text = (TextView) itemView.findViewById(R.id.r_approval_view_sign_text);
            sign3 = (TextView) itemView.findViewById(R.id.r_approval_view_sign3);
        }
    }

}
