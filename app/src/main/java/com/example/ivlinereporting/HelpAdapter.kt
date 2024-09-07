import android.content.Context
import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.ivlinereporting.R

class HelpAdapter(private val context: Context, private val listDataHeader: List<String>, private val listDataChild: HashMap<String, List<HelpItem>>) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return listDataHeader.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return listDataChild[listDataHeader[groupPosition]]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any {
        return listDataHeader[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return listDataChild[listDataHeader[groupPosition]]?.get(childPosition) ?: ""
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val headerTitle = getGroup(groupPosition) as String
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView = inflater.inflate(R.layout.list_group, null)

        val lblListHeader = convertView.findViewById<TextView>(R.id.lblListHeader)
        lblListHeader.setTypeface(null, Typeface.BOLD)
        lblListHeader.text = headerTitle

        return convertView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView = inflater.inflate(R.layout.list_item, null)

        val childText = getChild(groupPosition, childPosition) as HelpItem
        val txtListChild = convertView.findViewById<TextView>(R.id.lblListItem)
        txtListChild.text = childText.text
        txtListChild.movementMethod = LinkMovementMethod.getInstance()

        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}

data class HelpItem(val text: CharSequence, val layoutResId: Int)