package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import kr.koogle.android.smartconstruction.http.SmartApproval;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;

/**
 * Created by Administrator on 2016-12-30.
 */

public class SmartApprovalAdpater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SmartClientAdapter";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private static ArrayList<SmartApproval> mRows;
    private List<SmartApproval> mUsers = SmartSingleton.arrSmartApprovals;

    private Context getContext() {
        return mContext;
    }

    public SmartApprovalAdpater(Context mContext, ArrayList<SmartApproval> arrSmartApprovals) {
        this.mContext = mContext;
        this.mRows = arrSmartApprovals;
    }

    // Clean all elements of the recycler
    public void clear() {
        mRows.clear();
        notifyDataSetChanged();
    }

    public void setmOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mUsers.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_smart_approval, parent, false);
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
        SmartApproval smartApproval = mRows.get(position);

        // Set item views based on your views and data model
        if (holder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;

            ImageView ivImage = userViewHolder.image;
            TextView tvDocno = userViewHolder.docno;
            TextView tvTitle = userViewHolder.title;
            TextView tvCnt  = userViewHolder.cnt;
            TextView tvBuild = userViewHolder.build;
            TextView tvDate = userViewHolder.date;
            TextView tvLevel = userViewHolder.level;
            TextView tvName = userViewHolder.name;
            TextView tvSubject = userViewHolder.subject;

            tvSubject.setText(smartApproval.strSubject);
            tvDocno.setText(smartApproval.strDocNo);
            tvTitle.setText(smartApproval.strTitle);

            if(smartApproval.comCnt > 0 ){
                tvCnt.setText("("+smartApproval.comCnt+")");
            } else {
                tvCnt.setText("(0)");
            }

            tvBuild.setText("현장명");
            if (!SmartSingleton.arrSmartBuilds.isEmpty()) {
                for (int i = 0; i < SmartSingleton.arrSmartBuilds.size(); i++) {
                    if( SmartSingleton.arrSmartBuilds.get(i).strCode.equals(smartApproval.strCate1) )
                        tvBuild.setText(SmartSingleton.arrSmartBuilds.get(i).strName);
                }
            }


            tvName.setText("");
            if(smartApproval.intLevel == 1){
                tvLevel.setTextColor(Color.parseColor("#666666"));
                tvLevel.setText("진행문서");
                if (!SmartSingleton.arrSmartEmployees.isEmpty()) {
                    for (int i = 0; i < SmartSingleton.arrSmartEmployees.size(); i++) {
                        if( SmartSingleton.arrSmartEmployees.get(i).strCode.equals(smartApproval.strNowSign) ) {
                            tvName.setText(SmartSingleton.arrSmartEmployees.get(i).strName + " " + SmartSingleton.arrSmartEmployees.get(i).strPosition);
                        }
                    }
                }
            } else if(smartApproval.intLevel == 3){
                tvLevel.setText("반려문서");
            } else if(smartApproval.intLevel == 9){
                tvLevel.setText("완료문서");
            } else if(smartApproval.intLevel == 10){
                tvLevel.setText("전결문서");
                String[] arr_signDate = smartApproval.strSignDate.split("\\|");
                String[] arr_signCode = smartApproval.strSignCode.split("\\|");
                int cnt = arr_signDate.length - 2;
                if (!SmartSingleton.arrSmartEmployees.isEmpty()) {
                    for (int i = 0; i < SmartSingleton.arrSmartEmployees.size(); i++) {
                        if( SmartSingleton.arrSmartEmployees.get(i).strCode.equals(arr_signCode[cnt]) ) {
                            tvName.setText(SmartSingleton.arrSmartEmployees.get(i).strName + " " + SmartSingleton.arrSmartEmployees.get(i).strPosition);
                        }
                    }
                }

            } else {
                tvLevel.setText("");
            }

            tvDate.setText(smartApproval.datRegist.substring(0, 10));

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return mRows.size();
    }

    public void setLoaded() { isLoading = false; }

    /***************************************************************************/
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    /***************************************************************************/

    // 로딩용 뷰홀더 클래스 ########################################################################
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        private Context context;

        public LoadingViewHolder(Context context, final View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);

            this.context = context;
        }
    }

    // 뷰홀더 클래스 ###############################################################################
    public static class UserViewHolder extends RecyclerView.ViewHolder  { // implements View.OnClickListener

        public ImageView image;
        public TextView subject;
        public TextView title;
        public TextView cnt;
        public TextView docno;
        public TextView build;
        public TextView level;
        public TextView name;
        public TextView date;
        private Context context;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.r_sbc_btn_image);
            docno = (TextView) itemView.findViewById(R.id.r_sbc_docno);
            subject = (TextView) itemView.findViewById(R.id.r_sbc_subject) ;
            title = (TextView) itemView.findViewById(R.id.r_sbc_title);
            cnt = (TextView) itemView.findViewById(R.id.r_sbc_cnt);
            build = (TextView) itemView.findViewById(R.id.r_sbc_build);
            date = (TextView) itemView.findViewById(R.id.r_sbc_date);
            level = (TextView) itemView.findViewById(R.id.r_sbc_level);
            name = (TextView) itemView.findViewById(R.id.r_sbc_name);

            this.context = context;

            /***************************************************************************/
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });
            /***************************************************************************/
        }
    }




}
