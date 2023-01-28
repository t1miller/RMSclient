package rr.rms.messaging


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rr.rms.R
import rr.rms.messaging.models.Message


class MessagingAdapter : RecyclerView.Adapter<MessagingAdapter.ViewHolder>() {

    private var mList = listOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.messaging_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = mList[position]
        holder.userNameTextView.text = msg.user
        holder.msgTextView.text = msg.msg
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun addMessages(newMsgs: List<Message>) {
        mList = newMsgs
        notifyDataSetChanged()
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.user_text)
        val msgTextView: TextView = itemView.findViewById(R.id.msg_text)
    }
}