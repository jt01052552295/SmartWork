package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.SmartApprovalDetails;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;

/**
 * Created by Administrator on 2017-01-04.
 */

public class SmartApprovalViewDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final String TAG = "SmartApprovalViewDetailsAdapter";
    private RbPreference pref;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private ArrayList<SmartApprovalDetails> mRows;
    private Context getContext() {
        return mContext;
    }

    public SmartApprovalViewDetailsAdapter(Context context, ArrayList<SmartApprovalDetails> arrRows){
        mRows = arrRows;
        mContext = context;
        pref = new RbPreference(context.getApplicationContext());
    }

    public void setmOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void add(SmartApprovalDetails item, int position) {
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_approval_view_detail, parent, false);
            return new UserViewHolder(getContext(), view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_loading_item, parent, false);
            return new LoadingViewHolder(getContext(), view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SmartApprovalDetails row = mRows.get(position);
        if (holder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            // 생성되는 View에 position값 저장
            userViewHolder.position = position;

            TextView tvName = userViewHolder.name;
            TextView tvCost1 = userViewHolder.cost1;
            TextView tvCost2 = userViewHolder.cost2;
            TextView tvDate1 = userViewHolder.date1;
            TextView tvDate2 = userViewHolder.date2;
            TextView tvTel = userViewHolder.tel;
            TextView tvPerson = userViewHolder.person;
            TextView tvPersonTel = userViewHolder.personTel;
            TextView tvMemo = userViewHolder.memo;

            tvName.setText(row.strComName);
            tvCost1.setText("");
            tvCost2.setText("");
            if(!row.strCost1.isEmpty()){
                tvCost1.setText(row.strCost1 + "원");
            }
            if(!row.strCost2.isEmpty()){
                tvCost2.setText(row.strCost2 + "원");
            }

            tvDate1.setText(row.strDate1);
            tvDate2.setText(row.strDate2);
            tvTel.setText(row.strComTel);
            tvPerson.setText(row.strComPerson);
            tvPersonTel.setText(row.strComPersonTel);
            tvMemo.setText(row.strComMemo);

            Log.d(TAG, " 리스트 생성 : " +position+ " / " +row.strComName+ " / " +row.strComPerson+ " / " );

        } else if (holder instanceof LoadingViewHolder)  {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);

            Log.d(TAG, " 리스트 생성 : 실패 !! ");
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
        public TextView name;
        public TextView cost1;
        public TextView cost2;
        public TextView date1;
        public TextView date2;
        public TextView tel;
        public TextView person;
        public TextView personTel;
        public TextView memo;

        public UserViewHolder(View itemView) {
            super(itemView);
        }

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            this.context = context;
            name = (TextView) itemView.findViewById(R.id.r_approval_view_detail_name);
            cost1 = (TextView) itemView.findViewById(R.id.r_approval_view_detail_cost1);
            cost2 = (TextView) itemView.findViewById(R.id.r_approval_view_detail_cost2);
            date1 = (TextView) itemView.findViewById(R.id.r_approval_view_detail_date1);
            date2 = (TextView) itemView.findViewById(R.id.r_approval_view_detail_date2);
            tel = (TextView) itemView.findViewById(R.id.r_approval_view_detail_tel);
            person = (TextView) itemView.findViewById(R.id.r_approval_view_detail_person);
            personTel = (TextView) itemView.findViewById(R.id.r_approval_view_detail_person_tel);
            memo = (TextView) itemView.findViewById(R.id.r_approval_view_detail_memo);
        }
    }


}