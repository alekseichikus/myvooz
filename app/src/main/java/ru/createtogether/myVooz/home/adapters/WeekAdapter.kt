package com.example.myvoozkotlin.home.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myvoozkotlin.helpers.Utils
import com.example.myvoozkotlin.helpers.hide
import com.example.myvoozkotlin.helpers.show
import com.example.myvoozkotlin.home.helpers.OnDatePicked
import com.example.myvoozkotlin.home.helpers.OnDayPicked
import ru.createtogether.myVooz.R
import ru.createtogether.myVooz.databinding.ItemDayBinding
import java.util.*

class WeekAdapter(private var calendar: Calendar, private val onDayPicked: OnDayPicked): RecyclerView.Adapter<WeekAdapter.IntercomViewHolder>() {

    inner class IntercomViewHolder(val binding : ItemDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntercomViewHolder {
        return IntercomViewHolder(ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: IntercomViewHolder, position: Int) {
        val binding = holder.binding
        val c = Utils.getCalendarDayOfWeek(calendar, position)

        holder.itemView.setOnClickListener {
            onDayPicked.onDayClick(position)
        }

        binding.apply {
            if(position == 6){
                tvNumberName.hide()
                ivArrow.show()
                binding.root.setBackgroundColor(Color.TRANSPARENT)
                return
            }

            //tvDayName.show()
            tvNumberName.show()
            ivArrow.hide()

            tvNumberName.text = c.get(Calendar.DAY_OF_MONTH).toString()

            val resource = binding.root.resources

            if(calendar.equals(c)){
                tvNumberName.setTextColor(resource.getColor(R.color.white))
                val drawable = holder.itemView.context.getDrawable(R.drawable.circle_tintable)
                drawable!!.setTint(ContextCompat.getColor(holder.itemView.context, R.color.textSecondary))
                binding.root.background = drawable
            }
            else{
                tvNumberName.setTextColor(resource.getColor(R.color.textTertiary))
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    fun update(calendar: Calendar) {
        this.calendar = calendar
        notifyDataSetChanged()
    }

    override fun getItemCount() = 7
}