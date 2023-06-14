package com.delloon.ocentar.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delloon.ocentar.adminPanel.ReportCommentActivity
import com.delloon.ocentar.databinding.ListReportCommentBinding
import com.delloon.ocentar.model.DBManager
import com.delloon.ocentar.model.ReportCommentData
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class RecyclerViewReportCommentAdapter(val activity: ReportCommentActivity) : RecyclerView.Adapter<RecyclerViewReportCommentAdapter.ReportCommentHolder>() {
    private val commentReportArray = ArrayList<ReportCommentData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportCommentHolder {
        val binding = ListReportCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportCommentHolder(binding, activity)
    }

    override fun getItemCount(): Int {
        return commentReportArray.size
    }

    override fun onBindViewHolder(holder: ReportCommentHolder, position: Int) {
        holder.setData(commentReportArray[position])
    }

    fun updateAdapter(newList: List<ReportCommentData>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelperReportComment(commentReportArray, newList))
        commentReportArray.clear()
        commentReportArray.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class ReportCommentHolder(val binding: ListReportCommentBinding, val activity: ReportCommentActivity) :
        RecyclerView.ViewHolder(binding.root) {
        val auth = FirebaseAuth.getInstance()
        val dbManager = DBManager()

        fun setData(reportComment: ReportCommentData) {
            with(binding) {
                Picasso.get().load(reportComment.photoProfile).into(imageViewAvatarReportComment)
                textViewReportComment.text = reportComment.comment
                textViewNickNameReportComment.text = reportComment.nickname

                buttonReportSkipComment.setOnClickListener {
                    dbManager.skipReportComment(reportComment, activity, object :
                        DBManager.FinishWorkListener {
                        override fun onFinish() {
                            activity.updateAdapter()
                        }
                    })
                }
                buttonReportBlockComment.setOnClickListener {
                    dbManager.blockReportComment(reportComment, activity, object :
                        DBManager.FinishWorkListener {
                        override fun onFinish() {
                            activity.updateAdapter()
                        }
                    })
                }
            }
        }
    }
}
