package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.SmartApprovalComment;
import kr.koogle.android.smartconstruction.http.SmartComment;
import kr.koogle.android.smartconstruction.util.ItemClickSupport;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;

/**
 * Created by Administrator on 2017-01-03.
 */

public class SmartApprovalViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SmartApprovalViewAdapter";
    private RbPreference pref;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private ArrayList<SmartApprovalComment> mRows;

    private Context getContext() {
        return mContext;
    }

    public SmartApprovalViewAdapter(Context context, ArrayList<SmartApprovalComment> arrRows) {
        mRows = arrRows;
        mContext = context;
        pref = new RbPreference(context.getApplicationContext());
    }

    public void setmOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void add(SmartApprovalComment item, int position) {
        mRows.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mRows.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mRows.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    // 제네릭 형식의 변수로 ViewHolder를 생성 -----------------------------------------------------
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_approval_view_comment, parent, false);
            return new UserViewHolder(getContext(), view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_loading_item, parent, false);
            return new LoadingViewHolder(getContext(), view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get the data model based on position
        SmartApprovalComment row = mRows.get(position);
        if (holder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            // 생성되는 View에 position값 저장
            userViewHolder.position = position;

            ImageView ivPhoto = userViewHolder.photo;
            TextView tvWriter = userViewHolder.writer;
            TextView tvDate = userViewHolder.date;
            TextView tvContent = userViewHolder.content;
            ImageView ivFile = userViewHolder.commentFile;

            tvWriter.setText(row.strName);
            tvDate.setText(row.datRegist);
            tvContent.setText(row.strMemo);
            Log.d(TAG, " 코멘트 리스트 생성 : " +position+ " / " +row.strName+ " / " +row.strMemo+ " / " );

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);

            Log.d(TAG, " 코멘트 리스트 생성 : 실패 !! ");
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

    // 뷰홀더 클래스 ###############################################################################
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private Context context;
        public int position;
        public ImageView photo;
        public TextView writer;
        public TextView date;
        public TextView content;
        public ImageView commentFile;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            this.context = context;

            photo = (ImageView) itemView.findViewById(R.id.r_approval_view_comment_photo);
            writer = (TextView) itemView.findViewById(R.id.r_approval_view_comment_writer);
            date = (TextView) itemView.findViewById(R.id.r_approval_view_comment_date);
            content = (TextView) itemView.findViewById(R.id.r_approval_view_comment_content);
            commentFile = (ImageView) itemView.findViewById(R.id.r_approval_view_comment_file);

        }


    }

}