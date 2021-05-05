package ipvc.estg.fixity.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.fixity.R
import ipvc.estg.fixity.api.Category


class CategoryAdapter(context: Context, private var listener: CategoryRecycler) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var category = emptyList<Category>()
    private var arrayCategories: ArrayList<Int> = ArrayList()
    private var reset: Boolean = false

    inner class CategoryViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val cat: TextView = itemView.findViewById(R.id.categoryTxt)
        var checkCat: CheckBox = itemView.findViewById(R.id.checkCategory)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = inflater.inflate(R.layout.row_category_item, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val current = category[position]
        holder.cat.text = current.category

        holder.itemView.tag = current
        holder.checkCat.tag = current

        if (reset) {
            holder.checkCat.isChecked = false
        }

        holder.checkCat.setOnClickListener {

            current.isSelected = holder.checkCat.isChecked

            if (current.isSelected) {
                arrayCategories.add(position + 1)
            } else {
                arrayCategories.remove(position + 1)
            }

            listener.updateCategoryFilter(arrayCategories)

        }
    }

    internal fun setCategory(category: List<Category>) {
        this.category = category
        notifyDataSetChanged()
    }

    internal fun resetCheckbox(res: Boolean) {
        reset = res
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return category.size
    }

    interface CategoryRecycler {
        fun updateCategoryFilter(category: ArrayList<Int>)
    }

}