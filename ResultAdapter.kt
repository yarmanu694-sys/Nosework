package adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.my.R
import models.ResultItem
class ResultAdapter(private val context: Context, private val results: List<ResultItem>) : BaseAdapter() {

    override fun getCount(): Int = results.size

    override fun getItem(position: Int): Any = results[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.ir, parent, false)
            holder = ViewHolder(
                view.findViewById(R.id.tv_participant),
                view.findViewById(R.id.tv_category),
                view.findViewById(R.id.tv_duration),
                view.findViewById(R.id.tv_markers),
                view.findViewById(R.id.tv_penalties)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = results[position]
        // Используем строки из strings.xml
        holder.tvParticipant.text = "${context.getString(R.string.participant_label)}${item.participantId}"
        holder.tvCategory.text = "${context.getString(R.string.category_label)}${item.categoryName}"
        holder.tvDuration.text = "${context.getString(R.string.duration_label)}${item.duration}"
        holder.tvMarkers.text = "${context.getString(R.string.markers_label)}${item.foundMarkers}/${item.totalMarkers}"
        holder.tvPenalties.text = "${context.getString(R.string.penalties_label)}${item.penaltyPoints}"

        return view
    }

    private data class ViewHolder(
        val tvParticipant: TextView,
        val tvCategory: TextView,
        val tvDuration: TextView,
        val tvMarkers: TextView,
        val tvPenalties: TextView
    )
}