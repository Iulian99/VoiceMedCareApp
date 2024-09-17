package com.example.cocktailsapp.Setttings.Questions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R

class FaqAdapter(private val faqList: List<FaqItem>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.faq_item, parent, false)
        return FaqViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faqItem = faqList[position]

        holder.questionTextView.text = faqItem.question
        holder.answerTextView.text = faqItem.answer
        if (faqItem.isExpanded) {
            holder.answerTextView.visibility = View.VISIBLE
        } else {
            holder.answerTextView.visibility =View.GONE
        }

        holder.arrowImageView.setImageResource(if (faqItem.isExpanded) R.drawable.arrow_up else R.drawable.arrow_down)

        holder.itemView.setOnClickListener {
            faqItem.isExpanded = !faqItem.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return faqList.size
    }

    class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionTextView: TextView = itemView.findViewById(R.id.question_text_view)
        val answerTextView: TextView = itemView.findViewById(R.id.answer_text_view)
        val arrowImageView: ImageView = itemView.findViewById(R.id.arrow_image_view)
    }
}