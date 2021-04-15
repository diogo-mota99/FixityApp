package ipvc.estg.fixity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.fixity.entities.Note
import ipvc.estg.fixity.viewModel.NoteViewModel
import ipvc.estg.fixity.R
import java.text.SimpleDateFormat


class NoteAdapter(
    context: Context,
    onClickListener: View.OnClickListener/*,
    onDeletePressed: View.OnClickListener,
    onEditPressed: View.OnClickListener*/
) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notes = emptyList<Note>()
    private lateinit var noteViewModel: NoteViewModel
    private var clickListener: View.OnClickListener = onClickListener
    //private var editClickListener: View.OnClickListener = onEditPressed
    //private var deleteClickListener: View.OnClickListener = onDeletePressed


    inner class NoteViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val noteItemTitle: TextView = itemView.findViewById(R.id.note_title)
        val noteItemDescription: TextView = itemView.findViewById(R.id.note_description)
        val noteItemDate: TextView = itemView.findViewById(R.id.note_date)
        val btnDeleteNote: ImageView = itemView.findViewById(R.id.deleteBtn)
        val btnEditNote: ImageView = itemView.findViewById(R.id.editBtn)
        val noteItemLayout: CardView = itemView.findViewById(R.id.cardNotes)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return NoteViewHolder(itemView)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int
    ) {

        val dateFormat: String = "dd-MM-yyyy HH:mm" //Date format is Specified
        val strDateFormat =
            SimpleDateFormat(dateFormat) //Date format string is passed as an argument to the Date format object

        val current = notes[position]
        holder.noteItemTitle.text = current.title
        holder.noteItemDescription.text = current.description
        if (current.date != null) {
            holder.noteItemDate.text = strDateFormat.format(current.date)
        }

        holder.itemView.tag = current
        holder.btnDeleteNote.tag = current
        holder.btnEditNote.tag = current
        holder.itemView.setOnClickListener(clickListener)
        //holder.btnEditNote.setOnClickListener(editClickListener)
        //holder.btnDeleteNote.setOnClickListener(deleteClickListener)*/
    }

    internal fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    override fun getItemCount() = notes.size

}