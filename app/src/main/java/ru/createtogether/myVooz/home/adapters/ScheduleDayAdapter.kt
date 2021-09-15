package com.example.myvoozkotlin.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingViewPager
import ru.createtogether.myVooz.home.model.Lesson
import com.rd.PageIndicatorView
import ru.createtogether.myVooz.R


class ScheduleDayAdapter(val context: Context, private var lessons: List<List<Lesson>>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemPairLessonViewHolder(LayoutInflater.from(context).inflate(R.layout.item_pair_lesson, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemPairLessonViewHolder).bind(position)
    }

    fun update(news: List<List<Lesson>>) {
        this.lessons = news
        notifyDataSetChanged()
    }

    override fun getItemCount() = lessons.size

    private inner class ItemPairLessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val viewPager: LoopingViewPager = itemView.findViewById(R.id.viewpager)
        private val indicatorView: PageIndicatorView = itemView.findViewById(R.id.indicator)
        private val firstTimeTV: TextView = itemView.findViewById(R.id.tv_first_time)
        private val lastTimeTV: TextView = itemView.findViewById(R.id.tv_last_time)
        private val numberTV: TextView = itemView.findViewById(R.id.tv_number)



        fun bind(position: Int) {
            viewPager.adapter = SchedulePairAdapter(itemView.context, lessons[position], false)
            indicatorView.count = viewPager.indicatorCount
            viewPager.setIndicatorPageChangeListener(object :
                LoopingViewPager.IndicatorPageChangeListener {
                override fun onIndicatorProgress(selectingPosition: Int, progress: Float) {
                    indicatorView.setProgress(selectingPosition, progress)
                }

                override fun onIndicatorPageChange(newIndicatorPosition: Int) {
                    indicatorView.selection = newIndicatorPosition
                }

            })
            firstTimeTV.text = lessons[position][0].firstTime
            lastTimeTV.text = lessons[position][0].lastTime
            numberTV.text = lessons[position][0].number.toString() + "."
        }
    }
}