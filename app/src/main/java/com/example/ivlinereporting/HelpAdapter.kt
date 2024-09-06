import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.ivlinereporting.LayoutItem

class HelpAdapter(private val context: Context, private val groupList: List<String>, private val childList: HashMap<String, List<Any>>) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = groupList.size

    override fun getChildrenCount(groupPosition: Int): Int = childList[groupList[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = groupList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = childList[groupList[groupPosition]]!![childPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        }
        val textView = view?.findViewById<TextView>(android.R.id.text1)
        textView?.text = getGroup(groupPosition).toString()
        return view!!
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val child = getChild(groupPosition, childPosition)
        if (child is LayoutItem) {
            return LayoutInflater.from(context).inflate(child.layoutResId, parent, false)
        } else {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_2, parent, false)
            }
            val textView = view?.findViewById<TextView>(android.R.id.text1)
            textView?.text = child.toString()
            return view!!
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}

