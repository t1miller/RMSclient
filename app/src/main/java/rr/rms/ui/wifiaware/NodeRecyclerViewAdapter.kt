package rr.rms.ui.wifiaware


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rr.rms.R


class NodeRecyclerViewAdapter(
    private val mValues: List<NodeDataItem>,
    private val mListener: NodeListCallback? ) : RecyclerView.Adapter<NodeRecyclerViewAdapter.ViewHolder>() {

    /***/
    private val mOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        val item = v.tag as NodeDataItem
        mListener?.onNodeClicked(item)
    }

    /***/
    interface NodeListCallback {
        fun onNodeClicked(item: NodeDataItem?)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_nodesignature, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        // set data
        holder.mNodeId.text = item.node_id // node id of the peer
        holder.mNodeImage.setImageBitmap(item.bitmap) // the peers stamp that they sent us

        // set listener
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNodeId: TextView = mView.findViewById(R.id.node_id)
        var mNodeImage: ImageView = mView.findViewById(R.id.signature)
        override fun toString(): String {
            return super.toString() + " '" + mNodeId.text + "'"
        }
    }
}
